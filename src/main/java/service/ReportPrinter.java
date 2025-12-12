package org.example.service;

import org.example.config.AppConfig;
import org.example.model.AnalysisReport;
import org.example.model.Issue;
import org.example.model.ReportingDepthIssue;
import org.example.model.SalaryIssue;

import java.io.PrintStream;
import java.util.List;

/**
 * Prints the analysis report to stdout (or wherever you point it).
 */
public class ReportPrinter {
    
    private static final String SEPARATOR = "═".repeat(70);
    private static final String SECTION_SEPARATOR = "─".repeat(70);
    
    private final PrintStream output;
    private final AppConfig config;
    
    /** Print to a specific stream (useful for tests). */
    public ReportPrinter(PrintStream output) {
        this(output, AppConfig.get());
    }
    
    public ReportPrinter(PrintStream output, AppConfig config) {
        this.output = output;
        this.config = config;
    }
    
    /** Default: prints to stdout. */
    public ReportPrinter() {
        this(System.out);
    }
    
    /** Print the whole report with all sections. */
    public void printReport(AnalysisReport report) {
        printHeader();
        printSummary(report);
        printUnderpaidManagers(report.getUnderpaidManagers());
        printOverpaidManagers(report.getOverpaidManagers());
        printReportingLineIssues(report.getReportingLineIssues());
        printFooter(report);
    }
    
    private void printHeader() {
        output.println();
        output.println(SEPARATOR);
        output.println("        EMPLOYEE ORGANIZATIONAL STRUCTURE ANALYSIS REPORT");
        output.println(SEPARATOR);
        output.println();
    }
    
    private void printSummary(AnalysisReport report) {
        output.println("SUMMARY");
        output.println(SECTION_SEPARATOR);
        output.printf("Total employees analyzed: %d%n", report.getTotalEmployees());
        output.printf("Total managers: %d%n", report.getTotalManagers());
        output.printf("Issues found: %d%n", report.getTotalIssueCount());
        output.println();
    }
    
    private void printUnderpaidManagers(List<SalaryIssue> issues) {
        printSalarySection(
                "MANAGERS WHO EARN LESS THAN THEY SHOULD",
                "at least", config.getMinSalaryPercent(), issues);
    }
    
    private void printOverpaidManagers(List<SalaryIssue> issues) {
        printSalarySection(
                "MANAGERS WHO EARN MORE THAN THEY SHOULD",
                "no more than", config.getMaxSalaryPercent(), issues);
    }
    
    private void printSalarySection(String title, String constraint, int percent, List<SalaryIssue> issues) {
        output.println(title);
        output.println(SECTION_SEPARATOR);
        output.printf("(Managers should earn %s %d%% more than their direct subordinates' average)%n",
                constraint, percent);
        output.println();
        
        if (issues.isEmpty()) {
            output.println("  No issues found.");
        } else {
            issues.forEach(this::printSalaryIssue);
        }
        output.println();
    }
    
    private void printSalaryIssue(SalaryIssue issue) {
        output.printf("  • %s (ID: %d)%n", 
                issue.getManager().getFullName(), 
                issue.getManager().getId());
        output.printf("      Current salary: $%,.2f%n", issue.getCurrentSalary());
        output.printf("      Subordinates' average: $%,.2f%n", issue.getSubordinatesAverageSalary());
        output.printf("      Expected range: $%,.2f - $%,.2f%n", 
                issue.getExpectedMinSalary(),
                issue.getExpectedMaxSalary());
        
        if (issue.getType() == Issue.Type.UNDERPAID_ISSUE) {
            output.printf("      Underpaid by: $%,.2f%n", issue.getDifference());
        } else {
            output.printf("      Overpaid by: $%,.2f%n", issue.getDifference());
        }
        output.println();
    }
    
    private void printReportingLineIssues(List<ReportingDepthIssue> issues) {
        output.println("EMPLOYEES WITH REPORTING LINE TOO LONG");
        output.println(SECTION_SEPARATOR);
        output.printf("(Maximum allowed: %d managers between employee and CEO)%n", 
                config.getMaxReportingDepth());
        output.println();
        
        if (issues.isEmpty()) {
            output.println("  No issues found.");
        } else {
            for (ReportingDepthIssue issue : issues) {
                printReportingLineIssue(issue);
            }
        }
        output.println();
    }
    
    private void printReportingLineIssue(ReportingDepthIssue issue) {
        output.printf("  • %s (ID: %d)%n", 
                issue.getEmployee().getFullName(), 
                issue.getEmployee().getId());
        output.printf("      Reporting line depth: %d (exceeds by %d)%n", 
                issue.getActualDepth(), 
                issue.getExcessDepth());
        output.print("      Chain: ");
        output.print(issue.getEmployee().getFullName());
        for (var manager : issue.getReportingChain()) {
            output.print(" → " + manager.getFullName());
        }
        output.println(" (CEO)");
        output.println();
    }
    
    private void printFooter(AnalysisReport report) {
        output.println(SEPARATOR);
        if (report.hasIssues()) {
            output.println("Analysis complete. Please review the issues above.");
        } else {
            output.println("Analysis complete. No issues found in the organizational structure.");
        }
        output.println(SEPARATOR);
        output.println();
    }
}

