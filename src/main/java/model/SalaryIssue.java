package org.example.model;

/**
 * A manager whose salary is outside the 20-50% range above their reports' average.
 * Immutable – captures a snapshot of the problem for the report.
 */
public class SalaryIssue implements Issue {


    private final Employee manager;

    private final Type type;
    private final double subordinatesAverageSalary;
    private final double expectedMinSalary;
    private final double expectedMaxSalary;
    private final double difference;
    public SalaryIssue(Employee manager,
                       Type type,
                       double subordinatesAverageSalary,
                       double expectedMinSalary,
                       double expectedMaxSalary) {
        this.manager = manager;
        this.type = type;
        this.subordinatesAverageSalary = subordinatesAverageSalary;
        this.expectedMinSalary = expectedMinSalary;
        this.expectedMaxSalary = expectedMaxSalary;
        this.difference = Math.abs(manager.getSalary() - (type == Type.UNDERPAID_ISSUE ? expectedMinSalary : expectedMaxSalary));
    }

    @Override
    public String getDescription() {
        return toString();
    }

    public Employee getManager() {
        return manager;
    }
    
    public Type getType() {
        return type;
    }
    
    public double getCurrentSalary() {
        return manager.getSalary();
    }
    
    public double getSubordinatesAverageSalary() {
        return subordinatesAverageSalary;
    }
    
    public double getExpectedMinSalary() {
        return expectedMinSalary;
    }
    
    public double getExpectedMaxSalary() {
        return expectedMaxSalary;
    }
    
    /** How much they're off by (always positive). */
    public double getDifference() {
        return difference;
    }
    
    @Override
    public String toString() {
        String issueType = type == Type.UNDERPAID_ISSUE ? "earns less than they should" : "earns more than they should";
        return String.format("%s (ID: %d) %s by %.2f",
                manager.getFullName(), manager.getId(), issueType, difference);
    }
}

