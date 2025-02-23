package com.example.demo.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;
import javax.crypto.SecretKey;

@Component
public class JwtUtil {
//    private final String SECRET_KEY = "your_secret_key";
	
	@Value("${jwt_secret}")
	private String secret;

    // Convert the secret string to a SecretKey for HMAC - SHA256
	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(secret.getBytes());
	}
	
	// Generate the JWT token
	public String generateToken(String username) {
		long expirationTime = 1000*60*60; // 1 hr in milli-seconds
		
		return Jwts.builder()
				.subject(username)
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + expirationTime))
				.signWith(getSigningKey(), Jwts.SIG.HS256)
				.compact();
	}
	
	// Validate the JWT Token
	public boolean validateToken(String token) {
		try {
			Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token);
			
			System.out.println("Token is validated Successfully");
			return true;
		}catch(JwtException e) {
			System.out.println("Error in token validation: " + e.getMessage());
			return false;
		}
	}
	
	// Extract username from the token
	public String extractUsername(String token) {
		try {
			Claims claims = Jwts.parser()
							.verifyWith(getSigningKey())
							.build()
							.parseSignedClaims(token)
							.getPayload();
			
			System.out.println("Username extracted from the token");
			return claims.getSubject();
		}catch(JwtException e) {
			System.out.println("Token is invalid: " + e.getMessage());
			return null;
		}
	}
}
