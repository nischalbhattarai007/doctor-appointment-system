package com.doctorappointment.doctor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.annotation.Client;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import io.micronaut.http.client.HttpClient;
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

    public double[] getCoordinates(String address) {
        try {
            String url =  "?q=" + address.replace(" ", "+") + "&limit=1";
            log.info("Calling Photon with URL: {}", url);
            String response = client
                    .toBlocking()
                    .retrieve(HttpRequest.GET(url));
            log.info("Photon raw response: {}", response);

            JsonNode root = objectMapper.readTree(response);
            JsonNode features = root.get("features");

            if (features == null || features.isEmpty()) {
                log.warn("No coordinates found for address: {}", address);
                // default to Kathmandu center if not found
                return new double[]{27.7172, 85.3240};
            }

            JsonNode cords = features.get(0)
                    .get("geometry")
                    .get("coordinates");

            // Photon returns [longitude, latitude]
            double longitude = cords.get(0).asDouble();
            double latitude = cords.get(1).asDouble();

            log.info("Coordinates for {}: lat={}, lon={}", address, latitude, longitude);
            return new double[]{latitude, longitude};

        } catch (Exception e) {
            log.error("Geocoding failed for address {}: {}", address, e.getMessage());
            // default to Kathmandu center if API fails
            return new double[]{27.7172, 85.3240};
        }
    }}
