package org.example.validators;

import org.example.model.Organization;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs multiple validators and collects all their errors.
 * Just pass in whatever validators you want to run together.
 */
public class CompositeValidator implements OrganizationValidator {

    private final EmployeeRecordValidator<Organization>[] validators;

    public CompositeValidator(EmployeeRecordValidator<Organization>... validators) {
        this.validators = validators;
    }

    /** Runs all validators and merges the errors. */
    @Override
    public List<ValidationError> validate(Organization organization) {
        ArrayList<ValidationError> errors = new ArrayList<>();
        for (EmployeeRecordValidator<Organization> validator : validators) {
            errors.addAll(validator.validate(organization));
        }
        return errors;
    }
}
