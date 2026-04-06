package com.Tithaal.Wallet.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

/**
 * Client to communicate with the Centralized Auth Service.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthServiceClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${auth.service.base-url}")
    private String authServiceBaseUrl;

    /**
     * Fetches the email of a user from the Auth Service.
     * Assuming endpoint GET /api/v1/users/{userId} returns a JSON with an "email" field.
     */
    public String getUserEmail(UUID userId) {
        String url = authServiceBaseUrl + "/api/v1/users/" + userId;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("email")) {
                return (String) response.get("email");
            }
            log.warn("Email not found for userId: {} in Auth Service response", userId);
        } catch (Exception e) {
            log.error("Failed to fetch email for userId: {} from Auth Service at {}", userId, url, e);
        }
        return null;
    }
}
