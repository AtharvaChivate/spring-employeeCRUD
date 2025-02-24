package com.example.demo.Controller;

import com.example.demo.Model.AppUser;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private AuthenticationManager authenticationManager;
    private JwtUtil jwtUtil;
	// private final UserRepository userRepository;
	// private final PasswordEncoder passwordEncoder;

	
	public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserRepository userRepository,
			PasswordEncoder passwordEncoder) {
		super();
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		// this.userRepository = userRepository;
		// this.passwordEncoder = passwordEncoder;
	}

	// @PostMapping("/register")
	// public ResponseEntity<?> registerUser(@Valid @RequestBody AppUser user) {
	// 	if (userRepository.findByUsername(user.getUsername()).isPresent()) {
	// 		return ResponseEntity.badRequest().body("Username already exists");
	// 	}
	// 	user.setPassword(passwordEncoder.encode(user.getPassword()));
	// 	userRepository.save(user);
	// 	return ResponseEntity.ok("User registered successfully!");
	// }
	
	@PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AppUser loginRequest) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails.getUsername());

        return ResponseEntity.ok("{\"token\": \"" + token + "\"}");
    }
}
