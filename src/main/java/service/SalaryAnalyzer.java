package org.example.service;

import org.example.config.AppConfig;
import org.example.model.Employee;
import org.example.model.Issue;
import org.example.model.SalaryIssue;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks if managers earn within the configured range above their direct reports.
 */
public class SalaryAnalyzer {
    
    private final double minMultiplier;
    private final double maxMultiplier;
    
    public SalaryAnalyzer() {
        this(AppConfig.get());
    }
    
    /** For testing with custom config. */
    public SalaryAnalyzer(AppConfig config) {
        this.minMultiplier = config.getMinSalaryMultiplier();
        this.maxMultiplier = config.getMaxSalaryMultiplier();
    }
    
    /** Find all managers whose salary is outside the acceptable range. */
    public List<SalaryIssue> analyzeSalaries(List<Employee> employees) {
        List<SalaryIssue> issues = new ArrayList<>();
        for (Employee employee : employees) {
            SalaryIssue issue = analyzeManagerSalary(employee);
            if (issue != null) {
                issues.add(issue);
            }
        }
        return issues;
    }
    
    /** Check one manager. Returns null if not a manager or salary is fine. */
    public SalaryIssue analyzeManagerSalary(Employee manager) {
        if (!manager.isManager()) {
            return null;
        }
        
        double avg = manager.getAvgSalaryOfSubordinates();
        double salary = manager.getSalary();
        double min = avg * minMultiplier;
        double max = avg * maxMultiplier;
        
        if (salary < min) {
            return new SalaryIssue(manager, Issue.Type.UNDERPAID_ISSUE, avg, min, max);
        }
        if (salary > max) {
            return new SalaryIssue(manager, Issue.Type.OVERPAID_ISSUE, avg, min, max);
        }
        return null;
    }
}

