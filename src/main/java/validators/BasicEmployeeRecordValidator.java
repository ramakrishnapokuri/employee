package org.example.validators;

import org.example.model.Employee;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.example.validators.ErrorCode.EMPTY_EMPLOYEE_LIST;
import static org.example.validators.ErrorCode.UNIQUE_ID_VIOLATION;

/**
 * Checks basic stuff: is the list empty? Any duplicate IDs?
 */
public class BasicEmployeeRecordValidator implements EmployeeRecordValidator<List<Employee>> {

    @Override
    public List<ValidationError> validate(List<Employee> employees) {
        List<ValidationError> errors = new ArrayList<>();

        if (employees == null || employees.isEmpty()) {
            return List.of(
                    new ValidationError( EMPTY_EMPLOYEE_LIST)
            );
        }

        Set<Integer> seenIds = new HashSet<>();
        Set<Integer> duplicateIds = new HashSet<>();

        // Find all duplicate IDs - O(n) time complexity
        for (Employee employee : employees) {
            int id = employee.getId();
            if (!seenIds.add(id)) {
                duplicateIds.add(id);
            }
        }

        // Create an error for each duplicate ID - O(d) time complexity where d = number of duplicates
        for (Integer duplicateId : duplicateIds) {
            errors.add(new ValidationError(UNIQUE_ID_VIOLATION, duplicateId));
        }

        return errors;
    }
}

