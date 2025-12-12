package org.example.validators;

import org.example.model.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.example.fixtures.EmployeeDataFixture.*;
import static org.example.fixtures.OrganizationDataFixture.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CompositeValidator Tests")
class CompositeValidatorTest {

    @BeforeEach
    void setUp() {
        resetIdGenerator();
    }

    @Test
    @DisplayName("Should return no errors when all validators pass")
    void testValidate_AllValidatorPass_ReturnsNoErrors() {
        Organization organization = basicOrganization().build();

        CompositeValidator validator = new CompositeValidator(
                new InvalidManagerValidator(),
                new OrganizationCeoValidator()
        );

        List<ValidationError> errors = validator.validate(organization);

        assertTrue(errors.isEmpty(), "Should return no errors when organization is valid");
    }

    @Test
    @DisplayName("Should aggregate errors from multiple validators")
    void testValidate_MultipleValidatorsFail_ReturnsAllErrors() {
        // Organization with invalid manager AND multiple CEOs
        Organization organization = organization()
                .withEmployee(ceo().withId(1).withFirstName("CEO").withLastName("One"))
                .withEmployee(ceo().withId(2).withFirstName("CEO").withLastName("Two"))
                .withEmployee(employee().withId(3).reportingTo(999)) // Non-existent manager
                .build();

        CompositeValidator validator = new CompositeValidator(
                new InvalidManagerValidator(),
                new OrganizationCeoValidator()
        );

        List<ValidationError> errors = validator.validate(organization);

        assertEquals(2, errors.size(), "Should return errors from both validators");
    }

    @Test
    @DisplayName("Should work with single validator")
    void testValidate_SingleValidator_ReturnsErrors() {
        Organization organization = organization()
                .withEmployee(ceo().withId(1))
                .withEmployee(ceo().withId(2))
                .build();

        CompositeValidator validator = new CompositeValidator(
                new OrganizationCeoValidator()
        );

        List<ValidationError> errors = validator.validate(organization);

        assertEquals(1, errors.size());
        assertEquals(ErrorCode.MULTIPLE_CEOS, errors.get(0).getErrorCode());
    }

    @Test
    @DisplayName("Should work with no validators")
    void testValidate_NoValidators_ReturnsNoErrors() {
        Organization organization = basicOrganization().build();

        CompositeValidator validator = new CompositeValidator();

        List<ValidationError> errors = validator.validate(organization);

        assertTrue(errors.isEmpty(), "Should return no errors with no validators");
    }

    @Test
    @DisplayName("Should include CircularReferenceValidator in composite")
    void testValidate_WithCircularReferenceValidator_DetectsCircle() {
        Organization organization = organization()
                .withEmployee(employee().withId(1).withFirstName("Circular").withLastName("A").reportingTo(2))
                .withEmployee(employee().withId(2).withFirstName("Circular").withLastName("B").reportingTo(1))
                .build();

        CompositeValidator validator = new CompositeValidator(
                new InvalidManagerValidator(),
                new OrganizationCeoValidator(),
                new CircularReferenceValidator()
        );

        List<ValidationError> errors = validator.validate(organization);

        // Should have NO_CEO_FOUND and CIRCULAR_REFERENCE errors
        assertTrue(errors.size() >= 2, "Should detect multiple validation issues");
    }

    @Test
    @DisplayName("Should preserve order of errors from validators")
    void testValidate_PreservesErrorOrder() {
        Organization organization = organization()
                .withEmployee(ceo().withId(1))
                .withEmployee(ceo().withId(2))
                .withEmployee(employee().withId(3).reportingTo(999))
                .build();

        CompositeValidator validator = new CompositeValidator(
                new InvalidManagerValidator(),  // First
                new OrganizationCeoValidator()  // Second
        );

        List<ValidationError> errors = validator.validate(organization);

        // First error should be from InvalidManagerValidator
        assertEquals(ErrorCode.MANAGER_NOT_FOUND, errors.get(0).getErrorCode());
        // Second error should be from OrganizationCeoValidator
        assertEquals(ErrorCode.MULTIPLE_CEOS, errors.get(1).getErrorCode());
    }
}

