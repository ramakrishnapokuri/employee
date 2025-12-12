package org.example.model;

import org.example.config.AppConfig;

import java.util.Collections;
import java.util.List;

/**
 * Someone with too many managers between them and the CEO.
 * Keeps the full chain so we can show it in the report.
 */
public class ReportingDepthIssue implements Issue {
    
    private final Employee employee;
    private final int actualDepth;
    private final int maxAllowedDepth;
    private final int excessDepth;
    private final List<Employee> reportingChain;
    
    public ReportingDepthIssue(Employee employee, int actualDepth, List<Employee> reportingChain, int maxAllowedDepth) {
        this.employee = employee;
        this.actualDepth = actualDepth;
        this.maxAllowedDepth = maxAllowedDepth;
        this.excessDepth = actualDepth - maxAllowedDepth;
        this.reportingChain = reportingChain;
    }
    
    /** Convenience constructor using config default. */
    public ReportingDepthIssue(Employee employee, int actualDepth, List<Employee> reportingChain) {
        this(employee, actualDepth, reportingChain, AppConfig.get().getMaxReportingDepth());
    }
    
    public Employee getEmployee() {
        return employee;
    }
    
    /** How deep they actually are. */
    public int getActualDepth() {
        return actualDepth;
    }
    
    /** How many levels over the limit. */
    public int getExcessDepth() {
        return excessDepth;
    }
    
    /** The full chain from this person's manager up to the CEO. */
    public List<Employee> getReportingChain() {
        return Collections.unmodifiableList(reportingChain);
    }
    
    /** The configured max depth. */
    public int getMaxAllowedDepth() {
        return maxAllowedDepth;
    }
    
    @Override
    public String toString() {
        return String.format("%s (ID: %d) has reporting line too long by %d (depth: %d, max allowed: %d)",
                employee.getFullName(), employee.getId(), excessDepth, actualDepth, maxAllowedDepth);
    }

    @Override
    public Type getType() {
        return Type.HIERARCHY_DEPTH_ISSUE;
    }

    @Override
    public String getDescription() {
        return toString();
    }
}

