package com.Tithaal.Wallet.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPublicKey;

/**
 * Validates RS256-signed JWTs issued by the Centralized Auth Service.
 * The RSA public key is fetched once from the JWKS endpoint at startup.
 * No database call is made during token validation.
 *
 * Uses JJWT 0.12.6 API.
 */
@Component
@Slf4j
public class RsaJwtValidator {

    @Value("${auth.service.jwks-uri}")
    private String jwksUri;

    private RSAPublicKey publicKey;

    @PostConstruct
    public void init() {
        this.publicKey = JwksUtil.fetchPublicKey(jwksUri);
        log.info("RS256 public key loaded from: {}", jwksUri);
    }

    /**
     * Validates the token signature + expiry and returns all claims.
     * JJWT 0.12.6: use Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
     *
     * @throws io.jsonwebtoken.JwtException on any validation failure
     */
    public Claims validateAndGetClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Returns true if the token is valid (signature + expiry), false otherwise.
     */
    public boolean isValid(String token) {
        try {
            validateAndGetClaims(token);
            return true;
        } catch (SignatureException ex) {
            log.warn("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.warn("Malformed JWT token");
        } catch (ExpiredJwtException ex) {
            log.warn("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.warn("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims string is empty");
        }
        return false;
    }
}
