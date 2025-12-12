package org.example.validators;

import org.example.model.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.example.fixtures.EmployeeDataFixture.*;
import static org.example.fixtures.OrganizationDataFixture.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CircularReferenceValidator Tests")
class CircularReferenceValidatorTest {

    private CircularReferenceValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CircularReferenceValidator();
        resetIdGenerator();
    }

    @Test
    @DisplayName("Should pass validation when organization is null")
    void testValidate_WithNullOrganization_ReturnsNoErrors() {
        List<ValidationError> errors = validator.validate(null);
        assertTrue(errors.isEmpty(), "Null organization should not produce validation errors");
    }

    @Test
    @DisplayName("Should pass validation when organization is empty")
    void testValidate_WithEmptyOrganization_ReturnsNoErrors() {
        Organization organization = emptyOrganization().build();
        List<ValidationError> errors = validator.validate(organization);
        assertTrue(errors.isEmpty(), "Empty organization should not produce validation errors");
    }

    @Test
    @DisplayName("Should pass validation with valid hierarchy")
    void testValidate_WithValidHierarchy_ReturnsNoErrors() {
        Organization organization = basicOrganization().build();
        List<ValidationError> errors = validator.validate(organization);
        assertTrue(errors.isEmpty(), "Valid hierarchy should not produce validation errors");
    }

    @Test
    @DisplayName("Should pass validation with single CEO")
    void testValidate_WithSingleCeo_ReturnsNoErrors() {
        Organization organization = singleCeoOrganization().build();
        List<ValidationError> errors = validator.validate(organization);
        assertTrue(errors.isEmpty(), "Single CEO should not produce validation errors");
    }

    @Test
    @DisplayName("Should pass validation with deep hierarchy")
    void testValidate_WithDeepHierarchy_ReturnsNoErrors() {
        Organization organization = deepHierarchyOrganization().build();
        List<ValidationError> errors = validator.validate(organization);
        assertTrue(errors.isEmpty(), "Deep hierarchy without cycles should not produce validation errors");
    }

    @Test
    @DisplayName("Should detect self-referencing employee")
    void testValidate_WithSelfReference_ReturnsError() {
        // Employee reports to themselves
        Organization organization = organization()
                .withEmployee(ceo().withId(1))
                .withEmployee(employee().withId(2).withFirstName("Self").withLastName("Reference").reportingTo(2))
                .build();

        List<ValidationError> errors = validator.validate(organization);

        assertEquals(1, errors.size());
        assertEquals(ErrorCode.CIRCULAR_REFERENCE, errors.get(0).getErrorCode());
        assertTrue(errors.get(0).getMessage().contains("Self Reference"));
    }

    @Test
    @DisplayName("Should detect two-employee cycle")
    void testValidate_WithTwoEmployeeCycle_ReturnsError() {
        // A reports to B, B reports to A
        Organization organization = organization()
                .withEmployee(employee().withId(1).withFirstName("Employee").withLastName("A").reportingTo(2))
                .withEmployee(employee().withId(2).withFirstName("Employee").withLastName("B").reportingTo(1))
                .build();

        List<ValidationError> errors = validator.validate(organization);

        assertFalse(errors.isEmpty(), "Two-employee cycle should produce validation errors");
        assertEquals(ErrorCode.CIRCULAR_REFERENCE, errors.get(0).getErrorCode());
    }

    @Test
    @DisplayName("Should detect three-employee cycle")
    void testValidate_WithThreeEmployeeCycle_ReturnsError() {
        // A -> B -> C -> A
        Organization organization = organization()
                .withEmployee(employee().withId(1).withFirstName("Employee").withLastName("A").reportingTo(3))
                .withEmployee(employee().withId(2).withFirstName("Employee").withLastName("B").reportingTo(1))
                .withEmployee(employee().withId(3).withFirstName("Employee").withLastName("C").reportingTo(2))
                .build();

        List<ValidationError> errors = validator.validate(organization);

        assertFalse(errors.isEmpty(), "Three-employee cycle should produce validation errors");
    }

    @Test
    @DisplayName("Should handle mixed valid and circular references")
    void testValidate_WithMixedValidAndCircular_ReturnsErrorsOnlyForCircular() {
        // Valid: CEO -> Manager -> Worker
        // Circular: A -> B -> A
        Organization organization = organization()
                .withEmployee(ceo().withId(1))
                .withEmployee(manager().withId(2).reportingTo(1))
                .withEmployee(regularEmployee().withId(3).reportingTo(2))
                .withEmployee(employee().withId(4).withFirstName("Circular").withLastName("A").reportingTo(5))
                .withEmployee(employee().withId(5).withFirstName("Circular").withLastName("B").reportingTo(4))
                .build();

        List<ValidationError> errors = validator.validate(organization);

        assertFalse(errors.isEmpty(), "Should detect circular reference in mixed organization");
    }

    @Test
    @DisplayName("Should handle employee with non-existent manager gracefully")
    void testValidate_WithNonExistentManager_ReturnsNoCircularError() {
        // Employee references a manager that doesn't exist
        Organization organization = organization()
                .withEmployee(ceo().withId(1))
                .withEmployee(employee().withId(2).reportingTo(999)) // Manager 999 doesn't exist
                .build();

        List<ValidationError> errors = validator.validate(organization);

        // Should not detect circular reference (handled by InvalidManagerValidator)
        assertTrue(errors.isEmpty(), "Non-existent manager should not be flagged as circular reference");
    }
}

