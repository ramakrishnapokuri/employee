package org.example.service;

import org.example.model.Employee;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the employee CSV. Expects: Id,firstName,lastName,salary,managerId
 * First row is header (skipped), empty managerId = CEO.
 */
public class CsvEmployeeReader {
    
    private static final String DELIMITER = ",";
    private static final int EXPECTED_COLUMNS = 5;
    
    /** Parse the CSV and return employees. Throws on bad data. */
    public List<Employee> readEmployees(Path filePath) throws IOException {
        List<Employee> employees = new ArrayList<>();
        
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            // Skip header line
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("Empty CSV file");
            }
            
            String line;
            int lineNumber = 1; // Header is line 1
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    Employee employee = parseLine(line);
                    employees.add(employee);
                } catch (IllegalArgumentException e) {
                    throw new IOException(
                            String.format("Error parsing line %d: %s - %s", lineNumber, line, e.getMessage()));
                }
            }
        }
        
        if (employees.isEmpty()) {
            throw new IOException("No employee data found in file");
        }
        
        return employees;
    }
    
    /** Parse one line into an Employee. */
    private Employee parseLine(String line) {
        String[] parts = line.split(DELIMITER, -1); // -1 to keep trailing empty strings
        
        if (parts.length != EXPECTED_COLUMNS) {
            throw new IllegalArgumentException(
                    String.format("Expected %d columns but found %d", EXPECTED_COLUMNS, parts.length));
        }
        
        try {
            int id = Integer.parseInt(parts[0].trim());
            String firstName = parts[1].trim();
            String lastName = parts[2].trim();
            double salary = Double.parseDouble(parts[3].trim());
            
            // Manager ID is optional (empty for CEO)
            Integer managerId = null;
            String managerIdStr = parts[4].trim();
            if (!managerIdStr.isEmpty()) {
                managerId = Integer.parseInt(managerIdStr);
            }
            
            validateEmployeeData(id, firstName, lastName, salary);
            
            return new Employee(id, firstName, lastName, salary, managerId);
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format: " + e.getMessage());
        }
    }
    
    /** Basic sanity checks on the parsed values. */
    private void validateEmployeeData(int id, String firstName, String lastName, double salary) {
        if (id <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        if (firstName.isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        if (lastName.isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }
        if (salary < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }
    }
}

