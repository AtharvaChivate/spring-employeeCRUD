package com.example.demo.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Model.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
	boolean existsByEmail(String email);
	Optional<Employee> findByAppUserId(Long appUserId);
}
