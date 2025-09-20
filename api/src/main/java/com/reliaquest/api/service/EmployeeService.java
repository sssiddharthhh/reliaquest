package com.reliaquest.api.service;

import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.ExternalApiException;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.DeleteEmployeeRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmployeeService {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public EmployeeService(RestTemplate restTemplate, 
                          @Value("${employee.api.base-url:http://localhost:8112/api/v1/employee}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Retryable(
        retryFor = {ResourceAccessException.class, ExternalApiException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public List<Employee> getAllEmployees() {
        log.info("Fetching all employees");
        try {
            ResponseEntity<ApiResponse<List<Employee>>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<List<Employee>>>() {}
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Successfully fetched {} employees", response.getBody().getData().size());
                return response.getBody().getData();
            }
            throw new ExternalApiException("Failed to fetch employees");
        } catch (HttpClientErrorException e) {
            log.error("HTTP error while fetching employees: {}", e.getMessage());
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new ExternalApiException("Rate limit exceeded", e);
            }
            throw new ExternalApiException("Failed to fetch employees", e);
        } catch (Exception e) {
            log.error("Unexpected error while fetching employees", e);
            throw new ExternalApiException("Failed to fetch employees", e);
        }
    }

    public List<Employee> getEmployeesByNameSearch(String searchString) {
        log.info("Searching employees by name: {}", searchString);
        List<Employee> allEmployees = getAllEmployees();
        
        List<Employee> matchingEmployees = allEmployees.stream()
            .filter(employee -> employee.getEmployeeName() != null && 
                              employee.getEmployeeName().toLowerCase().contains(searchString.toLowerCase()))
            .collect(Collectors.toList());
            
        log.info("Found {} employees matching search: {}", matchingEmployees.size(), searchString);
        return matchingEmployees;
    }

    @Retryable(
        retryFor = {ResourceAccessException.class, ExternalApiException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public Employee getEmployeeById(String id) {
        log.info("Fetching employee by ID: {}", id);
        try {
            ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                baseUrl + "/" + id,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<Employee>>() {}
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Successfully fetched employee with ID: {}", id);
                return response.getBody().getData();
            }
            throw new EmployeeNotFoundException("Employee not found with ID: " + id);
        } catch (HttpClientErrorException e) {
            log.error("HTTP error while fetching employee {}: {}", id, e.getMessage());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new EmployeeNotFoundException("Employee not found with ID: " + id);
            }
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new ExternalApiException("Rate limit exceeded", e);
            }
            throw new ExternalApiException("Failed to fetch employee", e);
        } catch (Exception e) {
            log.error("Unexpected error while fetching employee {}", id, e);
            throw new ExternalApiException("Failed to fetch employee", e);
        }
    }

    public Integer getHighestSalaryOfEmployees() {
        log.info("Finding highest salary among all employees");
        List<Employee> employees = getAllEmployees();
        
        Integer highestSalary = employees.stream()
            .filter(employee -> employee.getEmployeeSalary() != null)
            .mapToInt(Employee::getEmployeeSalary)
            .max()
            .orElse(0);
            
        log.info("Highest salary found: {}", highestSalary);
        return highestSalary;
    }

    public List<String> getTopTenHighestEarningEmployeeNames() {
        log.info("Finding top 10 highest earning employees");
        List<Employee> employees = getAllEmployees();
        
        List<String> topEarners = employees.stream()
            .filter(employee -> employee.getEmployeeSalary() != null && employee.getEmployeeName() != null)
            .sorted(Comparator.comparing(Employee::getEmployeeSalary).reversed())
            .limit(10)
            .map(Employee::getEmployeeName)
            .collect(Collectors.toList());
            
        log.info("Found {} top earning employees", topEarners.size());
        return topEarners;
    }

    @Retryable(
        retryFor = {ResourceAccessException.class, ExternalApiException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public Employee createEmployee(EmployeeInput employeeInput) {
        log.info("Creating new employee: {}", employeeInput.getName());
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<EmployeeInput> request = new HttpEntity<>(employeeInput, headers);
            
            ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<ApiResponse<Employee>>() {}
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Employee createdEmployee = response.getBody().getData();
                log.info("Successfully created employee with ID: {}", createdEmployee.getId());
                return createdEmployee;
            }
            throw new ExternalApiException("Failed to create employee");
        } catch (HttpClientErrorException e) {
            log.error("HTTP error while creating employee: {}", e.getMessage());
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new ExternalApiException("Rate limit exceeded", e);
            }
            throw new ExternalApiException("Failed to create employee", e);
        } catch (Exception e) {
            log.error("Unexpected error while creating employee", e);
            throw new ExternalApiException("Failed to create employee", e);
        }
    }

    @Retryable(
        retryFor = {ResourceAccessException.class, ExternalApiException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public String deleteEmployeeById(String id) {
        log.info("Deleting employee by ID: {}", id);
        
        // First get the employee to find their name
        Employee employee = getEmployeeById(id);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            DeleteEmployeeRequest deleteRequest = new DeleteEmployeeRequest(employee.getEmployeeName());
            HttpEntity<DeleteEmployeeRequest> request = new HttpEntity<>(deleteRequest, headers);
            
            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.DELETE,
                request,
                new ParameterizedTypeReference<ApiResponse<Boolean>>() {}
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && 
                Boolean.TRUE.equals(response.getBody().getData())) {
                log.info("Successfully deleted employee: {}", employee.getEmployeeName());
                return employee.getEmployeeName();
            }
            throw new ExternalApiException("Failed to delete employee");
        } catch (HttpClientErrorException e) {
            log.error("HTTP error while deleting employee {}: {}", id, e.getMessage());
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new ExternalApiException("Rate limit exceeded", e);
            }
            throw new ExternalApiException("Failed to delete employee", e);
        } catch (Exception e) {
            log.error("Unexpected error while deleting employee {}", id, e);
            throw new ExternalApiException("Failed to delete employee", e);
        }
    }
}