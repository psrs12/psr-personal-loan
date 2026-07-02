package com.personalloan.offeracceptance.infrastructure.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates the applicant session JWT (issued by application-management-service) and enforces
 * that the token's subject (the applicationId it was issued for) matches the applicationId in
 * the request path. applicationId is a public, guessable value, so path-matching alone is not
 * authorization — the token is what proves the caller owns that application.
 */
@Component
public class BearerTokenFilter extends OncePerRequestFilter {

    private static final Pattern APPLICATION_ID_IN_PATH =
            Pattern.compile("/applications/([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})");

    private final SecretKey jwtSecret;

    public BearerTokenFilter(@Value("${jwt.secret}") String jwtSecret) {
        this.jwtSecret = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        boolean isApiDocs = path.contains("/swagger-ui") || path.contains("/v3/api-docs");
        if (path.contains("/actuator") || isApiDocs) {
            filterChain.doFilter(request, response);
            return;
        }

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ") || auth.length() <= 7) {
            reject(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing or invalid Authorization header");
            return;
        }

        UUID tokenApplicationId;
        try {
            String subject = Jwts.parser()
                    .verifyWith(jwtSecret)
                    .build()
                    .parseSignedClaims(auth.substring(7))
                    .getPayload()
                    .getSubject();
            tokenApplicationId = UUID.fromString(subject);
        } catch (JwtException | IllegalArgumentException e) {
            reject(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Invalid or expired session token");
            return;
        }

        Matcher matcher = APPLICATION_ID_IN_PATH.matcher(path);
        if (matcher.find()) {
            UUID pathApplicationId = UUID.fromString(matcher.group(1));
            if (!pathApplicationId.equals(tokenApplicationId)) {
                reject(response, HttpStatus.FORBIDDEN, "FORBIDDEN", "Session token does not grant access to this application");
                return;
            }
        }

        request.setAttribute("authenticatedApplicationId", tokenApplicationId);
        filterChain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, HttpStatus status, String errorCode, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"errorCode\":\"" + errorCode + "\",\"message\":\"" + message + "\"}");
    }
}
