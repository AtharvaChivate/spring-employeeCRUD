package com.example.demo.Controller;

import jakarta.validation.Valid;

import com.example.demo.Model.AppUser;
import com.example.demo.Model.Employee;
import com.example.demo.Model.Role;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    
    public EmployeeController(EmployeeService employeeService, UserRepository userRepository,
			PasswordEncoder passwordEncoder) {
		this.employeeService = employeeService;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

    @PreAuthorize("hasRole('ADMIN')")
	@GetMapping
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id, @RequestHeader("Authorization") String token){
    	
    	Optional<Employee> employee = employeeService.getEmployeeById(id);
    	
    	// Employee can only view their own details
    	if(employee.isPresent()) {
    		AppUser loggedInuser = userRepository.findByUsername(employee.get().getEmail()).orElse(null);
    		if(loggedInuser != null && loggedInuser.getUsername().equals(employee.get().getEmail())) {
    			return ResponseEntity.ok(employee.get());
    		}
    		return ResponseEntity.status(403).body("Access Denied. You can only view your own profile.");
    	}
    	return ResponseEntity.notFound().build();
    }  
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createEmployee(@Valid @RequestBody Employee employee, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(getValidationErrors(result));
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
        employee.setJoiningDate(LocalDate.now());
        Employee savedEmployee = employeeService.createEmployee(employee);
        
        System.out.println("Employee Created Successfully! Default password is 123");
        
        return ResponseEntity.ok("Employee Created Successfully! Default password is 123");
    }
    
    private Map<String, String> getValidationErrors(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return errors;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id, @Valid @RequestBody Employee employee, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(getValidationErrors(result));
        }
        Employee updatedEmployee = employeeService.updateEmployee(id, employee);
        return ResponseEntity.ok(updatedEmployee);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
    }
}


