package org.example.validators;

import org.example.model.Employee;
import org.example.model.Organization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.example.validators.ErrorCode.MANAGER_NOT_FOUND;

/**
 * Checks that everyone's manager actually exists (except the CEO who has none).
 */
public class InvalidManagerValidator implements OrganizationValidator {

    @Override
    public List<ValidationError> validate(Organization organization) {
        // Handle null or empty organization gracefully
        if (organization == null || organization.getEmployees().isEmpty()) {
            return Collections.emptyList();
        }

        List<ValidationError> errors = new ArrayList<>();

        // Check each employee
        for (Employee employee : organization.getEmployees()) {
            Integer managerId = employee.getManagerId();

            // If managerId is null, this is a CEO - skip validation
            if (managerId == null) {
                continue;
            }

            // If managerId is not null, verify the manager exists
            if (!organization.hasEmployee(managerId)) {
                errors.add(new ValidationError(
                    MANAGER_NOT_FOUND,
                    managerId,
                    employee.getFullName(),
                    employee.getId()
                ));
            }
        }

        return errors;
    }
}
