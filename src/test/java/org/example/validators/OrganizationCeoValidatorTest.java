package org.example.validators;

import org.example.model.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.example.fixtures.EmployeeDataFixture.*;
import static org.example.fixtures.OrganizationDataFixture.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MultipleCeoValidator Tests")
class OrganizationCeoValidatorTest {

    private OrganizationCeoValidator validator;

    @BeforeEach
    void setUp() {
        validator = new OrganizationCeoValidator();
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
    @DisplayName("Should pass validation when only one CEO exists")
    void testValidate_WithSingleCeo_ReturnsNoErrors() {
        // Arrange
        Organization organization = singleCeoOrganization().build();

        // Act
        List<ValidationError> errors = validator.validate(organization);

        // Assert
        assertTrue(errors.isEmpty(), "Single CEO should be valid");
    }

    @Test
    @DisplayName("Should pass validation with normal organization structure")
    void testValidate_WithBasicOrganization_ReturnsNoErrors() {
        // Arrange
        Organization organization = basicOrganization().build();

        // Act
        List<ValidationError> errors = validator.validate(organization);

        // Assert
        assertTrue(errors.isEmpty(), "Organization with one CEO and employees should be valid");
    }

    @Test
    @DisplayName("Should return error when two CEOs exist")
    void testValidate_WithTwoCeos_ReturnsError() {
        // Arrange
        Organization organization = organization()
                .withEmployee(ceo().withId(1).withFirstName("John").withLastName("CEO"))
                .withEmployee(ceo().withId(2).withFirstName("Jane").withLastName("CEO"))
                .build();

        // Act
        List<ValidationError> errors = validator.validate(organization);

        // Assert
        assertEquals(1, errors.size());
        assertEquals(ErrorCode.MULTIPLE_CEOS, errors.get(0).getErrorCode());
        assertTrue(errors.get(0).getMessage().contains("John CEO"));
        assertTrue(errors.get(0).getMessage().contains("Jane CEO"));
    }

    @Test
    @DisplayName("Should return error when three CEOs exist")
    void testValidate_WithThreeCeos_ReturnsError() {
        // Arrange
        Organization organization = organization()
                .withEmployee(ceo().withId(1).withFirstName("John").withLastName("Smith"))
                .withEmployee(ceo().withId(2).withFirstName("Jane").withLastName("Doe"))
                .withEmployee(ceo().withId(3).withFirstName("Bob").withLastName("Johnson"))
                .build();

        // Act
        List<ValidationError> errors = validator.validate(organization);

        // Assert
        assertEquals(1, errors.size());
        assertEquals(ErrorCode.MULTIPLE_CEOS, errors.get(0).getErrorCode());
        // Should report first two CEOs
        String message = errors.get(0).getMessage();
        assertTrue(message.contains("John Smith") || message.contains("Jane Doe"));
    }

    @Test
    @DisplayName("Should return error when multiple CEOs exist with employees")
    void testValidate_WithMultipleCeosAndEmployees_ReturnsError() {
        // Arrange
        Organization organization = organization()
                .withEmployee(ceo().withId(1).withFirstName("CEO").withLastName("One"))
                .withEmployee(ceo().withId(2).withFirstName("CEO").withLastName("Two"))
                .withEmployee(regularEmployee().withId(3).reportingTo(1))
                .withEmployee(regularEmployee().withId(4).reportingTo(2))
                .build();

        // Act
        List<ValidationError> errors = validator.validate(organization);

        // Assert
        assertEquals(1, errors.size());
        assertEquals(ErrorCode.MULTIPLE_CEOS, errors.get(0).getErrorCode());
    }

    @Test
    @DisplayName("Should pass validation with deep hierarchy and single CEO")
    void testValidate_WithDeepHierarchyAndSingleCeo_ReturnsNoErrors() {
        // Arrange
        Organization organization = deepHierarchyOrganization().build();

        // Act
        List<ValidationError> errors = validator.validate(organization);

        // Assert
        assertTrue(errors.isEmpty(), "Deep hierarchy with single CEO should be valid");
    }

    @Test
    @DisplayName("Should pass validation with flat organization and single CEO")
    void testValidate_WithFlatOrganizationAndSingleCeo_ReturnsNoErrors() {
        // Arrange
        Organization organization = flatOrganization().build();

        // Act
        List<ValidationError> errors = validator.validate(organization);

        // Assert
        assertTrue(errors.isEmpty(), "Flat organization with single CEO should be valid");
    }

    @Test
    @DisplayName("Should return error when no CEO exists (all employees have managers)")
    void testValidate_WithNoCeo_ReturnsError() {
        // Arrange - All employees have managers (no CEO - invalid)
        Organization organization = organization()
                .withEmployee(employee().withId(1).reportingTo(2))
                .withEmployee(employee().withId(2).reportingTo(1))
                .build();

        // Act
        List<ValidationError> errors = validator.validate(organization);

        // Assert
        assertEquals(1, errors.size());
        assertEquals(ErrorCode.NO_CEO_FOUND, errors.get(0).getErrorCode());
    }
}

