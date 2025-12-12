package org.example.validators;

import org.example.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.example.fixtures.EmployeeDataFixture.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BasicEmployeeRecordValidator Tests")
class BasicEmployeeRecordValidatorTest {

    private BasicEmployeeRecordValidator validator;

    @BeforeEach
    void setUp() {
        validator = new BasicEmployeeRecordValidator();
        resetIdGenerator();
    }

    @Test
    @DisplayName("Should return validation error when employee list is null")
    void testValidate_WithNullEmployeeList_ReturnsError() {
        // Act
        List<ValidationError> errors = validator.validate(null);

        // Assert
        assertEquals(1, errors.size());
        assertEquals(ErrorCode.EMPTY_EMPLOYEE_LIST, errors.get(0).getErrorCode());
        assertEquals("No employee records found", errors.get(0).getMessage());
    }

    @Test
    @DisplayName("Should return validation error when employee list is empty")
    void testValidate_WithEmptyEmployeeList_ReturnsError() {
        // Arrange
        List<Employee> employees = Collections.emptyList();

        // Act
        List<ValidationError> errors = validator.validate(employees);

        // Assert
        assertEquals(1, errors.size());
        assertEquals(ErrorCode.EMPTY_EMPLOYEE_LIST, errors.get(0).getErrorCode());
        assertTrue(errors.get(0).getMessage().contains("No employee records found"));
    }

    @Test
    @DisplayName("Should pass validation when at least one employee exists")
    void testValidate_WithSingleEmployee_ReturnsNoErrors() {
        // Arrange
        List<Employee> employees = buildList(
                employee().withFirstName("John").withLastName("Doe")
        );

        // Act
        List<ValidationError> errors = validator.validate(employees);

        // Assert
        assertTrue(errors.isEmpty(), "Should not return any errors when at least one employee exists");
    }

    @Test
    @DisplayName("Should pass validation when multiple employees exist")
    void testValidate_WithMultipleEmployees_ReturnsNoErrors() {
        // Arrange
        List<Employee> employees = buildList(
                ceo().withFirstName("John").withLastName("Doe"),
                manager().withFirstName("Jane").withLastName("Smith").reportingTo(1),
                regularEmployee().withFirstName("Bob").withLastName("Johnson").reportingTo(1)
        );

        // Act
        List<ValidationError> errors = validator.validate(employees);

        // Assert
        assertTrue(errors.isEmpty(), "Should not return any errors when employees exist");
    }

    @Test
    @DisplayName("Should detect duplicate employee IDs")
    void testValidate_WithDuplicateIds_ReturnsError() {
        // Arrange - two employees with same ID
        List<Employee> employees = buildList(
                employee().withId(100).withFirstName("John").withLastName("Doe"),
                employee().withId(100).withFirstName("Jane").withLastName("Smith")  // Duplicate ID
        );

        // Act
        List<ValidationError> errors = validator.validate(employees);

        // Assert
        assertEquals(1, errors.size());
        assertEquals(ErrorCode.UNIQUE_ID_VIOLATION, errors.get(0).getErrorCode());
        assertTrue(errors.get(0).getMessage().contains("100"));
    }

    @Test
    @DisplayName("Should detect multiple duplicate IDs")
    void testValidate_WithMultipleDuplicateIds_ReturnsMultipleErrors() {
        // Arrange - multiple sets of duplicate IDs
        List<Employee> employees = buildList(
                employee().withId(100).withFirstName("John").withLastName("A"),
                employee().withId(100).withFirstName("Jane").withLastName("B"),  // Duplicate 100
                employee().withId(200).withFirstName("Bob").withLastName("C"),
                employee().withId(200).withFirstName("Alice").withLastName("D"), // Duplicate 200
                employee().withId(300).withFirstName("Charlie").withLastName("E")  // Unique
        );

        // Act
        List<ValidationError> errors = validator.validate(employees);

        // Assert
        assertEquals(2, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.getMessage().contains("100")));
        assertTrue(errors.stream().anyMatch(e -> e.getMessage().contains("200")));
    }

    @Test
    @DisplayName("Should handle three employees with same ID")
    void testValidate_WithTripleDuplicateId_ReturnsOneError() {
        // Arrange - three employees with same ID
        List<Employee> employees = buildList(
                employee().withId(100).withFirstName("A").withLastName("A"),
                employee().withId(100).withFirstName("B").withLastName("B"),
                employee().withId(100).withFirstName("C").withLastName("C")
        );

        // Act
        List<ValidationError> errors = validator.validate(employees);

        // Assert - should only report one error per duplicate ID
        assertEquals(1, errors.size());
        assertEquals(ErrorCode.UNIQUE_ID_VIOLATION, errors.get(0).getErrorCode());
    }
}
