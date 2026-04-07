package com.Tithaal.Wallet.client;

import com.Tithaal.Wallet.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
     * Fetches the full user details from the Auth Service.
     */
    public UserResponseDTO getUserById(UUID userId) {
        String url = authServiceBaseUrl + "/api/v1/users/" + userId;
        try {
            return restTemplate.getForObject(url, UserResponseDTO.class);
        } catch (Exception e) {
            log.error("Failed to fetch user details for userId: {} from Auth Service at {}", userId, url, e);
            return null;
        }
    }

    /**
     * Fetches the email of a user from the Auth Service.
     */
    public String getUserEmail(UUID userId) {
        UserResponseDTO user = getUserById(userId);
        if (user != null) {
            return user.getEmail();
        }
        return null;
    }
}
