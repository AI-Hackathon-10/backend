package com.ktbaihackathon.common.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    public final String[] WHITE_LIST = {
            "/user",
    };

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return PatternMatchUtils.simpleMatch(WHITE_LIST, request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
            response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendErrorResponse(response, "AUTHORIZATION_HEADER_MISSING_OR_INVALID");
            return;
        }

        String token = authHeader.substring(7);

        try {
            jwtProvider.parse(token);

            if (!jwtProvider.isAccessToken(token)) {
                sendErrorResponse(response, "INVALID_TOKEN");
                return;
            }

            filterChain.doFilter(request, response);

        } catch (Exception exception) {
            sendErrorResponse(response, "INVALID_TOKEN");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String responseBody = objectMapper.writeValueAsString(errorMessage);
        response.getWriter().write(responseBody);
    }
}