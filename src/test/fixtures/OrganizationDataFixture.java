package org.example.fixtures;

import org.example.model.Employee;
import org.example.model.Organization;

import java.util.ArrayList;
import java.util.List;

/**
 * OrganizationDataFixture is the root test data builder for Organization-related domain objects.
 *
 * It provides a fluent, domain-specific API to construct complex Organization test data for unit and integration tests.
 * Use this fixture to easily create organizations with various structures, hierarchies, and employee configurations.
 *
 * Example usage:
 * <pre>
 *   OrganizationDataFixture.organization()
 *       .withCeo()
 *       .withManager(reportingTo: 1)
 *       .withEmployees(2)
 *       .build();
 * </pre>
 */
public class OrganizationDataFixture {

    private final List<EmployeeDataFixture> employeeFixtures;

    /**
     * Constructor with empty employee list.
     */
    public OrganizationDataFixture() {
        this.employeeFixtures = new ArrayList<>();
    }

    /**
     * Static factory method - main entry point for creating an organization fixture.
     *
     * @return a new OrganizationDataFixture with no employees
     */
    public static OrganizationDataFixture organization() {
        return new OrganizationDataFixture();
    }

    /**
     * Creates an empty organization with no employees.
     *
     * @return a new OrganizationDataFixture with no employees
     */
    public static OrganizationDataFixture emptyOrganization() {
        return new OrganizationDataFixture();
    }

    /**
     * Creates a simple organization with one CEO.
     *
     * @return a new OrganizationDataFixture with a CEO
     */
    public static OrganizationDataFixture singleCeoOrganization() {
        return organization()
                .withEmployee(EmployeeDataFixture.ceo().withId(1));
    }

    /**
     * Creates a basic organization with CEO and some employees.
     *
     * @return a new OrganizationDataFixture with CEO and 2 employees
     */
    public static OrganizationDataFixture basicOrganization() {
        return organization()
                .withEmployee(EmployeeDataFixture.ceo().withId(1))
                .withEmployee(EmployeeDataFixture.manager().withId(2).reportingTo(1))
                .withEmployee(EmployeeDataFixture.regularEmployee().withId(3).reportingTo(2));
    }

    /**
     * Creates a flat organization with CEO and multiple direct reports.
     *
     * @return a new OrganizationDataFixture with flat structure
     */
    public static OrganizationDataFixture flatOrganization() {
        return organization()
                .withEmployee(EmployeeDataFixture.ceo().withId(1))
                .withEmployee(EmployeeDataFixture.regularEmployee().withId(2).reportingTo(1))
                .withEmployee(EmployeeDataFixture.regularEmployee().withId(3).reportingTo(1))
                .withEmployee(EmployeeDataFixture.regularEmployee().withId(4).reportingTo(1));
    }

    /**
     * Creates a deep hierarchical organization.
     *
     * @return a new OrganizationDataFixture with deep hierarchy
     */
    public static OrganizationDataFixture deepHierarchyOrganization() {
        return organization()
                .withEmployee(EmployeeDataFixture.ceo().withId(1))
                .withEmployee(EmployeeDataFixture.manager().withId(2).reportingTo(1))
                .withEmployee(EmployeeDataFixture.manager().withId(3).reportingTo(2))
                .withEmployee(EmployeeDataFixture.regularEmployee().withId(4).reportingTo(3))
                .withEmployee(EmployeeDataFixture.regularEmployee().withId(5).reportingTo(3));
    }

    /**
     * Adds an employee fixture to this organization.
     *
     * @param employeeFixture the employee fixture to add
     * @return this fixture for method chaining
     */
    public OrganizationDataFixture withEmployee(EmployeeDataFixture employeeFixture) {
        this.employeeFixtures.add(employeeFixture);
        return this;
    }

    /**
     * Adds multiple employee fixtures to this organization.
     *
     * @param fixtures the employee fixtures to add
     * @return this fixture for method chaining
     */
    public OrganizationDataFixture withEmployees(EmployeeDataFixture... fixtures) {
        for (EmployeeDataFixture fixture : fixtures) {
            this.employeeFixtures.add(fixture);
        }
        return this;
    }

    /**
     * Adds a CEO to this organization.
     *
     * @return this fixture for method chaining
     */
    public OrganizationDataFixture withCeo() {
        this.employeeFixtures.add(EmployeeDataFixture.ceo());
        return this;
    }

    /**
     * Adds a CEO with specific ID to this organization.
     *
     * @param id the CEO's ID
     * @return this fixture for method chaining
     */
    public OrganizationDataFixture withCeo(int id) {
        this.employeeFixtures.add(EmployeeDataFixture.ceo().withId(id));
        return this;
    }

    /**
     * Adds a manager to this organization.
     *
     * @param managerId the manager's manager ID
     * @return this fixture for method chaining
     */
    public OrganizationDataFixture withManager(int managerId) {
        this.employeeFixtures.add(EmployeeDataFixture.manager().reportingTo(managerId));
        return this;
    }

    /**
     * Adds multiple regular employees reporting to a manager.
     *
     * @param count the number of employees to add
     * @param managerId the manager ID they report to
     * @return this fixture for method chaining
     */
    public OrganizationDataFixture withEmployees(int count, int managerId) {
        for (int i = 0; i < count; i++) {
            this.employeeFixtures.add(EmployeeDataFixture.regularEmployee().reportingTo(managerId));
        }
        return this;
    }

    /**
     * Builds and returns the Organization object.
     *
     * @return the constructed Organization
     */
    public Organization build() {
        List<Employee> employees = new ArrayList<>();
        for (EmployeeDataFixture fixture : employeeFixtures) {
            employees.add(fixture.build());
        }
        return new Organization(employees);
    }

    /**
     * Builds the Organization and resets the ID generator.
     * Useful for test isolation.
     *
     * @return the constructed Organization
     */
    public Organization buildAndReset() {
        Organization org = build();
        EmployeeDataFixture.resetIdGenerator();
        return org;
    }
}

