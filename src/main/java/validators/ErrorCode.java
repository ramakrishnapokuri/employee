package org.example.validators;

public enum ErrorCode {

    EMPTY_EMPLOYEE_LIST("No employee records found"),
    UNIQUE_ID_VIOLATION("Duplicate employee ID found: %d"),
    MANAGER_NOT_FOUND("Manager ID %d not found for employee %s (ID: %d)"),
    MULTIPLE_CEOS("Multiple CEOs found: %s and %s"),
    NO_CEO_FOUND("No CEO found (no employee without manager)"),
    CIRCULAR_REFERENCE("Circular reference detected: %s (ID: %d) has manager chain leading back to ID %d");

    private final String messageTemplate;

    ErrorCode(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public String getMessage(Object... args) {
        return String.format(messageTemplate, args);
    }

}
