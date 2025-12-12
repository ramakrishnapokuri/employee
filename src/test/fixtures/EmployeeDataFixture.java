package org.example.fixtures;

import org.example.model.Employee;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * EmployeeDataFixture is the root test data builder for Employee-related domain objects.
 *
 * It provides a fluent, domain-specific API to construct complex Employee test data for unit and integration tests.
 * Use this fixture to easily create employees with various roles, salaries, and organizational relationships.
 *
 * Example usage:
 * <pre>
 *   EmployeeDataFixture.employee()
 *       .withFirstName("John")
 *       .withLastName("Doe")
 *       .withSalary(60000)
 *       .build();
 * </pre>
 */
public class EmployeeDataFixture {

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);

    private int id;
    private String firstName;
    private String lastName;
    private int salary;
    private Integer managerId;

    /**
     * Constructor with sensible defaults.
     */
    public EmployeeDataFixture() {
        this.id = ID_GENERATOR.getAndIncrement();
        this.firstName = "Employee";
        this.lastName = "Test" + id;
        this.salary = 50000;
        this.managerId = null;
    }

    /**
     * Static factory method - main entry point for creating an employee fixture.
     *
     * @return a new EmployeeDataFixture with default values
     */
    public static EmployeeDataFixture employee() {
        return new EmployeeDataFixture();
    }

    /**
     * Creates a CEO employee (no manager).
     *
     * @return a new EmployeeDataFixture configured as a CEO
     */
    public static EmployeeDataFixture ceo() {
        return employee()
                .withFirstName("John")
                .withLastName("CEO")
                .withSalary(150000)
                .withManagerId(null);
    }

    /**
     * Creates a manager employee with typical manager salary.
     *
     * @return a new EmployeeDataFixture configured as a manager
     */
    public static EmployeeDataFixture manager() {
        return employee()
                .withFirstName("Jane")
                .withLastName("Manager")
                .withSalary(90000);
    }

    /**
     * Creates a regular employee with typical salary.
     *
     * @return a new EmployeeDataFixture configured as a regular employee
     */
    public static EmployeeDataFixture regularEmployee() {
        return employee()
                .withFirstName("Bob")
                .withLastName("Employee")
                .withSalary(50000);
    }

    /**
     * Sets the employee ID.
     *
     * @param id the employee ID
     * @return this fixture for method chaining
     */
    public EmployeeDataFixture withId(int id) {
        this.id = id;
        return this;
    }

    /**
     * Sets the employee's first name.
     *
     * @param firstName the first name
     * @return this fixture for method chaining
     */
    public EmployeeDataFixture withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    /**
     * Sets the employee's last name.
     *
     * @param lastName the last name
     * @return this fixture for method chaining
     */
    public EmployeeDataFixture withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    /**
     * Sets the employee's salary.
     *
     * @param salary the salary
     * @return this fixture for method chaining
     */
    public EmployeeDataFixture withSalary(int salary) {
        this.salary = salary;
        return this;
    }

    /**
     * Sets the employee's manager ID.
     *
     * @param managerId the manager ID (null for CEO)
     * @return this fixture for method chaining
     */
    public EmployeeDataFixture withManagerId(Integer managerId) {
        this.managerId = managerId;
        return this;
    }

    /**
     * Sets this employee as a CEO (no manager).
     *
     * @return this fixture for method chaining
     */
    public EmployeeDataFixture asCeo() {
        this.managerId = null;
        this.salary = Math.max(this.salary, 120000);
        return this;
    }

    /**
     * Sets this employee to report to a specific manager.
     *
     * @param managerId the manager's ID
     * @return this fixture for method chaining
     */
    public EmployeeDataFixture reportingTo(int managerId) {
        this.managerId = managerId;
        return this;
    }

    /**
     * Builds and returns the Employee object.
     *
     * @return the constructed Employee
     */
    public Employee build() {
        return new Employee(id, firstName, lastName, salary, managerId);
    }

    /**
     * Builds multiple employees from a list of fixtures.
     *
     * @param fixtures the employee fixtures
     * @return a list of Employee objects
     */
    public static List<Employee> buildList(EmployeeDataFixture... fixtures) {
        List<Employee> employees = new ArrayList<>();
        for (EmployeeDataFixture fixture : fixtures) {
            employees.add(fixture.build());
        }
        return employees;
    }

    /**
     * Resets the ID generator (useful for test isolation).
     */
    public static void resetIdGenerator() {
        ID_GENERATOR.set(1);
    }
}

