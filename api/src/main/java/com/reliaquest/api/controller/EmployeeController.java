package com.reliaquest.api.controller;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController implements IEmployeeController<Employee, EmployeeInput> {

    private final EmployeeService employeeService;

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        log.info("Request received to get all employees");
        List<Employee> employees = employeeService.getAllEmployees();
        log.info("Returning {} employees", employees.size());
        return ResponseEntity.ok(employees);
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        log.info("Request received to search employees by name: {}", searchString);
        List<Employee> employees = employeeService.getEmployeesByNameSearch(searchString);
        log.info("Returning {} employees matching search criteria", employees.size());
        return ResponseEntity.ok(employees);
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(String id) {
        log.info("Request received to get employee by ID: {}", id);
        Employee employee = employeeService.getEmployeeById(id);
        log.info("Returning employee with ID: {}", id);
        return ResponseEntity.ok(employee);
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        log.info("Request received to get highest salary of employees");
        Integer highestSalary = employeeService.getHighestSalaryOfEmployees();
        log.info("Returning highest salary: {}", highestSalary);
        return ResponseEntity.ok(highestSalary);
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        log.info("Request received to get top 10 highest earning employee names");
        List<String> employeeNames = employeeService.getTopTenHighestEarningEmployeeNames();
        log.info("Returning {} top earning employee names", employeeNames.size());
        return ResponseEntity.ok(employeeNames);
    }

    @Override
    public ResponseEntity<Employee> createEmployee(@Valid EmployeeInput employeeInput) {
        log.info("Request received to create employee: {}", employeeInput.getName());
        Employee createdEmployee = employeeService.createEmployee(employeeInput);
        log.info("Successfully created employee with ID: {}", createdEmployee.getId());
        return ResponseEntity.ok(createdEmployee);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        log.info("Request received to delete employee by ID: {}", id);
        String deletedEmployeeName = employeeService.deleteEmployeeById(id);
        log.info("Successfully deleted employee: {}", deletedEmployeeName);
        return ResponseEntity.ok(deletedEmployeeName);
    }
}