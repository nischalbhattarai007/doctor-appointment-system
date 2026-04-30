package com.doctorappointment.doctor.util;

import ch.hsr.geohash.GeoHash;

import java.util.HashSet;
import java.util.Set;

/**
 * WHY Geohash?
 * ------------
 * Geohash converts latitude & longitude into a short string.
 * Nearby locations will have similar prefixes.
 *
 * This is extremely useful for:
 * - Fast location-based search (e.g., "find doctors near me")
 * - Efficient database queries (prefix matching instead of full geo calculations)
 * - Reducing computation compared to Haversine distance on every row
 */
public class GeohashUtil {

    /**
     * Precision controls the size of the geographic grid.
     *
     * WHY PRECISION = 6?
     * -------------------
     * - Lower precision → larger area (less accurate, faster queries)
     * - Higher precision → smaller area (more accurate, more queries needed)
     *
     * Approx size at precision 5:
     * ~4.9km x 4.9km area
     *
     * This is a good balance for:
     * - City-level proximity search
     * - Reducing database load
     */
    private static final int PRECISION = 6;

    /**
     * Private constructor to prevent instantiation.
     *
     * WHY?
     * ----
     * This is a pure utility class (only static methods).
     * No need to create objects of this class.
     */
    private GeohashUtil() {}

    /**
     * Encode latitude and longitude into a Geohash string.
     *
     * @param latitude  - GPS latitude (-90 to +90)
     * @param longitude - GPS longitude (-180 to +180)
     * @return Geohash string (Base32 encoded)
     *
     * WHY this method exists:
     * -----------------------
     * - Converts raw coordinates into a searchable key
     * - Makes querying databases much faster
     *
     * HOW it works internally (high-level):
     * -------------------------------------
     * 1. Latitude and longitude ranges are repeatedly divided
     * 2. Each split generates binary bits
     * 3. Bits are combined and encoded into Base32 string
     *
     * Example:
     * Input  → (27.7172, 85.3240)
     * Output → "tu4prr"
     *
     * Nearby locations will share prefix:
     * tu4pr, tu4ps, tu4pt → all close to each other
     */
    public static String encode(double latitude, double longitude) {
        return GeoHash
                .withCharacterPrecision(latitude, longitude, PRECISION) // create geohash using given precision
                .toBase32(); // convert binary representation to readable Base32 string
    }

    /**
     * Get the geohash of a location AND all its neighboring cells.
     *
     * @param geohash - center geohash
     * @return Set containing:
     *         - original geohash
     *         - 8 surrounding neighbor geohashes
     *
     * WHY this method is CRITICAL:
     * ----------------------------
     * Problem:
     * If you search using only ONE geohash,
     * you might MISS nearby results near the boundary.
     *
     * Example:
     * --------
     * [ A ][ B ]
     * [ C ][ D ]
     *
     * If user is in A, and a doctor is in B (very close physically),
     * searching only A will NOT return B → WRONG RESULT
     *
     * Solution:
     * ---------
     * Include:
     * - Center cell
     * - All 8 neighbors
     *
     * This ensures:
     * - No edge-case misses
     * - Accurate "nearby" results
     *
     * This is exactly how:
     * - Uber finds nearby drivers
     * - Food delivery apps find nearby restaurants
     */
    public static Set<String> getNeighborAndSelf(String geohash) {

        // Using Set to avoid duplicates
        Set<String> result = new HashSet<>();

        // Add the center geohash itself
        result.add(geohash);

        /**
         * Convert string back to GeoHash object
         *
         * WHY?
         * ----
         * The library methods (like getAdjacent) work on GeoHash objects,
         * not raw strings.
         */
        GeoHash center = GeoHash.fromGeohashString(geohash);

        /**
         * Fetch all 8 neighboring geohashes
         *
         * These represent:
         * N, NE, E, SE, S, SW, W, NW
         */
        for (GeoHash neighbour : center.getAdjacent()) {

            /**
             * Convert each neighbor back to Base32 string
             *
             * WHY?
             * ----
             * - Database stores geohash as string
             * - Queries are done using string matching
             */
            result.add(neighbour.toBase32());
        }

        return result;
    }
}
