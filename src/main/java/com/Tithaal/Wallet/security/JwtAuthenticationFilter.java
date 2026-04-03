package com.Tithaal.Wallet.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter — RS256 edition.
 *
 * Validates every incoming Bearer token against the Auth Service's RSA public key.
 * Extracts identity (userId, tenantId, roles, status) directly from JWT claims.
 * NO database call is made per request.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final RsaJwtValidator rsaJwtValidator;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractBearerToken(request);

        if (StringUtils.hasText(token)) {
            try {
                Claims claims = rsaJwtValidator.validateAndGetClaims(token);

                // Extract identity from token claims — single source of truth
                UUID userId     = UUID.fromString(claims.get("userId", String.class));
                String tenantId = claims.get("tenantId", String.class);
                String status   = claims.get("status", String.class);
                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);

                // Build granted authorities from roles
                List<SimpleGrantedAuthority> authorities = roles == null ? List.of() :
                        roles.stream()
                             .map(SimpleGrantedAuthority::new)
                             .collect(Collectors.toList());

                // Store tenantId + status as credentials array for SecurityUtils
                String[] credentials = { tenantId, status };

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, credentials, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (JwtException ex) {
                log.warn("Blocked request [{}] — invalid/expired JWT: {}", request.getRequestURI(), ex.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
