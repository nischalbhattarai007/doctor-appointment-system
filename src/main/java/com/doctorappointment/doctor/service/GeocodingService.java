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
    public double[] getCoordinates(String clinicName, String clinicBuilding, String clinicAddress) {
        try {
            StringBuilder query = new StringBuilder();
            if (clinicName != null && !clinicName.isBlank()) {
                query.append(clinicName.trim()).append(", ");
            }
            if (clinicBuilding != null && !clinicBuilding.isBlank()) {
                query.append(clinicBuilding.trim()).append(", ");
            }
            query.append(clinicAddress.trim());

            return resolveCoordinates(query.toString());

        } catch (Exception e) {
            log.error("Geocoding failed: {}", e.getMessage());
            return defaultKathmandu();
        }
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
    private double[] resolveCoordinates(String queryText) {
        try {
            String url = "?q=" + queryText.replace(" ", "+") + "&limit=1";
            log.info("Calling Photon with URL: {}", url);

            String response = client
                    .toBlocking()
                    .retrieve(HttpRequest.GET(url));
            log.info("Photon raw response: {}", response);

            JsonNode root = objectMapper.readTree(response);
            JsonNode features = root.get("features");

            if (features == null || features.isEmpty()) {
                log.warn("No coordinates found for query: '{}'", queryText);
                return defaultKathmandu();
            }

            JsonNode coords = features.get(0)
                    .get("geometry")
                    .get("coordinates");

            // Photon returns [longitude, latitude]
            double longitude = coords.get(0).asDouble();
            double latitude  = coords.get(1).asDouble();

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