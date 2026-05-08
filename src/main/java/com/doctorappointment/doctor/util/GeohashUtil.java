package com.doctorappointment.doctor.util;

import ch.hsr.geohash.GeoHash;

import java.util.HashSet;
import java.util.Set;

public class GeohashUtil {
    private static final int PRECISION = 6;
    private GeohashUtil() {}

    /**
      1. Latitude and longitude ranges are repeatedly divided
      2. Each split generates binary bits
      3. Bits are combined and encoded into Base32 string
     */
    public static String encode(double latitude, double longitude) {
        return GeoHash
                .withCharacterPrecision(latitude, longitude, PRECISION) // create geohash using given precision
                .toBase32(); // convert binary representation to readable Base32 string
    }

    public static Set<String> getNeighborAndSelf(String geohash) {

        // Using Set to avoid duplicates
        Set<String> result = new HashSet<>();

        // Add the center geohash itself
        result.add(geohash);

        GeoHash center = GeoHash.fromGeohashString(geohash); // convert string to geohash as this method works on geohash object
        for (GeoHash neighbour : center.getAdjacent()) { //fetch all neighboring geohashes

            result.add(neighbour.toBase32()); //convert each neighbor back to base32 string and database stores as string
        }

        return result;
    }
}
