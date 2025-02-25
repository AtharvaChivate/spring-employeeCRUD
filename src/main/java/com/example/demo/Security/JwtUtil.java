package com.example.demo.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.DecodingException;
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
	
	// Validate the JWT Token with specific error messages
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            
            System.out.println("Token is validated Successfully");
            return true;
        } catch(ExpiredJwtException e) {
            System.out.println("Token has expired: " + e.getMessage());
            throw new JwtException("Token has expired");
        } catch(SecurityException e) {
            System.out.println("Invalid JWT signature: " + e.getMessage());
            throw new JwtException("Invalid JWT signature");
        } catch(MalformedJwtException e) {
            System.out.println("Invalid JWT token: " + e.getMessage());
            throw new JwtException("Invalid JWT token format");
        } catch(DecodingException e) {
            System.out.println("JWT decoding failed: " + e.getMessage());
            throw new JwtException("JWT decoding failed");
        } catch(JwtException e) {
            System.out.println("Error in token validation: " + e.getMessage());
            throw new JwtException("JWT validation error: " + e.getMessage());
        } catch(Exception e) {
            System.out.println("Unexpected error in token validation: " + e.getMessage());
            throw new JwtException("Unexpected error in token validation");
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
        } catch(ExpiredJwtException e) {
            // Special case: if token is expired but we still want to extract the username
            System.out.println("Token is expired but extracting username anyway");
            return e.getClaims().getSubject();
        } catch(JwtException e) {
            System.out.println("Token is invalid: " + e.getMessage());
            throw new JwtException("Cannot extract username: " + e.getMessage());
        }
    }
}
