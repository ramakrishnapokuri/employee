package org.example.validators;

import org.example.model.Employee;
import org.example.model.Organization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.example.validators.ErrorCode.MULTIPLE_CEOS;
import static org.example.validators.ErrorCode.NO_CEO_FOUND;

/**
 * Makes sure there's exactly one CEO (employee with no manager).
 * Zero CEOs or multiple CEOs = error.
 */
public class OrganizationCeoValidator implements OrganizationValidator {

    @Override
    public List<ValidationError> validate(Organization organization) {
        // Handle null or empty organization gracefully
        if (organization == null || organization.getEmployees().isEmpty()) {
            return Collections.emptyList();
        }

        List<ValidationError> errors = new ArrayList<>();

        // Find all employees with null managerId (CEOs)
        List<Employee> ceos = organization.getEmployees().stream()
                .filter(employee -> employee.getManagerId() == null)
                .toList();

        // If no CEO found
        if( ceos.isEmpty()) {
            errors.add(new ValidationError( NO_CEO_FOUND ));
        }

        // If more than one CEO found, create error
        else if (ceos.size() > 1) {
            // Report the first two CEOs found
            String firstCeo = ceos.get(0).getFullName();
            String secondCeo = ceos.get(1).getFullName();

            errors.add(new ValidationError(
                MULTIPLE_CEOS,
                firstCeo,
                secondCeo
            ));
        }

        return errors;
    }
}

