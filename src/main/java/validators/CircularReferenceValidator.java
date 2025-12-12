package org.example.validators;

import org.example.model.Employee;
import org.example.model.Organization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.example.validators.ErrorCode.CIRCULAR_REFERENCE;

/**
 * Detects circular manager chains (A → B → C → A). 
 * Walks up each person's chain and flags if we see the same ID twice.
 */
public class CircularReferenceValidator implements OrganizationValidator {

    @Override
    public List<ValidationError> validate(Organization organization) {
        // Handle null or empty organization gracefully
        if (organization == null || organization.getEmployees().isEmpty()) {
            return Collections.emptyList();
        }

        List<ValidationError> errors = new ArrayList<>();
        Set<Integer> employeesInCycles = new HashSet<>();

        for (Employee employee : organization.getEmployees()) {
            // Skip if already identified as part of a cycle
            if (employeesInCycles.contains(employee.getId())) {
                continue;
            }

            Set<Integer> visited = new HashSet<>();
            Integer currentId = employee.getId();
            Integer managerId = employee.getManagerId();

            visited.add(currentId);

            while (managerId != null) {
                if (visited.contains(managerId)) {
                    // Found a cycle - record all employees in this path
                    employeesInCycles.addAll(visited);
                    errors.add(new ValidationError(
                            CIRCULAR_REFERENCE,
                            employee.getFullName(),
                            employee.getId(),
                            managerId
                    ));
                    break;
                }

                visited.add(managerId);
                Employee manager = organization.getEmployeeById(managerId);
                if (manager == null) {
                    // Manager not found - handled by InvalidManagerValidator
                    break;
                }
                managerId = manager.getManagerId();
            }
        }

        return errors;
    }
}

