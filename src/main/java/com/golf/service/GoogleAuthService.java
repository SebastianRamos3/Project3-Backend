package com.golf.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class GoogleAuthService {

    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GoogleAuthService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Verifies a Google ID token and returns user information
     * 
     * @param idToken The Google ID token to verify
     * @return GoogleUserInfo containing email, name, and picture, or null if
     *         invalid
     */
    public GoogleUserInfo verifyIdToken(String idToken) {
        try {
            String url = GOOGLE_TOKEN_INFO_URL + idToken;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return null;
            }

            JsonNode jsonNode = objectMapper.readTree(response.body());

            // Check if the token is valid (has email and not expired)
            if (!jsonNode.has("email") || !jsonNode.has("email_verified")) {
                return null;
            }

            // Check if email is verified
            if (!jsonNode.get("email_verified").asBoolean()) {
                return null;
            }

            String email = jsonNode.get("email").asText();
            String name = jsonNode.has("name") ? jsonNode.get("name").asText() : email.split("@")[0];
            String picture = jsonNode.has("picture") ? jsonNode.get("picture").asText() : null;
            String givenName = jsonNode.has("given_name") ? jsonNode.get("given_name").asText() : null;
            String familyName = jsonNode.has("family_name") ? jsonNode.get("family_name").asText() : null;
            String googleId = jsonNode.has("sub") ? jsonNode.get("sub").asText() : null;

            return new GoogleUserInfo(email, name, picture, givenName, familyName, googleId);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class GoogleUserInfo {
        private final String email;
        private final String name;
        private final String picture;
        private final String givenName;
        private final String familyName;
        private final String googleId;

        public GoogleUserInfo(String email, String name, String picture, String givenName, String familyName,
                String googleId) {
            this.email = email;
            this.name = name;
            this.picture = picture;
            this.givenName = givenName;
            this.familyName = familyName;
            this.googleId = googleId;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }

        public String getPicture() {
            return picture;
        }

        public String getGivenName() {
            return givenName;
        }

        public String getFamilyName() {
            return familyName;
        }

        public String getGoogleId() {
            return googleId;
        }
    }
}
