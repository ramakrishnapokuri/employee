package org.example.service;

import org.example.model.AnalysisReport;
import org.example.model.Employee;
import org.example.model.Issue;
import org.example.model.ReportingDepthIssue;
import org.example.model.SalaryIssue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.example.fixtures.EmployeeDataFixture.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReportPrinter Tests")
class ReportPrinterTest {

    private ReportPrinter printer;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        resetIdGenerator();
        outputStream = new ByteArrayOutputStream();
        printer = new ReportPrinter(new PrintStream(outputStream));
    }

    @Test
    @DisplayName("Should print report header")
    void testPrintReport_ContainsHeader() {
        AnalysisReport report = new AnalysisReport(
                Collections.emptyList(),
                Collections.emptyList(),
                10,
                3
        );

        printer.printReport(report);
        String output = outputStream.toString();

        assertTrue(output.contains("EMPLOYEE ORGANIZATIONAL STRUCTURE ANALYSIS REPORT"));
    }

    @Test
    @DisplayName("Should print summary section")
    void testPrintReport_ContainsSummary() {
        AnalysisReport report = new AnalysisReport(
                Collections.emptyList(),
                Collections.emptyList(),
                25,
                8
        );

        printer.printReport(report);
        String output = outputStream.toString();

        assertTrue(output.contains("SUMMARY"));
        assertTrue(output.contains("Total employees analyzed: 25"));
        assertTrue(output.contains("Total managers: 8"));
        assertTrue(output.contains("Issues found: 0"));
    }

    @Test
    @DisplayName("Should print underpaid managers section")
    void testPrintReport_ContainsUnderpaidSection() {
        Employee manager = employee()
                .withId(101)
                .withFirstName("John")
                .withLastName("Manager")
                .withSalary(50000)
                .build();

        SalaryIssue underpaidIssue = new SalaryIssue(
                manager,
                Issue.Type.UNDERPAID_ISSUE,
                60000,  // subordinates avg
                72000,  // min expected
                90000   // max expected
        );

        AnalysisReport report = new AnalysisReport(
                List.of(underpaidIssue),
                Collections.emptyList(),
                10,
                3
        );

        printer.printReport(report);
        String output = outputStream.toString();

        assertTrue(output.contains("MANAGERS WHO EARN LESS THAN THEY SHOULD"));
        assertTrue(output.contains("John Manager"));
        assertTrue(output.contains("ID: 101"));
        assertTrue(output.contains("Current salary:"));
        assertTrue(output.contains("Underpaid by:"));
    }

    @Test
    @DisplayName("Should print overpaid managers section")
    void testPrintReport_ContainsOverpaidSection() {
        Employee manager = employee()
                .withId(102)
                .withFirstName("Jane")
                .withLastName("Boss")
                .withSalary(120000)
                .build();

        SalaryIssue overpaidIssue = new SalaryIssue(
                manager,
                Issue.Type.OVERPAID_ISSUE,
                60000,  // subordinates avg
                72000,  // min expected
                90000   // max expected
        );

        AnalysisReport report = new AnalysisReport(
                List.of(overpaidIssue),
                Collections.emptyList(),
                10,
                3
        );

        printer.printReport(report);
        String output = outputStream.toString();

        assertTrue(output.contains("MANAGERS WHO EARN MORE THAN THEY SHOULD"));
        assertTrue(output.contains("Jane Boss"));
        assertTrue(output.contains("Overpaid by:"));
    }

    @Test
    @DisplayName("Should print reporting line issues section")
    void testPrintReport_ContainsReportingLineSection() {
        Employee deepEmployee = employee()
                .withId(200)
                .withFirstName("Deep")
                .withLastName("Worker")
                .withSalary(40000)
                .build();

        Employee mgr1 = employee().withId(1).withFirstName("Mgr").withLastName("One").build();
        Employee mgr2 = employee().withId(2).withFirstName("Mgr").withLastName("Two").build();
        Employee mgr3 = employee().withId(3).withFirstName("Mgr").withLastName("Three").build();
        Employee mgr4 = employee().withId(4).withFirstName("Mgr").withLastName("Four").build();
        Employee ceoEmp = ceo().withId(5).build();

        List<Employee> chain = List.of(mgr1, mgr2, mgr3, mgr4, ceoEmp);

        ReportingDepthIssue depthIssue = new ReportingDepthIssue(deepEmployee, 5, chain);

        AnalysisReport report = new AnalysisReport(
                Collections.emptyList(),
                List.of(depthIssue),
                20,
                6
        );

        printer.printReport(report);
        String output = outputStream.toString();

        assertTrue(output.contains("EMPLOYEES WITH REPORTING LINE TOO LONG"));
        assertTrue(output.contains("Deep Worker"));
        assertTrue(output.contains("exceeds by 1"));
        assertTrue(output.contains("Chain:"));
    }

    @Test
    @DisplayName("Should print 'No issues found' when sections are empty")
    void testPrintReport_NoIssuesMessage() {
        AnalysisReport report = new AnalysisReport(
                Collections.emptyList(),
                Collections.emptyList(),
                10,
                3
        );

        printer.printReport(report);
        String output = outputStream.toString();

        // Should contain "No issues found" for each section
        assertTrue(output.contains("No issues found"));
    }

    @Test
    @DisplayName("Should print footer with issues message when issues exist")
    void testPrintReport_FooterWithIssues() {
        Employee manager = employee()
                .withId(101)
                .withFirstName("Test")
                .withLastName("Manager")
                .withSalary(50000)
                .build();

        SalaryIssue issue = new SalaryIssue(
                manager,
                Issue.Type.UNDERPAID_ISSUE,
                60000, 72000, 90000
        );

        AnalysisReport report = new AnalysisReport(
                List.of(issue),
                Collections.emptyList(),
                10,
                3
        );

        printer.printReport(report);
        String output = outputStream.toString();

        assertTrue(output.contains("Please review the issues above"));
    }

    @Test
    @DisplayName("Should print footer with no issues message when clean")
    void testPrintReport_FooterWithNoIssues() {
        AnalysisReport report = new AnalysisReport(
                Collections.emptyList(),
                Collections.emptyList(),
                10,
                3
        );

        printer.printReport(report);
        String output = outputStream.toString();

        assertTrue(output.contains("No issues found in the organizational structure"));
    }

    @Test
    @DisplayName("Should use default constructor with System.out")
    void testDefaultConstructor() {
        ReportPrinter defaultPrinter = new ReportPrinter();
        assertNotNull(defaultPrinter);
    }

    @Test
    @DisplayName("Should print multiple underpaid managers")
    void testPrintReport_MultipleUnderpaidManagers() {
        List<SalaryIssue> issues = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            Employee manager = employee()
                    .withId(100 + i)
                    .withFirstName("Manager" + i)
                    .withLastName("Test")
                    .withSalary(50000)
                    .build();
            issues.add(new SalaryIssue(manager, Issue.Type.UNDERPAID_ISSUE, 60000, 72000, 90000));
        }

        AnalysisReport report = new AnalysisReport(issues, Collections.emptyList(), 20, 5);

        printer.printReport(report);
        String output = outputStream.toString();

        assertTrue(output.contains("Manager1 Test"));
        assertTrue(output.contains("Manager2 Test"));
        assertTrue(output.contains("Manager3 Test"));
    }

    @Test
    @DisplayName("Should print multiple reporting depth issues")
    void testPrintReport_MultipleReportingDepthIssues() {
        List<ReportingDepthIssue> issues = new ArrayList<>();
        List<Employee> chain = List.of(
                employee().withId(10).withFirstName("Mgr").withLastName("A").build(),
                ceo().withId(1).build()
        );

        for (int i = 1; i <= 2; i++) {
            Employee emp = employee()
                    .withId(200 + i)
                    .withFirstName("Deep" + i)
                    .withLastName("Worker")
                    .build();
            issues.add(new ReportingDepthIssue(emp, 5, chain));
        }

        AnalysisReport report = new AnalysisReport(Collections.emptyList(), issues, 20, 5);

        printer.printReport(report);
        String output = outputStream.toString();

        assertTrue(output.contains("Deep1 Worker"));
        assertTrue(output.contains("Deep2 Worker"));
    }
}

