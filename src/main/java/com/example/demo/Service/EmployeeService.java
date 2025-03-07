package com.example.demo.Service;

import com.example.demo.Model.Employee;
import com.example.demo.Repository.EmployeeRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    public Employee createEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Long id, Employee updatedEmployee) {
        Optional<Employee> employeeOpt = employeeRepository.findById(id);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            employee.setFirstName(updatedEmployee.getFirstName());
            employee.setLastName(updatedEmployee.getLastName());
            employee.setEmail(updatedEmployee.getEmail());
            employee.setSalary(updatedEmployee.getSalary());
            employee.setDepartment(updatedEmployee.getDepartment());
            employee.setJoiningDate(updatedEmployee.getJoiningDate());
            return employeeRepository.save(employee);
        }
        throw new EntityNotFoundException("Employee with ID " + id + " not found");
    }

    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
    
    public boolean existsByEmail(String email) {
        return employeeRepository.existsByEmail(email);
    }
    
    public Optional<Employee> findByAppUserId(Long appUserId) {
        return employeeRepository.findByAppUserId(appUserId);
    }
}

