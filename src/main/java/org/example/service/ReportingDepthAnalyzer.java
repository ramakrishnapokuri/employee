package org.example.service;

import org.example.config.AppConfig;
import org.example.model.Employee;
import org.example.model.ReportingDepthIssue;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds people who are buried too deep in the org chart.
 */
public class ReportingDepthAnalyzer {

    private final int maxDepth;
    
    public ReportingDepthAnalyzer() {
        this(AppConfig.get());
    }
    
    /** For testing with custom config. */
    public ReportingDepthAnalyzer(AppConfig config) {
        this.maxDepth = config.getMaxReportingDepth();
    }

    /** Find everyone with too many managers above them. */
    public List<ReportingDepthIssue> analyzeReportingDepth(List<Employee> employees) {
        List<ReportingDepthIssue> issues = new ArrayList<>();

        for (Employee employee : employees) {
            Employee.DepthAndChain depthAndChain = employee.findDepthAndChain();
            
            if (depthAndChain.depth() > maxDepth) {
                issues.add(new ReportingDepthIssue(
                        employee, 
                        depthAndChain.depth(), 
                        depthAndChain.chain(),
                        maxDepth
                ));
            }
        }

        return issues;
    }
}

