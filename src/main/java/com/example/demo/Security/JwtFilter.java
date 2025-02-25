package com.example.demo.Security;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        if (path.equals("/api/auth/register") || path.equals("/api/auth/login") 
                || path.startsWith("/h2-console")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                    "Authorization header is missing or has invalid format");
            return;
        }

        String jwt = authorizationHeader.substring(7);

        try {
            if (!jwtUtil.validateToken(jwt)) {
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Invalid JWT token");
                return;
            }
            
            String username = jwtUtil.extractUsername(jwt);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                filterChain.doFilter(request, response);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, 
                        "Could not set user authentication in security context");
            }
        } catch (JwtException e) {
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, 
                    "Authentication error: " + e.getMessage());
        }
    }
    
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", status);
        errorDetails.put("error", status == 401 ? "Unauthorized" : "Forbidden");
        errorDetails.put("message", message);
        errorDetails.put("timestamp", System.currentTimeMillis());
        errorDetails.put("path", "authentication");
        
        String jsonResponse = objectMapper.writeValueAsString(errorDetails);
        response.getWriter().write(jsonResponse);
    }
}
