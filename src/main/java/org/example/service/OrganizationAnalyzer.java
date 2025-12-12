package org.example.service;

import org.example.model.AnalysisReport;
import org.example.model.Employee;
import org.example.model.ReportingDepthIssue;
import org.example.model.SalaryIssue;

import java.util.List;

/**
 * Runs all the analyzers and bundles up the results.
 * This is the one-stop-shop for "analyze this org."
 */
public class OrganizationAnalyzer {
    
    private final SalaryAnalyzer salaryAnalyzer;
    private final ReportingDepthAnalyzer reportingDepthAnalyzer;
    
    /** Default setup with real analyzers. */
    public OrganizationAnalyzer() {
        this.salaryAnalyzer = new SalaryAnalyzer();
        this.reportingDepthAnalyzer = new ReportingDepthAnalyzer();
    }
    
    /** For tests – inject your own analyzers. */
    public OrganizationAnalyzer(SalaryAnalyzer salaryAnalyzer, ReportingDepthAnalyzer reportingDepthAnalyzer) {
        this.salaryAnalyzer = salaryAnalyzer;
        this.reportingDepthAnalyzer = reportingDepthAnalyzer;
    }
    
    /** Runs salary + depth analysis and returns the combined report. */
    public AnalysisReport analyze(List<Employee> employees) {
        // Run salary analysis
        List<SalaryIssue> salaryIssues = salaryAnalyzer.analyzeSalaries(employees);
        
        // Run reporting line analysis
        List<ReportingDepthIssue> reportingDepthIssues = reportingDepthAnalyzer.analyzeReportingDepth(employees);
        
        // Count managers
        int managerCount = (int) employees.stream()
                .filter(Employee::isManager)
                .count();
        
        return new AnalysisReport(
                salaryIssues,
                reportingDepthIssues,
                employees.size(),
                managerCount
        );
    }

}

