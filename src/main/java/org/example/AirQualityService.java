package org.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * AirQualityService: Fetches air quality data for Tunis, Tunisia from OpenWeatherMap API
 * 
 * This service monitors air pollution levels and provides health alerts for asthma patients.
 * 
 * Location: Tunis, Tunisia (36.8065°N, 10.1815°E)
 * 
 * Configuration:
 * - Set OPENWEATHERMAP_API_KEY environment variable or system property
 * - The service runs asynchronously and won't block the UI
 * 
 * Air Quality Index (AQI) Levels:
 * 1 = Good
 * 2 = Fair
 * 3 = Moderate
 * 4 = Poor
 * 5 = Very Poor (Hazardous)
 */
public class AirQualityService {

    // Tunis, Tunisia coordinates
    private static final double TUNIS_LATITUDE = 36.8065;
    private static final double TUNIS_LONGITUDE = 10.1815;
    private static final String OPENWEATHERMAP_API_URL = "http://api.openweathermap.org/data/2.5/air_pollution";

    private final String apiKey;
    private final HttpClient httpClient;

    public AirQualityService() {
        this.apiKey = getConfigValue("OPENWEATHERMAP_API_KEY", "openweathermap.api.key");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        if (isConfigured()) {
            System.out.println("✅ Air Quality Service Initialized for Tunis, Tunisia");
        } else {
            System.out.println("⚠️ Air Quality Service: API key not configured. Weather warnings will be disabled.");
        }
    }

    /**
     * Fetch air quality asynchronously and return a CompletableFuture
     * 
     * @return CompletableFuture with AirQualityData or null if service not configured
     */
    public CompletableFuture<AirQualityData> fetchAirQualityAsync() {
        if (!isConfigured()) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                return fetchAirQuality();
            } catch (Exception e) {
                System.err.println("❌ Error fetching air quality: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        });
    }

    /**
     * Fetch air quality data from OpenWeatherMap API
     * This method is synchronous - use fetchAirQualityAsync() for non-blocking calls
     * 
     * @return AirQualityData containing AQI and details
     */
    private AirQualityData fetchAirQuality() throws Exception {
        String url = buildApiUrl();
        System.out.println("🌍 Fetching air quality for Tunis...");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("OpenWeatherMap API error (" + response.statusCode() + "): " + response.body());
        }

        return parseAirQualityResponse(response.body());
    }

    /**
     * Parse JSON response from OpenWeatherMap API
     * 
     * Response structure:
     * {
     *   "list": [
     *     {
     *       "main": {
     *         "aqi": 1-5
     *       },
     *       "components": {
     *         "pm2_5": value,
     *         "pm10": value,
     *         "no2": value
     *       }
     *     }
     *   ]
     * }
     */
    private AirQualityData parseAirQualityResponse(String jsonResponse) {
        try {
            JsonObject rootObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            
            if (!rootObject.has("list")) {
                System.out.println("⚠️ No 'list' field in air quality response");
                return null;
            }

            var listArray = rootObject.getAsJsonArray("list");
            if (listArray == null || listArray.isEmpty()) {
                System.out.println("⚠️ No air quality data available for Tunis");
                return null;
            }

            JsonObject listItem = listArray.get(0).getAsJsonObject();
            
            if (!listItem.has("main")) {
                System.out.println("⚠️ No 'main' field in air quality data");
                return null;
            }

            JsonObject main = listItem.getAsJsonObject("main");
            if (!main.has("aqi")) {
                System.out.println("⚠️ No 'aqi' field in main object");
                return null;
            }

            int aqi = main.get("aqi").getAsInt();

            // Extract component data if available
            String pm25 = "N/A";
            String pm10 = "N/A";
            String no2 = "N/A";

            if (listItem.has("components")) {
                JsonObject components = listItem.getAsJsonObject("components");
                pm25 = components.has("pm2_5") ? String.format("%.1f", components.get("pm2_5").getAsDouble()) : "N/A";
                pm10 = components.has("pm10") ? String.format("%.1f", components.get("pm10").getAsDouble()) : "N/A";
                no2 = components.has("no2") ? String.format("%.1f", components.get("no2").getAsDouble()) : "N/A";
            }

            AirQualityData data = new AirQualityData(aqi, pm25, pm10, no2);
            System.out.println("✅ Air Quality Data Retrieved: AQI=" + aqi + ", PM2.5=" + pm25);
            return data;

        } catch (Exception e) {
            System.err.println("❌ Error parsing air quality response: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Build the OpenWeatherMap API URL
     */
    private String buildApiUrl() {
        return String.format("%s?lat=%.4f&lon=%.4f&appid=%s",
                OPENWEATHERMAP_API_URL,
                TUNIS_LATITUDE,
                TUNIS_LONGITUDE,
                apiKey);
    }

    /**
     * Check if API is configured
     */
    private boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }

    /**
     * Get configuration value from environment or system properties
     */
    private String getConfigValue(String envVarName, String systemPropertyName) {
        String envValue = System.getenv(envVarName);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }

        String propValue = System.getProperty(systemPropertyName);
        return propValue != null && !propValue.isEmpty() ? propValue : null;
    }

    /**
     * Get health warning message based on AQI
     */
    public String getHealthWarning(int aqi) {
        return switch (aqi) {
            case 1, 2 -> "Air Quality: Good. Great day for a walk!";
            case 3 -> "Air Quality: Moderate. Sensitive groups should limit outdoor activities.";
            case 4, 5 -> "Health Alert: Poor air quality in Tunis today. Asthma patients should wear a mask.";
            default -> "Air Quality: Unknown";
        };
    }

    /**
     * Get background color CSS for the warning box based on AQI
     */
    public String getWarningBoxColor(int aqi) {
        return switch (aqi) {
            case 1, 2 -> "-fx-background-color: #d4edda; -fx-border-color: #28a745;"; // Light green
            case 3 -> "-fx-background-color: #fff3cd; -fx-border-color: #ffc107;"; // Light yellow
            case 4, 5 -> "-fx-background-color: #f8d7da; -fx-border-color: #dc3545;"; // Light red
            default -> "-fx-background-color: #e2e3e5; -fx-border-color: #6c757d;"; // Gray
        };
    }

    /**
     * Get text color CSS based on AQI
     */
    public String getWarningTextColor(int aqi) {
        return switch (aqi) {
            case 1, 2 -> "-fx-text-fill: #155724;"; // Dark green
            case 3 -> "-fx-text-fill: #856404;"; // Dark yellow/brown
            case 4, 5 -> "-fx-text-fill: #721c24;"; // Dark red
            default -> "-fx-text-fill: #383d41;"; // Dark gray
        };
    }

    /**
     * Inner class to hold air quality data
     */
    public static class AirQualityData {
        public final int aqi;
        public final String pm25;
        public final String pm10;
        public final String no2;

        public AirQualityData(int aqi, String pm25, String pm10, String no2) {
            this.aqi = aqi;
            this.pm25 = pm25;
            this.pm10 = pm10;
            this.no2 = no2;
        }

        @Override
        public String toString() {
            return "AirQualityData{" +
                    "aqi=" + aqi +
                    ", pm2.5=" + pm25 +
                    ", pm10=" + pm10 +
                    ", no2=" + no2 +
                    '}';
        }
    }
}

