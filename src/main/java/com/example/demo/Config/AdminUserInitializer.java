package com.example.demo.Config;

import com.example.demo.Model.AppUser;
import com.example.demo.Model.Role;
import com.example.demo.Repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if the admin user already exists to prevent duplicate admin creation
        if (!userRepository.findByUsername("admin").isPresent()) {
            // Create the default admin user
            AppUser adminUser = new AppUser();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("123"));  // Set a default password
            adminUser.setRole(Role.ADMIN);

            // Save the admin user to the database
            userRepository.save(adminUser);

            System.out.println("Default admin user created with username 'admin' and password '123'");
        }
    }
}
