package org.example.service;

import org.example.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the CsvEmployeeReader service.
 */
@DisplayName("CSV Employee Reader Tests")
class CsvEmployeeReaderTest {
    
    private CsvEmployeeReader reader;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        reader = new CsvEmployeeReader();
    }
    
    @Test
    @DisplayName("Should read valid CSV file with multiple employees")
    void shouldReadValidCsvFile() throws IOException {
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                123,Joe,Doe,60000,
                124,Martin,Chekov,45000,123
                125,Bob,Ronstad,47000,123
                """;
        
        Path csvFile = createTempCsvFile(csvContent);
        List<Employee> employees = reader.readEmployees(csvFile);
        
        assertEquals(3, employees.size());
        
        // Verify CEO
        Employee ceo = employees.get(0);
        assertEquals(123, ceo.getId());
        assertEquals("Joe", ceo.getFirstName());
        assertEquals("Doe", ceo.getLastName());
        assertEquals(60000, ceo.getSalary());
        assertNull(ceo.getManagerId());
        
        // Verify employee with manager
        Employee emp = employees.get(1);
        assertEquals(124, emp.getId());
        assertEquals(123, emp.getManagerId());
    }
    
    @Test
    @DisplayName("Should skip empty lines in CSV")
    void shouldSkipEmptyLines() throws IOException {
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                123,Joe,Doe,60000,
                
                124,Martin,Chekov,45000,123
                """;
        
        Path csvFile = createTempCsvFile(csvContent);
        List<Employee> employees = reader.readEmployees(csvFile);
        
        assertEquals(2, employees.size());
    }
    
    @Test
    @DisplayName("Should throw exception for empty file")
    void shouldThrowExceptionForEmptyFile() throws IOException {
        Path csvFile = createTempCsvFile("");
        
        assertThrows(IOException.class, () -> reader.readEmployees(csvFile));
    }
    
    @Test
    @DisplayName("Should throw exception for header only file")
    void shouldThrowExceptionForHeaderOnlyFile() throws IOException {
        String csvContent = "Id,firstName,lastName,salary,managerId\n";
        
        Path csvFile = createTempCsvFile(csvContent);
        
        assertThrows(IOException.class, () -> reader.readEmployees(csvFile));
    }
    
    @Test
    @DisplayName("Should throw exception for invalid column count")
    void shouldThrowExceptionForInvalidColumnCount() throws IOException {
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                123,Joe,Doe,60000
                """;
        
        Path csvFile = createTempCsvFile(csvContent);
        
        IOException exception = assertThrows(IOException.class, () -> reader.readEmployees(csvFile));
        assertTrue(exception.getMessage().contains("Expected 5 columns"));
    }
    
    @Test
    @DisplayName("Should throw exception for invalid salary format")
    void shouldThrowExceptionForInvalidSalaryFormat() throws IOException {
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                123,Joe,Doe,invalid,
                """;
        
        Path csvFile = createTempCsvFile(csvContent);
        
        IOException exception = assertThrows(IOException.class, () -> reader.readEmployees(csvFile));
        assertTrue(exception.getMessage().contains("Invalid number format"));
    }
    
    @Test
    @DisplayName("Should throw exception for negative salary")
    void shouldThrowExceptionForNegativeSalary() throws IOException {
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                123,Joe,Doe,-1000,
                """;
        
        Path csvFile = createTempCsvFile(csvContent);
        
        IOException exception = assertThrows(IOException.class, () -> reader.readEmployees(csvFile));
        assertTrue(exception.getMessage().contains("negative"));
    }
    
    @Test
    @DisplayName("Should throw exception for empty first name")
    void shouldThrowExceptionForEmptyFirstName() throws IOException {
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                123,,Doe,60000,
                """;
        
        Path csvFile = createTempCsvFile(csvContent);
        
        IOException exception = assertThrows(IOException.class, () -> reader.readEmployees(csvFile));
        assertTrue(exception.getMessage().contains("First name"));
    }
    
    @Test
    @DisplayName("Should handle decimal salaries")
    void shouldHandleDecimalSalaries() throws IOException {
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                123,Joe,Doe,60000.50,
                """;
        
        Path csvFile = createTempCsvFile(csvContent);
        List<Employee> employees = reader.readEmployees(csvFile);
        
        assertEquals(60000.50, employees.get(0).getSalary());
    }
    
    @Test
    @DisplayName("Should handle whitespace in fields")
    void shouldHandleWhitespaceInFields() throws IOException {
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                123 , Joe , Doe , 60000 ,
                """;
        
        Path csvFile = createTempCsvFile(csvContent);
        List<Employee> employees = reader.readEmployees(csvFile);
        
        assertEquals("Joe", employees.get(0).getFirstName());
        assertEquals("Doe", employees.get(0).getLastName());
    }
    
    private Path createTempCsvFile(String content) throws IOException {
        Path csvFile = tempDir.resolve("test.csv");
        Files.writeString(csvFile, content);
        return csvFile;
    }
}

