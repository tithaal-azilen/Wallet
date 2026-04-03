package com.Tithaal.Wallet.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Utility to extract identity information from the current Spring Security context.
 * All values are sourced from RS256 JWT claims — no database calls.
 */
public class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Returns the authenticated user's UUID (from the "userId" JWT claim).
     * The JwtAuthenticationFilter stores this as the Authentication principal.
     */
    public static UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("No authenticated user in security context");
        }
        return (UUID) auth.getPrincipal();
    }

    /**
     * Returns the authenticated user's tenantId (stored as an auth credential string).
     */
    public static String getCurrentTenantId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getCredentials() == null) {
            return null;
        }
        // credentials[0] = tenantId, credentials[1] = status
        if (auth.getCredentials() instanceof String[]) {
            return ((String[]) auth.getCredentials())[0];
        }
        return null;
    }

    /**
     * Returns the authenticated user's status from the JWT (e.g. "ACTIVE", "SUSPENDED").
     */
    public static String getCurrentStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getCredentials() == null) {
            return null;
        }
        if (auth.getCredentials() instanceof String[]) {
            return ((String[]) auth.getCredentials())[1];
        }
        return null;
    }
}
