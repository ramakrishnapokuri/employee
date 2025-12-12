package org.example.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Container for all employees. Basically a lookup table by ID
 * with the ability to wire up the manager/subordinate relationships.
 */
public class Organization {

    private final Map<Integer, Employee> employees;
    private Employee ceo;

    /** Loads employees into a map for fast lookup. */
    public Organization(List<Employee> employeeList) {
        this.employees = employeeList.stream()
                .collect(toMap(Employee::getId, identity()));
    }

    /**
     * Wires up all the manager ↔ subordinate links. 
     * Call this after validation passes.
     */
    public void buildHierarchy() {
        for (Employee employee : employees.values()) {
            Integer managerId = employee.getManagerId();
            if (managerId == null) {
                this.ceo = employee;
            } else {
                Employee manager = employees.get(managerId);
                if (manager != null) {
                    employee.setManager(manager);
                }
            }
        }
    }
    
    /** Returns the CEO. Only works after buildHierarchy() is called. */
    public Employee getCeo() {
        return ceo;
    }

    /** All employees (unmodifiable). */
    public Collection<Employee> getEmployees() {
        return Collections.unmodifiableCollection(employees.values());
    }

    /** Lookup by ID. Returns null if not found. */
    public Employee getEmployeeById(int id) {
        return employees.get(id);
    }

    public boolean hasEmployee(int id) {
        return employees.containsKey(id);
    }

    /** How many employees total. */
    public int size() {
        return employees.size();
    }
}
