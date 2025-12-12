package org.example.service;

import org.example.model.AnalysisReport;
import org.example.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.example.fixtures.EmployeeDataFixture.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the OrganizationAnalyzer service.
 */
@DisplayName("Organization Analyzer Tests")
class OrganizationAnalyzerTest {

    private OrganizationAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new OrganizationAnalyzer();
        resetIdGenerator();
    }

    @Test
    @DisplayName("Should produce report with no issues for valid organization")
    void shouldProduceReportWithNoIssues() {
        // CEO with properly paid subordinates and short reporting lines
        // CEO's avg subordinate salary: 72000, so CEO needs 86400-108000
        // Manager's avg subordinate salary: 60000, so manager needs 72000-90000
        Employee ceoEmp = ceo().withId(1).withSalary(95000).build();  // Within 86400-108000 range
        Employee mgrEmp = manager().withId(2).withSalary(72000).reportingTo(1).build();  // 20% above 60000 avg
        Employee worker1 = regularEmployee().withId(3).withSalary(55000).reportingTo(2).build();
        Employee worker2 = regularEmployee().withId(4).withSalary(65000).reportingTo(2).build();

        mgrEmp.setManager(ceoEmp);
        worker1.setManager(mgrEmp);
        worker2.setManager(mgrEmp);

        List<Employee> employees = buildList(
                ceo().withId(1).withSalary(95000),
                manager().withId(2).withSalary(72000).reportingTo(1),
                regularEmployee().withId(3).withSalary(55000).reportingTo(2),
                regularEmployee().withId(4).withSalary(65000).reportingTo(2)
        );

        // Build relationships
        employees.get(1).setManager(employees.get(0));
        employees.get(2).setManager(employees.get(1));
        employees.get(3).setManager(employees.get(1));

        AnalysisReport report = analyzer.analyze(employees);

        assertFalse(report.hasIssues());
        assertEquals(0, report.getTotalIssueCount());
        assertEquals(4, report.getTotalEmployees());
        assertEquals(2, report.getTotalManagers()); // CEO and Manager
    }

    @Test
    @DisplayName("Should find both salary and reporting line issues")
    void shouldFindBothTypeOfIssues() {
        // Create underpaid manager and deep hierarchy
        List<Employee> employees = buildList(
                ceo().withId(1).withSalary(200000),
                manager().withId(2).withSalary(50000).reportingTo(1),   // Underpaid
                manager().withId(3).withSalary(60000).reportingTo(2),
                manager().withId(4).withSalary(50000).reportingTo(3),
                manager().withId(5).withSalary(50000).reportingTo(4),
                manager().withId(6).withSalary(50000).reportingTo(5),
                regularEmployee().withId(7).withSalary(40000).reportingTo(6)  // Depth 6 > 4
        );

        // Build hierarchy
        employees.get(1).setManager(employees.get(0));
        employees.get(2).setManager(employees.get(1));
        employees.get(3).setManager(employees.get(2));
        employees.get(4).setManager(employees.get(3));
        employees.get(5).setManager(employees.get(4));
        employees.get(6).setManager(employees.get(5));

        AnalysisReport report = analyzer.analyze(employees);

        assertTrue(report.hasIssues());
        assertFalse(report.getSalaryIssues().isEmpty());
        assertFalse(report.getReportingLineIssues().isEmpty());
    }

    @Test
    @DisplayName("Should correctly count managers")
    void shouldCorrectlyCountManagers() {
        List<Employee> employees = buildList(
                ceo().withId(1).withSalary(200000),
                manager().withId(2).withSalary(100000).reportingTo(1),
                regularEmployee().withId(3).withSalary(50000).reportingTo(2),
                regularEmployee().withId(4).withSalary(50000).reportingTo(2),
                regularEmployee().withId(5).withSalary(50000).reportingTo(1)  // Direct to CEO
        );

        // Build hierarchy
        employees.get(1).setManager(employees.get(0));
        employees.get(2).setManager(employees.get(1));
        employees.get(3).setManager(employees.get(1));
        employees.get(4).setManager(employees.get(0));

        AnalysisReport report = analyzer.analyze(employees);

        assertEquals(5, report.getTotalEmployees());
        assertEquals(2, report.getTotalManagers());  // CEO and manager have subordinates
    }

    @Test
    @DisplayName("Should separate underpaid and overpaid managers in report")
    void shouldSeparateUnderpaidAndOverpaid() {
        // CEO avg subordinate = 75000, needs 90000-112500
        // Setting CEO salary to 100000 (within range)
        List<Employee> employees = buildList(
                ceo().withId(1).withSalary(100000),
                manager().withId(2).withFirstName("Underpaid").withSalary(50000).reportingTo(1),  // Earns same as subs
                manager().withId(3).withFirstName("Overpaid").withSalary(100000).reportingTo(1),   // Earns 2x subs
                regularEmployee().withId(4).withSalary(50000).reportingTo(2),
                regularEmployee().withId(5).withSalary(50000).reportingTo(3)
        );

        // Build hierarchy
        Employee ceoEmp = employees.get(0);
        Employee underpaidMgr = employees.get(1);
        Employee overpaidMgr = employees.get(2);
        Employee sub1 = employees.get(3);
        Employee sub2 = employees.get(4);

        underpaidMgr.setManager(ceoEmp);
        overpaidMgr.setManager(ceoEmp);
        sub1.setManager(underpaidMgr);
        sub2.setManager(overpaidMgr);

        AnalysisReport report = analyzer.analyze(employees);

        assertEquals(1, report.getUnderpaidManagers().size());
        assertEquals(1, report.getOverpaidManagers().size());
        assertEquals(underpaidMgr, report.getUnderpaidManagers().get(0).getManager());
        assertEquals(overpaidMgr, report.getOverpaidManagers().get(0).getManager());
    }

    @Test
    @DisplayName("Should create analyzer with custom dependencies")
    void shouldCreateAnalyzerWithCustomDependencies() {
        SalaryAnalyzer salaryAnalyzer = new SalaryAnalyzer();
        ReportingDepthAnalyzer reportingDepthAnalyzer = new ReportingDepthAnalyzer();

        OrganizationAnalyzer customAnalyzer = new OrganizationAnalyzer(salaryAnalyzer, reportingDepthAnalyzer);

        assertNotNull(customAnalyzer);
    }

    @Test
    @DisplayName("Should handle empty employee list")
    void shouldHandleEmptyEmployeeList() {
        List<Employee> employees = Collections.emptyList();

        AnalysisReport report = analyzer.analyze(employees);

        assertEquals(0, report.getTotalEmployees());
        assertEquals(0, report.getTotalManagers());
        assertFalse(report.hasIssues());
    }

    @Test
    @DisplayName("Should handle single employee (CEO only)")
    void shouldHandleSingleEmployee() {
        List<Employee> employees = buildList(ceo().withId(1).withSalary(100000));

        AnalysisReport report = analyzer.analyze(employees);

        assertEquals(1, report.getTotalEmployees());
        assertEquals(0, report.getTotalManagers());  // CEO has no subordinates
        assertFalse(report.hasIssues());
    }
}
