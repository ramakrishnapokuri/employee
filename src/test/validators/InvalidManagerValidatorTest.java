package org.example.validators;

import org.example.model.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.example.fixtures.EmployeeDataFixture.*;
import static org.example.fixtures.OrganizationDataFixture.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ManagerNotNullValidator Tests")
class InvalidManagerValidatorTest {

    private InvalidManagerValidator validator;

    @BeforeEach
    void setUp() {
        validator = new InvalidManagerValidator();
        resetIdGenerator();
    }

    @Test
    @DisplayName("Should pass validation when organization is null")
    void testValidate_WithNullOrganization_ReturnsNoErrors() {
        // Act
        List<ValidationError> errors = validator.validate(null);

        // Assert
        assertTrue(errors.isEmpty(), "Null organization should not produce validation errors");
    }

    @Test
    @DisplayName("Should pass validation when organization is empty")
    void testValidate_WithEmptyOrganization_ReturnsNoErrors() {
        // Arrange
        Organization organization = emptyOrganization().build();

        // Act
        List<ValidationError> errors = validator.validate(organization);

        // Assert
        assertTrue(errors.isEmpty(), "Empty organization should not produce validation errors");
    }

    @Test
    @DisplayName("Should pass validation when only CEO exists (null manager)")
    void testValidate_WithOnlyCeo_ReturnsNoErrors() {
        // Arrange
        Organization organization = singleCeoOrganization().build();

        // Act
        List<ValidationError> errors = validator.validate(organization);

        // Assert
        assertTrue(errors.isEmpty(), "CEO with null manager should be valid");
    }

    @Test
    @DisplayName("Should pass validation when all employees have valid managers")
    void testValidate_WithValidManagers_ReturnsNoErrors() {
        // Arrange
        Organization organization = basicOrganization().build();

        // Act
        List<ValidationError> errors = validator.validate(organization);

        // Assert
        assertTrue(errors.isEmpty(), "All employees with valid managers should pass validation");
    }

    @Test
    @DisplayName("Should return error when employee has non-existent manager ID")
    void testValidate_WithNonExistentManager_ReturnsError() {
        // Arrange
        Organization organization = organization()
                .withCeo(1)
                .withEmployee(employee().withId(2).withFirstName("John").withLastName("Doe").reportingTo(999))
                .build();

        // Act
        List<ValidationError> errors = validator.validate(organization);

        // Assert
        assertEquals(1, errors.size());
        assertEquals(ErrorCode.MANAGER_NOT_FOUND, errors.get(0).getErrorCode());
        assertTrue(errors.get(0).getMessage().contains("999"));
        assertTrue(errors.get(0).getMessage().contains("John Doe"));
    }

    @Test
    @DisplayName("Should return multiple errors when multiple employees have invalid managers")
    void testValidate_WithMultipleInvalidManagers_ReturnsMultipleErrors() {
        // Arrange
        Organization organization = organization()
                .withCeo(1)
                .withEmployee(employee().withId(2).withFirstName("John").withLastName("Doe").reportingTo(888))
                .withEmployee(employee().withId(3).withFirstName("Jane").withLastName("Smith").reportingTo(999))
                .build();

        // Act
        List<ValidationError> errors = validator.validate(organization);

        // Assert
        assertEquals(2, errors.size());

        List<String> errorMessages = errors.stream()
                .map(ValidationError::getMessage)
                .toList();

        assertTrue(errorMessages.stream().anyMatch(msg -> msg.contains("888")));
        assertTrue(errorMessages.stream().anyMatch(msg -> msg.contains("999")));
    }

    @Test
    @DisplayName("Should pass validation with multiple CEOs (all with null managers)")
    void testValidate_WithMultipleCeos_ReturnsNoErrors() {
        // Arrange
        Organization organization = organization()
                .withCeo(1)
                .withCeo(2)
                .withEmployee(regularEmployee().withId(3).reportingTo(1))
                .build();

        // Act
        List<ValidationError> errors = validator.validate(organization);

        // Assert
        assertTrue(errors.isEmpty(), "Multiple CEOs with null managers should be valid for this validator");
    }

    @Test
    @DisplayName("Should pass validation when employee references itself as manager (self-reference)")
    void testValidate_WithSelfReference_PassesValidation() {
        // Arrange
        Organization organization = organization()
                .withCeo(1)
                .withEmployee(employee().withId(2).withFirstName("Circular").withLastName("Reference").reportingTo(2))
                .build();

        // Act
        List<ValidationError> errors = validator.validate(organization);

        // Assert
        assertTrue(errors.isEmpty(), "Self-reference is technically valid (manager exists), circular detection is separate");
    }

    @Test
    @DisplayName("Should pass validation with deep hierarchy and all valid managers")
    void testValidate_WithDeepHierarchy_ReturnsNoErrors() {
        // Arrange
        Organization organization = deepHierarchyOrganization().build();

        // Act
        List<ValidationError> errors = validator.validate(organization);

        // Assert
        assertTrue(errors.isEmpty(), "Deep hierarchy with all valid managers should pass");
    }
}
