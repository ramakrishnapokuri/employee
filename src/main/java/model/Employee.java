package org.example.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An employee in the org chart.
 * 
 * Has the basic stuff (id, name, salary) plus links to their manager
 * and direct reports. The CEO is the one with no managerId.
 */
public class Employee {
    
    private final int id;
    private final String firstName;
    private final String lastName;
    private final double salary;
    private final Integer managerId;
    
    // Relationships (set after initial parsing)
    private Employee manager;
    private final List<Employee> directSubordinates;
    
    /** Creates an employee. Pass null for managerId if this is the CEO. */
    public Employee(int id, String firstName, String lastName, double salary, Integer managerId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.salary = salary;
        this.managerId = managerId;
        this.directSubordinates = new ArrayList<>();
    }


    /** Average salary of direct reports. Returns 0 if no subordinates. */
    public double getAvgSalaryOfSubordinates() {
        return directSubordinates.stream()
                .mapToDouble(Employee::getSalary)
                .average()
                .orElse(0.0);
    }


    /**
     * How many levels down from the CEO? CEO=0, their reports=1, etc.
     */
    public int findDepth() {
        int depth = 0;
        Employee current = manager;

        // Traverse up the hierarchy until we reach the CEO
        while (current != null) {
            current = current.manager;
            depth++;
        }

        return depth;
    }

    /** The chain of managers from here up to the CEO. */
    public List<Employee> getReportingChain() {
        List<Employee> chain = new ArrayList<>();
        Employee current = manager;
        while (current != null) {
            chain.add(current);
            current = current.manager;
        }
        return chain;
    }

    /**
     * Gets depth and chain in one pass. Use this when you need both
     * to avoid walking up the tree twice.
     */
    public DepthAndChain findDepthAndChain() {
        List<Employee> chain = new ArrayList<>();
        Employee current = manager;
        while (current != null) {
            chain.add(current);
            current = current.manager;
        }
        return new DepthAndChain(chain.size(), chain);
    }

    /** Holds depth + chain together so we only traverse once. */
    public record DepthAndChain(int depth, List<Employee> chain) {}
    
    // Getters
    
    public int getId() {
        return id;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public double getSalary() {
        return salary;
    }
    
    public Integer getManagerId() {
        return managerId;
    }
    
    public Employee getManager() {
        return manager;
    }
    
    public List<Employee> getDirectSubordinates() {
        return Collections.unmodifiableList(directSubordinates);
    }
    
    /** True if they have anyone reporting to them. */
    public boolean isManager() {
        return !directSubordinates.isEmpty();
    }
    
    /** True if this is the CEO (no manager). */
    public boolean isCeo() {
        return managerId == null;
    }
    
    /**
     * Sets this employee's manager and wires up the subordinate link.
     * Handles re-assignment if already had a different manager.
     */
    public void setManager(Employee newManager) {
        if (this.manager == newManager) {
            return; // No change
        }
        
        // Remove from old manager
        if (this.manager != null) {
            this.manager.directSubordinates.remove(this);
        }
        
        this.manager = newManager;
        
        // Add to new manager (if not null and not already there)
        if (newManager != null && !newManager.directSubordinates.contains(this)) {
            newManager.directSubordinates.add(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return id == employee.id;
    }
    
    @Override
    public int hashCode() {
        return id; // Primitive int is its own hash - avoids Integer boxing
    }
    
    @Override
    public String toString() {
        return String.format("Employee{id=%d, name='%s %s', salary=%.2f, managerId=%s}",
                id, firstName, lastName, salary, managerId);
    }

}
