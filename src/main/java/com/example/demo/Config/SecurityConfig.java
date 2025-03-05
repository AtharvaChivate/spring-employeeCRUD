package com.example.demo.Config;

import com.example.demo.Security.JwtFilter;
import com.example.demo.Security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

	@Autowired
	private final JwtUtil jwtUtil;

	@Autowired
	private final UserDetailsService userDetailsService;

	public SecurityConfig(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
		super();
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()).cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.authorizeHttpRequests(
						authz -> authz.requestMatchers("/api/auth/register", "/api/auth/login", "/h2-console/**")
								.permitAll().requestMatchers(HttpMethod.GET, "/api/employees").hasRole("ADMIN")
								.requestMatchers(HttpMethod.GET, "/api/employees/{id}").hasAnyRole("ADMIN", "EMPLOYEE")
								.requestMatchers(HttpMethod.POST, "/api/employees").hasRole("ADMIN")
								.requestMatchers(HttpMethod.PUT, "/api/employees/{id}").hasAnyRole("ADMIN", "EMPLOYEE")
								.requestMatchers(HttpMethod.PUT, "/api/employees/{id}/update-credentials")
								.hasRole("EMPLOYEE").requestMatchers(HttpMethod.DELETE, "/api/employees/{id}")
								.hasRole("ADMIN").anyRequest().authenticated())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.headers(headers ->
                headers
                .contentSecurityPolicy(csp ->
                    csp.policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; frame-src 'self';")
                )
                .addHeaderWriter((request, response) -> {
                    response.setHeader("X-Frame-Options", "SAMEORIGIN");
                })
        )
				.exceptionHandling(
						exceptions -> exceptions.authenticationEntryPoint((request, response, authException) -> {
							response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
							response.setContentType(MediaType.APPLICATION_JSON_VALUE);

							Map<String, Object> errorDetails = new HashMap<>();
							errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);
							errorDetails.put("error", "Unauthorized");
							errorDetails.put("message", "Authentication required: " + authException.getMessage());
							errorDetails.put("timestamp", System.currentTimeMillis());
							errorDetails.put("path", request.getRequestURI());

							new ObjectMapper().writeValue(response.getOutputStream(), errorDetails);
						}).accessDeniedHandler((request, response, accessDeniedException) -> {
							response.setStatus(HttpServletResponse.SC_FORBIDDEN);
							response.setContentType(MediaType.APPLICATION_JSON_VALUE);

							Map<String, Object> errorDetails = new HashMap<>();
							errorDetails.put("status", HttpServletResponse.SC_FORBIDDEN);
							errorDetails.put("error", "Forbidden");
							errorDetails.put("message", "Access denied: " + accessDeniedException.getMessage());
							errorDetails.put("timestamp", System.currentTimeMillis());
							errorDetails.put("path", request.getRequestURI());

							new ObjectMapper().writeValue(response.getOutputStream(), errorDetails);
						}))
				.addFilterBefore(new JwtFilter(jwtUtil, userDetailsService),
						UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of("http://localhost:3000"));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
		config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}
