package com.example.demo.Controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import com.example.demo.Model.AppUser;
import com.example.demo.Model.Employee;
import com.example.demo.Model.Role;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Security.JwtUtil;
import com.example.demo.Service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

//    @Autowired
	private EmployeeService employeeService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;

	public EmployeeController(EmployeeService employeeService, UserRepository userRepository,
			PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
		this.employeeService = employeeService;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtil = jwtUtil;
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping
	public List<Employee> getAllEmployees() {
		return employeeService.getAllEmployees();
	}

	@PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
	@GetMapping("/{id}")
	public ResponseEntity<?> getEmployeeById(@PathVariable Long id, @RequestHeader("Authorization") String token) {

		Optional<Employee> employeeOpt = employeeService.getEmployeeById(id);

		if (!employeeOpt.isPresent()) {
			throw new EntityNotFoundException("Employee with ID " + id + " not found");
		}
		
		Employee employee = employeeOpt.get();
		AppUser employeeAppUser = employee.getAppUser();
		
		if(employeeAppUser == null) {
			throw new IllegalStateException("No associated user found for this employee");
		}
		
		String jwt = token.substring(7);
		String loggedInUsername = jwtUtil.extractUsername(jwt);
		
		Optional<AppUser> loggedInUserOpt = userRepository.findByUsername(loggedInUsername);
		if(!loggedInUserOpt.isPresent() || !loggedInUserOpt.get().getId().equals(employeeAppUser.getId())) {
			throw new AccessDeniedException("Access Denied. You can only view your own profile.");
		}
		return ResponseEntity.ok(employee);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping
	public ResponseEntity<?> createEmployee(@Valid @RequestBody Employee employee, BindingResult result) {
		if (result.hasErrors()) {
			Map<String, String> errors = new HashMap<>();
			result.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

			return ResponseEntity.badRequest().body(errors);
		}

		if (employeeService.existsByEmail(employee.getEmail())) {
			Map<String, String> errors = new HashMap<>();
			errors.put("email", "Email already exists");
			return ResponseEntity.badRequest().body(errors);
		}

		// Creating new user for Authentication
		AppUser appUser = new AppUser();
		appUser.setUsername(employee.getEmail());
		appUser.setPassword(passwordEncoder.encode("123"));

		// Assign Employee Role using ENUM
		appUser.setRole(Role.EMPLOYEE);

		// Save the user
		userRepository.save(appUser);

		// Linking the user to employee
		employee.setAppUser(appUser);
		if (employee.getJoiningDate() == null) {
			employee.setJoiningDate(LocalDate.now());
		}
		Employee savedEmployee = employeeService.createEmployee(employee);

		System.out.println("Employee Created Successfully! Default password is 123");
		return ResponseEntity.ok(savedEmployee);
	}

	private Map<String, String> getValidationErrors(BindingResult result) {
		Map<String, String> errors = new HashMap<>();
		result.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
		return errors;
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/{id}")
	public ResponseEntity<?> updateEmployee(@PathVariable Long id, @Valid @RequestBody Employee employee,
			BindingResult result) {
		if (result.hasErrors()) {
			return ResponseEntity.badRequest().body(getValidationErrors(result));
		}
		Employee updatedEmployee = employeeService.updateEmployee(id, employee);
		return ResponseEntity.ok(updatedEmployee);
	}

	@PreAuthorize("hasRole('EMPLOYEE')")
	@Transactional
	@PutMapping("/{id}/update-credentials")
	public ResponseEntity<?> updateCredentials(@PathVariable Long id, @RequestBody Map<String, String> credentials,
			@RequestHeader("Authorization") String token) {
		Optional<Employee> employeeOpt = employeeService.getEmployeeById(id);
		if (!employeeOpt.isPresent()) {
			throw new EntityNotFoundException("Employee with ID " + id + " not found");
		}

		Employee employee = employeeOpt.get();
		AppUser appUser = employee.getAppUser();
		if (appUser == null) {
			throw new IllegalStateException("No associated user found for this employee");
		}

		// Check if the logged-in user owns this profile
		String currentUsername = appUser.getUsername();
		Optional<AppUser> loggedInUserOpt = userRepository.findByUsername(currentUsername);
		if (!loggedInUserOpt.isPresent() || !appUser.getId().equals(loggedInUserOpt.get().getId())) {
			throw new AccessDeniedException("You can only update your own credentials");
		}

		Map<String, String> errors = new HashMap<>();
		String newUsername = credentials.get("username");
		String newPassword = credentials.get("password");

		// Validate and update username
		if (newUsername != null && !newUsername.isEmpty() && !newUsername.equals(currentUsername)) {
			Optional<AppUser> existingUser = userRepository.findByUsername(newUsername);
			if (existingUser.isPresent() && !existingUser.get().getId().equals(appUser.getId())) {
				errors.put("username", "Username already exists");
			} else {
				appUser.setUsername(newUsername);
				// Do NOT automatically update employee.email unless explicitly requested
				// employee.setEmail(newUsername); // Removed this line
			}
		}

		// Validate and update password
		if (newPassword != null && !newPassword.isEmpty()) {
			if (newPassword.length() < 6) {
				errors.put("password", "Password must be at least 6 characters");
			} else {
				appUser.setPassword(passwordEncoder.encode(newPassword));
			}
		}

		if (!errors.isEmpty()) {
			return ResponseEntity.badRequest().body(errors);
		}

		try {
			userRepository.save(appUser);
			// Only update Employee if necessary (e.g., if other fields changed)
			// employeeService.updateEmployee(id, employee); // Comment out unless needed
			return ResponseEntity.ok("Credentials updated successfully");
		} catch (Exception e) {
			e.printStackTrace(); // Log the full stack trace
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", "Failed to update credentials: " + e.getMessage());
			return ResponseEntity.status(500).body(errorResponse);
		}
	}

	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{id}")
	public void deleteEmployee(@PathVariable Long id) {
		employeeService.deleteEmployee(id);
	}
}