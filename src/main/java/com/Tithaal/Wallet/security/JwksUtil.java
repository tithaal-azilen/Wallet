package com.Tithaal.Wallet.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Slf4j
public class JwksUtil {

    private JwksUtil() {}

    /**
     * Fetches the JWKS endpoint and extracts the first RSA public key.
     */
    public static RSAPublicKey fetchPublicKey(String jwksUri) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(jwksUri))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IllegalStateException("Failed to fetch JWKS from " + jwksUri
                        + " — HTTP " + response.statusCode());
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            JsonNode keys = root.get("keys");

            if (keys == null || !keys.isArray() || keys.isEmpty()) {
                throw new IllegalStateException("No keys found in JWKS response from " + jwksUri);
            }

            // Use the first RSA key
            JsonNode keyNode = keys.get(0);
            String nBase64 = keyNode.get("n").asText();
            String eBase64 = keyNode.get("e").asText();

            Base64.Decoder decoder = Base64.getUrlDecoder();
            BigInteger modulus = new BigInteger(1, decoder.decode(nBase64));
            BigInteger exponent = new BigInteger(1, decoder.decode(eBase64));

            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(spec);

            log.info("Successfully loaded RSA public key from JWKS endpoint: {}", jwksUri);
            return publicKey;

        } catch (Exception e) {
            throw new IllegalStateException("Could not load RSA public key from JWKS: " + jwksUri, e);
        }
    }
}
