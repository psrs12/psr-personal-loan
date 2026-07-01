package com.personalloan.applicationmanagement.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class BearerTokenFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        boolean isPost = "POST".equalsIgnoreCase(request.getMethod());
        boolean isApplicationIntake = isPost && path.matches(".*/applications/?$");
        if (path.contains("/actuator") || path.endsWith("/login") || path.contains("/invitations") || isApplicationIntake) {
            filterChain.doFilter(request, response);
            return;
        }
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ") || auth.length() <= 7) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("{\"errorCode\":\"UNAUTHORIZED\",\"message\":\"Missing or invalid Authorization header\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
