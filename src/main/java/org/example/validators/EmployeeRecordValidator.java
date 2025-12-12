package org.example.validators;

import java.util.List;

/**
 * Interface for validators. Each implementation checks something different
 * (empty list, bad manager refs, circular deps, etc).
 * 
 * @param <T> what we're validating (employee list or organization)
 */
public interface EmployeeRecordValidator<T> {

    /** Check the data and return any errors found. Empty list = all good. */
    List<ValidationError> validate(T data);
}
