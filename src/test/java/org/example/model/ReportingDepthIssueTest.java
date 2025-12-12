package org.example.model;

import org.example.config.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.example.fixtures.EmployeeDataFixture.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReportingDepthIssue Tests")
class ReportingDepthIssueTest {

    @BeforeEach
    void setUp() {
        resetIdGenerator();
    }

    @Test
    @DisplayName("Should create issue with correct properties")
    void testConstructor_SetsPropertiesCorrectly() {
        Employee emp = employee()
                .withId(100)
                .withFirstName("Deep")
                .withLastName("Worker")
                .build();

        List<Employee> chain = List.of(
                employee().withId(1).withFirstName("Mgr").withLastName("One").build(),
                employee().withId(2).withFirstName("Mgr").withLastName("Two").build(),
                ceo().withId(3).build()
        );

        ReportingDepthIssue issue = new ReportingDepthIssue(emp, 5, chain);

        assertEquals(emp, issue.getEmployee());
        assertEquals(5, issue.getActualDepth());
        assertEquals(1, issue.getExcessDepth()); // 5 - 4 (MAX_ALLOWED_DEPTH)
        assertEquals(3, issue.getReportingChain().size());
    }

    @Test
    @DisplayName("Should calculate excess depth correctly")
    void testGetExcessDepth_CalculatesCorrectly() {
        Employee emp = employee().withId(1).build();
        List<Employee> chain = List.of(ceo().withId(2).build());

        ReportingDepthIssue issueDepth5 = new ReportingDepthIssue(emp, 5, chain);
        ReportingDepthIssue issueDepth6 = new ReportingDepthIssue(emp, 6, chain);
        ReportingDepthIssue issueDepth10 = new ReportingDepthIssue(emp, 10, chain);

        assertEquals(1, issueDepth5.getExcessDepth());
        assertEquals(2, issueDepth6.getExcessDepth());
        assertEquals(6, issueDepth10.getExcessDepth());
    }

    @Test
    @DisplayName("Should return unmodifiable reporting chain")
    void testGetReportingChain_ReturnsUnmodifiable() {
        Employee emp = employee().withId(1).build();
        List<Employee> chain = List.of(
                employee().withId(2).build(),
                ceo().withId(3).build()
        );

        ReportingDepthIssue issue = new ReportingDepthIssue(emp, 5, chain);

        assertThrows(UnsupportedOperationException.class, () -> {
            issue.getReportingChain().add(employee().withId(99).build());
        });
    }

    @Test
    @DisplayName("Should return correct issue type")
    void testGetType_ReturnsHierarchyDepthIssue() {
        Employee emp = employee().withId(1).build();
        List<Employee> chain = List.of(ceo().withId(2).build());

        ReportingDepthIssue issue = new ReportingDepthIssue(emp, 5, chain);

        assertEquals(Issue.Type.HIERARCHY_DEPTH_ISSUE, issue.getType());
    }

    @Test
    @DisplayName("Should return description matching toString")
    void testGetDescription_MatchesToString() {
        Employee emp = employee()
                .withId(100)
                .withFirstName("Test")
                .withLastName("Employee")
                .build();
        List<Employee> chain = List.of(ceo().withId(1).build());

        ReportingDepthIssue issue = new ReportingDepthIssue(emp, 5, chain);

        assertEquals(issue.toString(), issue.getDescription());
    }

    @Test
    @DisplayName("Should provide meaningful toString")
    void testToString_ContainsRelevantInfo() {
        Employee emp = employee()
                .withId(123)
                .withFirstName("Deep")
                .withLastName("Worker")
                .build();
        List<Employee> chain = List.of(ceo().withId(1).build());

        ReportingDepthIssue issue = new ReportingDepthIssue(emp, 6, chain);
        String str = issue.toString();

        assertTrue(str.contains("Deep Worker"));
        assertTrue(str.contains("123"));
        assertTrue(str.contains("too long"));
        assertTrue(str.contains("2")); // excess
        assertTrue(str.contains("6")); // actual depth
        assertTrue(str.contains("4")); // max allowed
    }

    @Test
    @DisplayName("Should use config max depth correctly")
    void testMaxAllowedDepth_IsCorrect() {
        assertEquals(4, AppConfig.get().getMaxReportingDepth());
    }
}

