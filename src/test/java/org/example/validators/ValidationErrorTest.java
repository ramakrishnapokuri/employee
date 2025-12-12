package org.example.validators;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationError Tests")
class ValidationErrorTest {

    @Test
    @DisplayName("Should create error with error code and format message")
    void testConstructor_FormatsMessage() {
        ValidationError error = new ValidationError(ErrorCode.UNIQUE_ID_VIOLATION, 123);

        assertEquals(ErrorCode.UNIQUE_ID_VIOLATION, error.getErrorCode());
        assertEquals("Duplicate employee ID found: 123", error.getMessage());
    }

    @Test
    @DisplayName("Should create error with multiple arguments")
    void testConstructor_MultipleArgs() {
        ValidationError error = new ValidationError(
                ErrorCode.MANAGER_NOT_FOUND, 999, "John Doe", 123
        );

        assertEquals(ErrorCode.MANAGER_NOT_FOUND, error.getErrorCode());
        assertTrue(error.getMessage().contains("999"));
        assertTrue(error.getMessage().contains("John Doe"));
        assertTrue(error.getMessage().contains("123"));
    }

    @Test
    @DisplayName("Should create error without arguments")
    void testConstructor_NoArgs() {
        ValidationError error = new ValidationError(ErrorCode.EMPTY_EMPLOYEE_LIST);

        assertEquals(ErrorCode.EMPTY_EMPLOYEE_LIST, error.getErrorCode());
        assertEquals("No employee records found", error.getMessage());
    }

    @Test
    @DisplayName("Should provide meaningful toString")
    void testToString_ContainsCodeAndMessage() {
        ValidationError error = new ValidationError(ErrorCode.NO_CEO_FOUND);
        String str = error.toString();

        assertTrue(str.contains("NO_CEO_FOUND"));
        assertTrue(str.contains("No CEO found"));
    }

    @Test
    @DisplayName("Should format MULTIPLE_CEOS error correctly")
    void testMultipleCeosError() {
        ValidationError error = new ValidationError(
                ErrorCode.MULTIPLE_CEOS, "CEO One", "CEO Two"
        );

        assertTrue(error.getMessage().contains("CEO One"));
        assertTrue(error.getMessage().contains("CEO Two"));
    }

    @Test
    @DisplayName("Should format CIRCULAR_REFERENCE error correctly")
    void testCircularReferenceError() {
        ValidationError error = new ValidationError(
                ErrorCode.CIRCULAR_REFERENCE, "John Doe", 123, 456
        );

        assertTrue(error.getMessage().contains("John Doe"));
        assertTrue(error.getMessage().contains("123"));
        assertTrue(error.getMessage().contains("456"));
    }
}

