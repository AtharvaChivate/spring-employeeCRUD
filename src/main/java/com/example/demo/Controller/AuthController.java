package com.example.demo.Controller;

import com.example.demo.Model.AppUser;
import com.example.demo.Model.Employee;
import com.example.demo.Model.Role;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Security.JwtUtil;
import com.example.demo.Service.EmployeeService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;
	private final EmployeeService employeeService;

	public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserRepository userRepository,
			EmployeeService employeeService) {
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.userRepository = userRepository;
		this.employeeService = employeeService;
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody AppUser loginRequest) {
		// Authenticate user
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String token = jwtUtil.generateToken(userDetails.getUsername());

		// Fetch the AppUser entity
		AppUser appUser = userRepository.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found after authentication"));

		// Prepare the response
		Map<String, Object> response = new HashMap<>();
		response.put("token", token);

		// Check role and set appropriate ID
		if (appUser.getRole() == Role.EMPLOYEE) {
			// Fetch Employee by AppUser ID
			Optional<Employee> employeeOpt = employeeService.findByAppUserId(appUser.getId());
			if (employeeOpt.isPresent()) {
				response.put("id", employeeOpt.get().getId().toString());
			} else {
				// Handle case where no Employee is linked (shouldn't happen normally)
				throw new RuntimeException("Employee profile not found for user: " + appUser.getUsername());
			}
		} else {
			// For admins, use AppUser ID 
			response.put("id", appUser.getId().toString());
		}

		return ResponseEntity.ok(response);
	}
}
