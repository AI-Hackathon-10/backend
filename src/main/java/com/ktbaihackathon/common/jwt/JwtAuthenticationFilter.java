package com.ktbaihackathon.common.jwt;


import com.ktbaihackathon.common.response.ApiResponse;
import com.ktbaihackathon.common.response.ResultCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.Set;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    /**
     * 경로가 같아도 HTTP 메서드에 따라 인증 요구 여부가 갈리는 엔드포인트가 있어(예: "/api/auth"의
     * POST=로그인/PUT=재발급은 인증 불필요, DELETE=로그아웃은 인증 필요) 경로만으로는 화이트리스트를 판단할 수 없다.
     */
    private static final Map<String, Set<String>> WHITE_LIST = Map.of(
            "/api/user", Set.of(HttpMethod.POST.name()),
            "/api/auth", Set.of(HttpMethod.POST.name(), HttpMethod.PUT.name())
    );

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        Set<String> allowedMethods = WHITE_LIST.get(request.getRequestURI());
        return allowedMethods != null && allowedMethods.contains(request.getMethod());
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
            sendErrorResponse(response, ResultCode.UNAUTHORIZED);
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (!jwtProvider.isAccessToken(token)) {
                sendErrorResponse(response, ResultCode.INVALID_TOKEN);
                return;
            }

            Long userId = jwtProvider.getUserId(token);
            request.setAttribute("userId", userId);

            filterChain.doFilter(request, response);

        } catch (Exception exception) {
            sendErrorResponse(response, ResultCode.INVALID_TOKEN);
        }
    }

    private void sendErrorResponse(HttpServletResponse response, ResultCode resultCode) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Void> body = ApiResponse.fail(resultCode.name(), resultCode.getMessage());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}