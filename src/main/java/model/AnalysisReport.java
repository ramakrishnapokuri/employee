package org.example.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds all the issues found during analysis.
 * 
 * Splits salary issues into underpaid/overpaid buckets upfront
 * so we don't have to filter every time someone asks.
 */
public class AnalysisReport {
    
    private final List<SalaryIssue> underpaidManagers;
    private final List<SalaryIssue> overpaidManagers;
    private final List<ReportingDepthIssue> reportingDepthIssues;
    private final int totalEmployees;
    private final int totalManagers;
    
    /** Builds the report and pre-sorts salary issues into buckets. */
    public AnalysisReport(List<SalaryIssue> salaryIssues,
                          List<ReportingDepthIssue> reportingDepthIssues,
                          int totalEmployees,
                          int totalManagers) {
        // Partition salary issues once at construction - O(n) single pass
        List<SalaryIssue> underpaid = new ArrayList<>();
        List<SalaryIssue> overpaid = new ArrayList<>();
        for (SalaryIssue issue : salaryIssues) {
            if (issue.getType() == Issue.Type.UNDERPAID_ISSUE) {
                underpaid.add(issue);
            } else {
                overpaid.add(issue);
            }
        }
        this.underpaidManagers = Collections.unmodifiableList(underpaid);
        this.overpaidManagers = Collections.unmodifiableList(overpaid);
        this.reportingDepthIssues = Collections.unmodifiableList(new ArrayList<>(reportingDepthIssues));
        this.totalEmployees = totalEmployees;
        this.totalManagers = totalManagers;
    }
    
    /** All salary issues combined. */
    public List<SalaryIssue> getSalaryIssues() {
        List<SalaryIssue> all = new ArrayList<>(underpaidManagers.size() + overpaidManagers.size());
        all.addAll(underpaidManagers);
        all.addAll(overpaidManagers);
        return Collections.unmodifiableList(all);
    }
    
    /** Managers earning less than they should. */
    public List<SalaryIssue> getUnderpaidManagers() {
        return underpaidManagers;
    }
    
    /** Managers earning more than they should. */
    public List<SalaryIssue> getOverpaidManagers() {
        return overpaidManagers;
    }
    
    /** People with too many managers above them. */
    public List<ReportingDepthIssue> getReportingLineIssues() {
        return reportingDepthIssues;
    }
    
    public int getTotalEmployees() {
        return totalEmployees;
    }
    
    public int getTotalManagers() {
        return totalManagers;
    }
    
    /** True if we found any problems at all. */
    public boolean hasIssues() {
        return !underpaidManagers.isEmpty() || !overpaidManagers.isEmpty() || !reportingDepthIssues.isEmpty();
    }
    
    public int getTotalIssueCount() {
        return underpaidManagers.size() + overpaidManagers.size() + reportingDepthIssues.size();
    }
}

