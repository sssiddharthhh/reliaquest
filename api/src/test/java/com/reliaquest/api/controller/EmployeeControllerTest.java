package com.reliaquest.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.ExternalApiException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllEmployees_Success() throws Exception {
        // Arrange
        List<Employee> employees = Arrays.asList(
            Employee.builder().id("1").employeeName("John Doe").employeeSalary(50000).build(),
            Employee.builder().id("2").employeeName("Jane Smith").employeeSalary(60000).build()
        );
        when(employeeService.getAllEmployees()).thenReturn(employees);

        // Act & Assert
        mockMvc.perform(get("/employees"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value("1"))
            .andExpect(jsonPath("$[0].employee_name").value("John Doe"))
            .andExpected(jsonPath("$[1].id").value("2"))
            .andExpect(jsonPath("$[1].employee_name").value("Jane Smith"));
    }

    @Test
    void getAllEmployees_ExternalApiException() throws Exception {
        // Arrange
        when(employeeService.getAllEmployees()).thenThrow(new ExternalApiException("External API error"));

        // Act & Assert
        mockMvc.perform(get("/employees"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.error").value("External Service Error"));
    }

    @Test
    void getEmployeesByNameSearch_Success() throws Exception {
        // Arrange
        List<Employee> employees = Arrays.asList(
            Employee.builder().id("1").employeeName("John Doe").employeeSalary(50000).build()
        );
        when(employeeService.getEmployeesByNameSearch("John")).thenReturn(employees);

        // Act & Assert
        mockMvc.perform(get("/employees/search/John"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].employee_name").value("John Doe"));
    }

    @Test
    void getEmployeeById_Success() throws Exception {
        // Arrange
        Employee employee = Employee.builder()
            .id("1")
            .employeeName("John Doe")
            .employeeSalary(50000)
            .build();
        when(employeeService.getEmployeeById("1")).thenReturn(employee);

        // Act & Assert
        mockMvc.perform(get("/employees/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value("1"))
            .andExpect(jsonPath("$.employee_name").value("John Doe"));
    }

    @Test
    void getEmployeeById_NotFound() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById("999")).thenThrow(new EmployeeNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/employees/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Employee Not Found"));
    }

    @Test
    void getHighestSalaryOfEmployees_Success() throws Exception {
        // Arrange
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(75000);

        // Act & Assert
        mockMvc.perform(get("/employees/highestSalary"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string("75000"));
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_Success() throws Exception {
        // Arrange
        List<String> names = Arrays.asList("Jane Smith", "John Doe", "Bob Johnson");
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(names);

        // Act & Assert
        mockMvc.perform(get("/employees/topTenHighestEarningEmployeeNames"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0]").value("Jane Smith"))
            .andExpect(jsonPath("$[1]").value("John Doe"));
    }

    @Test
    void createEmployee_Success() throws Exception {
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
            .build();

        when(employeeService.createEmployee(any(EmployeeInput.class))).thenReturn(createdEmployee);

        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value("123"))
            .andExpect(jsonPath("$.employee_name").value("New Employee"));
    }

    @Test
    void createEmployee_ValidationError() throws Exception {
        // Arrange
        EmployeeInput input = EmployeeInput.builder()
            .name("") // Invalid - blank name
            .salary(-1000) // Invalid - negative salary
            .age(10) // Invalid - age too low
            .title("") // Invalid - blank title
            .build();

        // Act & Assert
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    void deleteEmployeeById_Success() throws Exception {
        // Arrange
        when(employeeService.deleteEmployeeById("1")).thenReturn("John Doe");

        // Act & Assert
        mockMvc.perform(delete("/employees/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string("\"John Doe\""));
    }

    @Test
    void deleteEmployeeById_NotFound() throws Exception {
        // Arrange
        when(employeeService.deleteEmployeeById("999")).thenThrow(new EmployeeNotFoundException("Employee not found"));

        // Act & Assert
        mockMvc.perform(delete("/employees/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Employee Not Found"));
    }
}