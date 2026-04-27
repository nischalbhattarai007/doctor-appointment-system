package com.doctorappointment.doctor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class GeocodingService {
    private static final String PHOTON_URL = "https://photon.komoot.io/api/";
    private final HttpClient client;
    private final ObjectMapper objectMapper;

    public GeocodingService(@Client(PHOTON_URL) HttpClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    // used for register/update doctor (full clinic detail)
    // conversion between text to coordinates
    // all three fields are required and validated before calling this
    public double[] getCoordinates(String street, String area, String city) {
        // build query in specific-to-general order for best Photon accuracy
        // e.g. "Gyaneshwor Marga, Nepaltar, Kathmandu, Nepal"
        String query = street.trim() + ", " + area.trim() + ", " + city.trim() + ", Nepal";
        return resolveCoordinates(query);
    }

    // used for location search (GetNearestDoctor, GetDoctorsByLocation)
    public double[] getCoordinates(String locationName) {
        try {
            return resolveCoordinates(locationName.trim());
        } catch (Exception e) {
            log.error("Geocoding failed for '{}': {}", locationName, e.getMessage());
            return defaultKathmandu();
        }
    }

    // shared private method — actual HTTP call happens here
    // calls the Photon API, reads the JSON response, and extracts the coordinates.
    private double[] resolveCoordinates(String queryText) {
        try {
            String url = "?q=" + queryText.replace(" ", "+") + "&limit=1";
            log.info("Calling Photon with URL: {}", url);

            String response = client
                    /*
                        micronaut http client is non-blocking by default
                        in here we have to wait for the api response and then continue
                        that is why blocking is used here.
                    */
                    .toBlocking()
                    .retrieve(HttpRequest.GET(url)); // send request and get back body as string
            log.info("Photon raw response: {}", response);

            JsonNode root = objectMapper.readTree(response); // converts raw JSON string into a tree
            JsonNode features = root.get("features"); // goes inside and grabs the feature array

            if (features == null || features.isEmpty()) {
                log.warn("No coordinates found for query: '{}'", queryText);
                return defaultKathmandu();
            }

            // extract coordinates
            JsonNode coords = features.get(0).get("geometry").get("coordinates");

            // Photon returns [longitude, latitude] — note the reversed order
            double longitude = coords.get(0).asDouble();
            double latitude = coords.get(1).asDouble();

            log.info("Resolved '{}' → lat={}, lon={}", queryText, latitude, longitude);
            return new double[]{latitude, longitude};

        } catch (Exception e) {
            log.error("Photon HTTP call failed for '{}': {}", queryText, e.getMessage());
            return defaultKathmandu();
        }
    }

    private double[] defaultKathmandu() {
        return new double[]{27.7172, 85.3240};
    }
}