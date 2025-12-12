package org.example.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Issue.Type Tests")
class IssueTypeTest {

    @Test
    @DisplayName("Should have correct description for UNDERPAID_ISSUE")
    void testUnderpaidIssue_HasCorrectDescription() {
        assertEquals("MANAGERS WHO EARN LESS THAN THEY SHOULD", Issue.Type.UNDERPAID_ISSUE.getDescription());
    }

    @Test
    @DisplayName("Should have correct description for OVERPAID_ISSUE")
    void testOverpaidIssue_HasCorrectDescription() {
        assertEquals("MANAGERS WHO EARN MORE THAN THEY SHOULD", Issue.Type.OVERPAID_ISSUE.getDescription());
    }

    @Test
    @DisplayName("Should have correct description for HIERARCHY_DEPTH_ISSUE")
    void testHierarchyDepthIssue_HasCorrectDescription() {
        assertEquals("EMPLOYEES WITH REPORTING LINE TOO LONG", Issue.Type.HIERARCHY_DEPTH_ISSUE.getDescription());
    }

    @Test
    @DisplayName("Should have three issue types")
    void testIssueTypes_HasThreeValues() {
        assertEquals(3, Issue.Type.values().length);
    }

    @Test
    @DisplayName("Should be able to get type by name")
    void testValueOf_ReturnsCorrectType() {
        assertEquals(Issue.Type.UNDERPAID_ISSUE, Issue.Type.valueOf("UNDERPAID_ISSUE"));
        assertEquals(Issue.Type.OVERPAID_ISSUE, Issue.Type.valueOf("OVERPAID_ISSUE"));
        assertEquals(Issue.Type.HIERARCHY_DEPTH_ISSUE, Issue.Type.valueOf("HIERARCHY_DEPTH_ISSUE"));
    }
}

