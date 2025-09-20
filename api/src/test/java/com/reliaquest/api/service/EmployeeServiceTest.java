package com.reliaquest.api.service;

import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.ExternalApiException;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private EmployeeService employeeService;
    private final String baseUrl = "http://localhost:8112/api/v1/employee";

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(restTemplate, baseUrl);
    }

    @Test
    void getAllEmployees_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(
            Employee.builder().id("1").employeeName("John Doe").employeeSalary(50000).build(),
            Employee.builder().id("2").employeeName("Jane Smith").employeeSalary(60000).build()
        );
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(employees, "Success", null);
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity = 
            new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
            eq(baseUrl),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // Act
        List<Employee> result = employeeService.getAllEmployees();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getEmployeeName());
        assertEquals("Jane Smith", result.get(1).getEmployeeName());
    }

    @Test
    void getAllEmployees_HttpClientErrorException() {
        // Arrange
        when(restTemplate.exchange(
            eq(baseUrl),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // Act & Assert
        assertThrows(ExternalApiException.class, () -> employeeService.getAllEmployees());
    }

    @Test
    void getEmployeesByNameSearch_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(
            Employee.builder().id("1").employeeName("John Doe").employeeSalary(50000).build(),
            Employee.builder().id("2").employeeName("Jane Smith").employeeSalary(60000).build(),
            Employee.builder().id("3").employeeName("John Johnson").employeeSalary(55000).build()
        );
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(employees, "Success", null);
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity = 
            new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
            eq(baseUrl),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // Act
        List<Employee> result = employeeService.getEmployeesByNameSearch("John");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(emp -> emp.getEmployeeName().contains("John")));
    }

    @Test
    void getEmployeeById_Success() {
        // Arrange
        Employee employee = Employee.builder()
            .id("1")
            .employeeName("John Doe")
            .employeeSalary(50000)
            .build();
        ApiResponse<Employee> apiResponse = new ApiResponse<>(employee, "Success", null);
        ResponseEntity<ApiResponse<Employee>> responseEntity = 
            new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
            eq(baseUrl + "/1"),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // Act
        Employee result = employeeService.getEmployeeById("1");

        // Assert
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("John Doe", result.getEmployeeName());
        assertEquals(50000, result.getEmployeeSalary());
    }

    @Test
    void getEmployeeById_NotFound() {
        // Arrange
        when(restTemplate.exchange(
            eq(baseUrl + "/999"),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById("999"));
    }

    @Test
    void getHighestSalaryOfEmployees_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(
            Employee.builder().id("1").employeeName("John Doe").employeeSalary(50000).build(),
            Employee.builder().id("2").employeeName("Jane Smith").employeeSalary(75000).build(),
            Employee.builder().id("3").employeeName("Bob Johnson").employeeSalary(60000).build()
        );
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(employees, "Success", null);
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity = 
            new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
            eq(baseUrl),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // Act
        Integer result = employeeService.getHighestSalaryOfEmployees();

        // Assert
        assertNotNull(result);
        assertEquals(75000, result);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_Success() {
        // Arrange
        List<Employee> employees = Arrays.asList(
            Employee.builder().id("1").employeeName("John Doe").employeeSalary(50000).build(),
            Employee.builder().id("2").employeeName("Jane Smith").employeeSalary(75000).build(),
            Employee.builder().id("3").employeeName("Bob Johnson").employeeSalary(60000).build()
        );
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(employees, "Success", null);
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity = 
            new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
            eq(baseUrl),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // Act
        List<String> result = employeeService.getTopTenHighestEarningEmployeeNames();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Jane Smith", result.get(0)); // Highest salary
        assertEquals("Bob Johnson", result.get(1));
        assertEquals("John Doe", result.get(2));
    }

    @Test
    void createEmployee_Success() {
        // Arrange
        EmployeeInput input = EmployeeInput.builder()
            .name("New Employee")
            .salary(55000)
            .age(30)
            .title("Developer")
            .build();

        Employee createdEmployee = Employee.builder()
            .id("123")
            .employeeName("New Employee")
            .employeeSalary(55000)
            .employeeAge(30)
            .employeeTitle("Developer")
            .employeeEmail("newemployee@company.com")
            .build();

        ApiResponse<Employee> apiResponse = new ApiResponse<>(createdEmployee, "Success", null);
        ResponseEntity<ApiResponse<Employee>> responseEntity = 
            new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
            eq(baseUrl),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // Act
        Employee result = employeeService.createEmployee(input);

        // Assert
        assertNotNull(result);
        assertEquals("123", result.getId());
        assertEquals("New Employee", result.getEmployeeName());
        assertEquals(55000, result.getEmployeeSalary());
    }

    @Test
    void deleteEmployeeById_Success() {
        // Arrange
        String employeeId = "123";
        Employee employee = Employee.builder()
            .id(employeeId)
            .employeeName("John Doe")
            .employeeSalary(50000)
            .build();

        // Mock getting employee by ID
        ApiResponse<Employee> getApiResponse = new ApiResponse<>(employee, "Success", null);
        ResponseEntity<ApiResponse<Employee>> getResponseEntity = 
            new ResponseEntity<>(getApiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
            eq(baseUrl + "/" + employeeId),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenReturn(getResponseEntity);

        // Mock delete operation
        ApiResponse<Boolean> deleteApiResponse = new ApiResponse<>(true, "Success", null);
        ResponseEntity<ApiResponse<Boolean>> deleteResponseEntity = 
            new ResponseEntity<>(deleteApiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
            eq(baseUrl),
            eq(HttpMethod.DELETE),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
        )).thenReturn(deleteResponseEntity);

        // Act
        String result = employeeService.deleteEmployeeById(employeeId);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result);
    }

    @Test
    void deleteEmployeeById_EmployeeNotFound() {
        // Arrange
        String employeeId = "999";

        when(restTemplate.exchange(
            eq(baseUrl + "/" + employeeId),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.deleteEmployeeById(employeeId));
    }
}