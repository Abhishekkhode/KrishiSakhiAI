package com.KrishiSakhi.AI.Security.jwt;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // This is triggered when an unauthenticated user tries to access a secured resource.
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized. Requires valid access token.");
    }
}