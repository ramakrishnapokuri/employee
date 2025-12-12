package org.example.service;

import org.example.model.Employee;
import org.example.model.Issue;
import org.example.model.SalaryIssue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.example.fixtures.EmployeeDataFixture.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the SalaryAnalyzer service.
 *
 * Business Rules:
 * - Managers should earn at least 20% more than subordinates' average
 * - Managers should earn no more than 50% more than subordinates' average
 */
@DisplayName("Salary Analyzer Tests")
class SalaryAnalyzerTest {

    private SalaryAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new SalaryAnalyzer();
        resetIdGenerator();
    }

    @Test
    @DisplayName("Should detect underpaid manager")
    void shouldDetectUnderpaidManager() {
        // Manager with salary 50000, subordinates avg 50000
        // Expected min: 50000 * 1.20 = 60000
        // Underpaid by: 60000 - 50000 = 10000
        Employee mgrEmp = employee()
                .withId(1)
                .withFirstName("John")
                .withLastName("Manager")
                .withSalary(50000)
                .withManagerId(null)
                .build();
        Employee sub1 = regularEmployee().withId(2).withSalary(50000).reportingTo(1).build();
        Employee sub2 = regularEmployee().withId(3).withSalary(50000).reportingTo(1).build();

        sub1.setManager(mgrEmp);
        sub2.setManager(mgrEmp);

        SalaryIssue issue = analyzer.analyzeManagerSalary(mgrEmp);

        assertNotNull(issue);
        assertEquals(Issue.Type.UNDERPAID_ISSUE, issue.getType());
        assertEquals(mgrEmp, issue.getManager());
        assertEquals(50000, issue.getCurrentSalary());
        assertEquals(50000, issue.getSubordinatesAverageSalary());
        assertEquals(60000, issue.getExpectedMinSalary());
        assertEquals(75000, issue.getExpectedMaxSalary());
        assertEquals(10000, issue.getDifference());
    }

    @Test
    @DisplayName("Should detect overpaid manager")
    void shouldDetectOverpaidManager() {
        // Manager with salary 100000, subordinates avg 50000
        // Expected max: 50000 * 1.50 = 75000
        // Overpaid by: 100000 - 75000 = 25000
        Employee mgrEmp = employee()
                .withId(1)
                .withFirstName("John")
                .withLastName("Manager")
                .withSalary(100000)
                .withManagerId(null)
                .build();
        Employee sub1 = regularEmployee().withId(2).withSalary(50000).reportingTo(1).build();
        Employee sub2 = regularEmployee().withId(3).withSalary(50000).reportingTo(1).build();

        sub1.setManager(mgrEmp);
        sub2.setManager(mgrEmp);

        SalaryIssue issue = analyzer.analyzeManagerSalary(mgrEmp);

        assertNotNull(issue);
        assertEquals(Issue.Type.OVERPAID_ISSUE, issue.getType());
        assertEquals(25000, issue.getDifference());
    }

    @Test
    @DisplayName("Should not detect issue when salary is within range")
    void shouldNotDetectIssueWhenSalaryWithinRange() {
        // Manager with salary 65000, subordinates avg 50000
        // Expected range: 60000 - 75000
        Employee mgrEmp = employee()
                .withId(1)
                .withFirstName("John")
                .withLastName("Manager")
                .withSalary(65000)
                .withManagerId(null)
                .build();
        Employee sub1 = regularEmployee().withId(2).withSalary(50000).reportingTo(1).build();
        Employee sub2 = regularEmployee().withId(3).withSalary(50000).reportingTo(1).build();

        sub1.setManager(mgrEmp);
        sub2.setManager(mgrEmp);

        SalaryIssue issue = analyzer.analyzeManagerSalary(mgrEmp);

        assertNull(issue);
    }

    @Test
    @DisplayName("Should accept salary at minimum threshold (exactly 20% more)")
    void shouldAcceptSalaryAtMinimumThreshold() {
        // Manager with salary 60000, subordinates avg 50000
        // Expected min: 50000 * 1.20 = 60000 (exactly at threshold)
        Employee mgrEmp = employee()
                .withId(1)
                .withFirstName("John")
                .withLastName("Manager")
                .withSalary(60000)
                .withManagerId(null)
                .build();
        Employee sub1 = regularEmployee().withId(2).withSalary(50000).reportingTo(1).build();

        sub1.setManager(mgrEmp);

        SalaryIssue issue = analyzer.analyzeManagerSalary(mgrEmp);

        assertNull(issue);
    }

    @Test
    @DisplayName("Should accept salary at maximum threshold (exactly 50% more)")
    void shouldAcceptSalaryAtMaximumThreshold() {
        // Manager with salary 75000, subordinates avg 50000
        // Expected max: 50000 * 1.50 = 75000 (exactly at threshold)
        Employee mgrEmp = employee()
                .withId(1)
                .withFirstName("John")
                .withLastName("Manager")
                .withSalary(75000)
                .withManagerId(null)
                .build();
        Employee sub1 = regularEmployee().withId(2).withSalary(50000).reportingTo(1).build();

        sub1.setManager(mgrEmp);

        SalaryIssue issue = analyzer.analyzeManagerSalary(mgrEmp);

        assertNull(issue);
    }

    @Test
    @DisplayName("Should not analyze non-managers")
    void shouldNotAnalyzeNonManagers() {
        Employee emp = employee()
                .withId(1)
                .withFirstName("John")
                .withLastName("Worker")
                .withSalary(50000)
                .withManagerId(null)
                .build();
        // No subordinates

        SalaryIssue issue = analyzer.analyzeManagerSalary(emp);

        assertNull(issue);
    }

    @Test
    @DisplayName("Should analyze all managers in list")
    void shouldAnalyzeAllManagersInList() {
        // Create hierarchy with 2 managers (mgr1 and mgr2), both underpaid
        // CEO is also a manager (has subordinates) and will be checked too
        List<Employee> employees = buildList(
                ceo().withId(1).withSalary(100000),   // Avg of subs = 50000, overpaid (needs 60000-75000)
                employee().withId(2).withFirstName("Mgr").withLastName("One").withSalary(50000).reportingTo(1),
                employee().withId(3).withFirstName("Mgr").withLastName("Two").withSalary(50000).reportingTo(1),
                regularEmployee().withId(4).withSalary(50000).reportingTo(2),
                regularEmployee().withId(5).withSalary(50000).reportingTo(3)
        );

        // Build hierarchy
        employees.get(1).setManager(employees.get(0));
        employees.get(2).setManager(employees.get(0));
        employees.get(3).setManager(employees.get(1));
        employees.get(4).setManager(employees.get(2));

        List<SalaryIssue> issues = analyzer.analyzeSalaries(employees);

        // CEO, mgr1, and mgr2 are all managers - all have salary issues
        assertEquals(3, issues.size());
    }

    @Test
    @DisplayName("Should only consider direct subordinates, not indirect")
    void shouldOnlyConsiderDirectSubordinates() {
        // CEO -> Manager -> Worker
        // Manager's salary should only be compared to Worker, not CEO
        List<Employee> employees = buildList(
                ceo().withId(1).withSalary(200000),
                employee().withId(2).withFirstName("Manager").withLastName("Mid").withSalary(60000).reportingTo(1),
                regularEmployee().withId(3).withSalary(50000).reportingTo(2)
        );

        // Build hierarchy
        employees.get(1).setManager(employees.get(0));
        employees.get(2).setManager(employees.get(1));

        // Manager earns exactly 20% more than worker - should be OK
        SalaryIssue issue = analyzer.analyzeManagerSalary(employees.get(1));

        assertNull(issue);
    }
}
