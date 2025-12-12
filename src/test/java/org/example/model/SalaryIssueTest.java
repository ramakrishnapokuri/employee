package org.example.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.example.fixtures.EmployeeDataFixture.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SalaryIssue Tests")
class SalaryIssueTest {

    @BeforeEach
    void setUp() {
        resetIdGenerator();
    }

    @Test
    @DisplayName("Should create underpaid issue with correct properties")
    void testConstructor_UnderpaidIssue_SetsPropertiesCorrectly() {
        Employee manager = employee()
                .withId(100)
                .withFirstName("John")
                .withLastName("Manager")
                .withSalary(50000)
                .build();

        SalaryIssue issue = new SalaryIssue(
                manager,
                Issue.Type.UNDERPAID_ISSUE,
                60000,  // subordinates avg
                72000,  // min expected (60000 * 1.2)
                90000   // max expected (60000 * 1.5)
        );

        assertEquals(manager, issue.getManager());
        assertEquals(Issue.Type.UNDERPAID_ISSUE, issue.getType());
        assertEquals(50000, issue.getCurrentSalary());
        assertEquals(60000, issue.getSubordinatesAverageSalary());
        assertEquals(72000, issue.getExpectedMinSalary());
        assertEquals(90000, issue.getExpectedMaxSalary());
        assertEquals(22000, issue.getDifference()); // 72000 - 50000
    }

    @Test
    @DisplayName("Should create overpaid issue with correct properties")
    void testConstructor_OverpaidIssue_SetsPropertiesCorrectly() {
        Employee manager = employee()
                .withId(100)
                .withFirstName("Jane")
                .withLastName("Boss")
                .withSalary(100000)
                .build();

        SalaryIssue issue = new SalaryIssue(
                manager,
                Issue.Type.OVERPAID_ISSUE,
                50000,  // subordinates avg
                60000,  // min expected
                75000   // max expected
        );

        assertEquals(Issue.Type.OVERPAID_ISSUE, issue.getType());
        assertEquals(100000, issue.getCurrentSalary());
        assertEquals(50000, issue.getSubordinatesAverageSalary());
        assertEquals(25000, issue.getDifference()); // 100000 - 75000
    }

    @Test
    @DisplayName("Should calculate difference correctly for underpaid")
    void testGetDifference_Underpaid_CalculatesFromMinExpected() {
        Employee manager = employee().withId(1).withSalary(40000).build();

        // Min expected: 60000, Current: 40000, Difference: 20000
        SalaryIssue issue = new SalaryIssue(
                manager,
                Issue.Type.UNDERPAID_ISSUE,
                50000, 60000, 75000
        );

        assertEquals(20000, issue.getDifference());
    }

    @Test
    @DisplayName("Should calculate difference correctly for overpaid")
    void testGetDifference_Overpaid_CalculatesFromMaxExpected() {
        Employee manager = employee().withId(1).withSalary(100000).build();

        // Max expected: 75000, Current: 100000, Difference: 25000
        SalaryIssue issue = new SalaryIssue(
                manager,
                Issue.Type.OVERPAID_ISSUE,
                50000, 60000, 75000
        );

        assertEquals(25000, issue.getDifference());
    }

    @Test
    @DisplayName("Should return description matching toString")
    void testGetDescription_MatchesToString() {
        Employee manager = employee()
                .withId(123)
                .withFirstName("Test")
                .withLastName("Manager")
                .withSalary(50000)
                .build();

        SalaryIssue issue = new SalaryIssue(
                manager,
                Issue.Type.UNDERPAID_ISSUE,
                60000, 72000, 90000
        );

        assertEquals(issue.toString(), issue.getDescription());
    }

    @Test
    @DisplayName("Should provide meaningful toString for underpaid")
    void testToString_Underpaid_ContainsRelevantInfo() {
        Employee manager = employee()
                .withId(456)
                .withFirstName("Under")
                .withLastName("Paid")
                .withSalary(50000)
                .build();

        SalaryIssue issue = new SalaryIssue(
                manager,
                Issue.Type.UNDERPAID_ISSUE,
                60000, 72000, 90000
        );

        String str = issue.toString();

        assertTrue(str.contains("Under Paid"));
        assertTrue(str.contains("456"));
        assertTrue(str.contains("earns less than they should"));
    }

    @Test
    @DisplayName("Should provide meaningful toString for overpaid")
    void testToString_Overpaid_ContainsRelevantInfo() {
        Employee manager = employee()
                .withId(789)
                .withFirstName("Over")
                .withLastName("Paid")
                .withSalary(100000)
                .build();

        SalaryIssue issue = new SalaryIssue(
                manager,
                Issue.Type.OVERPAID_ISSUE,
                50000, 60000, 75000
        );

        String str = issue.toString();

        assertTrue(str.contains("Over Paid"));
        assertTrue(str.contains("789"));
        assertTrue(str.contains("earns more than they should"));
    }

    @Test
    @DisplayName("Should get current salary from manager")
    void testGetCurrentSalary_ReturnsManagerSalary() {
        Employee manager = employee().withId(1).withSalary(75000).build();

        SalaryIssue issue = new SalaryIssue(
                manager,
                Issue.Type.UNDERPAID_ISSUE,
                80000, 96000, 120000
        );

        assertEquals(75000, issue.getCurrentSalary());
        assertEquals(manager.getSalary(), issue.getCurrentSalary());
    }
}

