package org.example;

import org.example.model.AnalysisReport;
import org.example.model.Employee;
import org.example.model.Organization;
import org.example.service.CsvEmployeeReader;
import org.example.service.OrganizationAnalyzer;
import org.example.service.ReportPrinter;
import org.example.validators.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;
import static org.example.constants.ExitCodes.*;

/**
 * Entry point for the org analyzer.
 * 
 * Reads a CSV, validates the data, builds the org tree, and spits out
 * a report showing salary issues and deep reporting lines.
 * 
 * Dependencies are injected via constructors so we can swap them out in tests.
 */
public class Main {
    
    private final CsvEmployeeReader csvReader;
    private final OrganizationAnalyzer analyzer;
    private final ReportPrinter printer;
    
    /** Default constructor – wires up the real dependencies. */
    public Main() {
        this.csvReader = new CsvEmployeeReader();
        this.analyzer = new OrganizationAnalyzer();
        this.printer = new ReportPrinter();
    }
    
    /** Constructor for tests – pass in mocks/stubs. */
    public Main(CsvEmployeeReader csvReader,
                OrganizationAnalyzer analyzer,
                ReportPrinter printer) {
        this.csvReader = csvReader;
        this.analyzer = analyzer;
        this.printer = printer;
    }
    
    /**
     * CLI entry point. Pass the CSV path as the first arg.
     * Exit codes: 0=ok, 1=bad args, 2=IO error, 3=validation failed, 4=unexpected
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            exit(INVALID_INPUT);
        }
        
        String filePath = args[0];
        Main app = new Main();
        
        try {
            app.run(filePath);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            exit(ANALYSIS_ERROR);
        } catch (IllegalStateException e) {
            System.err.println("Error in organization structure: " + e.getMessage());
            exit(REPORTING_ERROR);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            exit(UNEXPECTED_ERROR);
        }
    }
    
    /**
     * The main pipeline: read CSV → validate → build hierarchy → analyze → print.
     * Throws if the file can't be read or if validation finds problems.
     */
    public void run(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        
        System.out.println("Reading employee data from: " + path.toAbsolutePath());
        
        // Step 1: Read employees from CSV
        List<Employee> employees = csvReader.readEmployees(path);
        System.out.printf("Loaded %d employees.%n%n", employees.size());

        // Step 2: Validate employee records
        List<ValidationError> allErrors = new ArrayList<>();
        
        List<ValidationError> basicErrors = new BasicEmployeeRecordValidator().validate(employees);
        allErrors.addAll(basicErrors);

        Organization org = new Organization(employees);
        
        List<ValidationError> orgErrors = new CompositeValidator(
                new InvalidManagerValidator(),
                new OrganizationCeoValidator(),
                new CircularReferenceValidator()
        ).validate(org);
        allErrors.addAll(orgErrors);
        
        // If there are validation errors, report them and halt
        if (!allErrors.isEmpty()) {
            System.err.println("Validation errors found:");
            allErrors.forEach(error -> System.err.println("  " + error));
            throw new IllegalStateException(
                    String.format("Found %d validation error(s) in the input data", allErrors.size()));
        }

        // Step 3: Build hierarchy and analyze the organization
        org.buildHierarchy();
        AnalysisReport report = analyzer.analyze(employees);
        
        // Step 4: Print the report
        printer.printReport(report);
    }
    
    /** Prints help text when user forgets the CSV arg. */
    private static void printUsage() {
        System.out.println("Employee Organizational Structure Analyzer");
        System.out.println("==========================================");
        System.out.println();
        System.out.println("Usage: java -jar employee-analyzer.jar <csv-file-path>");
        System.out.println();
        System.out.println("Description:");
        System.out.println("  Analyzes employee organizational structure to identify:");
        System.out.println("  - Managers earning outside the expected salary range");
        System.out.println("  - Employees with overly long reporting lines to the CEO");
        System.out.println();
        System.out.println("CSV File Format:");
        System.out.println("  Id,firstName,lastName,salary,managerId");
        System.out.println("  123,Joe,Doe,60000,");
        System.out.println("  124,Martin,Chekov,45000,123");
        System.out.println();
        System.out.println("Note: CEO has no managerId (empty field)");
    }
}
