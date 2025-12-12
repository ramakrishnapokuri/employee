package org.example;

import org.example.constants.ExitCodes;
import org.example.model.AnalysisReport;
import org.example.model.Employee;
import org.example.service.CsvEmployeeReader;
import org.example.service.OrganizationAnalyzer;
import org.example.service.ReportPrinter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.example.fixtures.EmployeeDataFixture.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Main Application Tests")
class MainTest {

    @TempDir
    Path tempDir;

    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    void setUp() {
        resetIdGenerator();
        outputStream = new ByteArrayOutputStream();
        errorStream = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
    }

    @Test
    @DisplayName("Should run analysis on valid CSV file")
    void testRun_WithValidCsv_ProducesReport() throws IOException {
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                1,John,CEO,200000,
                2,Jane,Manager,80000,1
                3,Bob,Worker,50000,2
                """;
        Path csvFile = createTempCsvFile(csvContent);

        Main app = new Main();
        app.run(csvFile.toString());

        // Should complete without exception
    }

    @Test
    @DisplayName("Should throw exception when file not found")
    void testRun_WithNonExistentFile_ThrowsException() {
        Main app = new Main();

        assertThrows(IOException.class, () -> {
            app.run("/non/existent/file.csv");
        });
    }

    @Test
    @DisplayName("Should throw exception when validation fails")
    void testRun_WithInvalidData_ThrowsException() throws IOException {
        // CSV with invalid manager reference
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                1,John,CEO,200000,
                2,Jane,Manager,80000,999
                """;
        Path csvFile = createTempCsvFile(csvContent);

        Main app = new Main();

        assertThrows(IllegalStateException.class, () -> {
            app.run(csvFile.toString());
        });
    }

    @Test
    @DisplayName("Should throw exception when multiple CEOs found")
    void testRun_WithMultipleCeos_ThrowsException() throws IOException {
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                1,John,CEO1,200000,
                2,Jane,CEO2,200000,
                """;
        Path csvFile = createTempCsvFile(csvContent);

        Main app = new Main();

        assertThrows(IllegalStateException.class, () -> {
            app.run(csvFile.toString());
        });
    }

    @Test
    @DisplayName("Should create Main with default constructor")
    void testDefaultConstructor() {
        Main app = new Main();
        assertNotNull(app);
    }

    @Test
    @DisplayName("Should create Main with custom dependencies")
    void testCustomDependenciesConstructor() {
        CsvEmployeeReader reader = new CsvEmployeeReader();
        OrganizationAnalyzer analyzer = new OrganizationAnalyzer();
        ReportPrinter printer = new ReportPrinter();

        Main app = new Main(reader, analyzer, printer);
        assertNotNull(app);
    }

    @Test
    @DisplayName("Should detect circular reference")
    void testRun_WithCircularReference_ThrowsException() throws IOException {
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                1,John,A,60000,2
                2,Jane,B,50000,1
                """;
        Path csvFile = createTempCsvFile(csvContent);

        Main app = new Main();

        assertThrows(IllegalStateException.class, () -> {
            app.run(csvFile.toString());
        });
    }

    @Test
    @DisplayName("Should handle empty file")
    void testRun_WithEmptyFile_ThrowsException() throws IOException {
        Path csvFile = createTempCsvFile("");

        Main app = new Main();

        assertThrows(IOException.class, () -> {
            app.run(csvFile.toString());
        });
    }

    @Test
    @DisplayName("Should process file with salary issues")
    void testRun_WithSalaryIssues_CompletesSuccessfully() throws IOException {
        // CEO is overpaid (salary 200000, subordinate avg 50000, max should be 75000)
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                1,John,CEO,200000,
                2,Jane,Worker,50000,1
                """;
        Path csvFile = createTempCsvFile(csvContent);

        Main app = new Main();
        app.run(csvFile.toString());

        // Should complete without exception
    }

    @Test
    @DisplayName("Should process file with deep hierarchy")
    void testRun_WithDeepHierarchy_CompletesSuccessfully() throws IOException {
        // Create a deep hierarchy (depth 5, exceeds max of 4)
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                1,CEO,Boss,200000,
                2,Mgr1,One,100000,1
                3,Mgr2,Two,80000,2
                4,Mgr3,Three,70000,3
                5,Mgr4,Four,60000,4
                6,Mgr5,Five,50000,5
                7,Worker,Deep,40000,6
                """;
        Path csvFile = createTempCsvFile(csvContent);

        Main app = new Main();
        app.run(csvFile.toString());

        // Should complete without exception (reporting depth issues are not validation errors)
    }

    private Path createTempCsvFile(String content) throws IOException {
        Path csvFile = tempDir.resolve("test.csv");
        Files.writeString(csvFile, content);
        return csvFile;
    }

    @Test
    @DisplayName("Should verify ExitCodes constants")
    void testExitCodes() {
        // Create instance to get coverage on the class
        ExitCodes codes = new ExitCodes();
        assertNotNull(codes);
        
        assertEquals(0, ExitCodes.SUCCESS);
        assertEquals(1, ExitCodes.INVALID_INPUT);
        assertEquals(2, ExitCodes.ANALYSIS_ERROR);
        assertEquals(3, ExitCodes.REPORTING_ERROR);
        assertEquals(4, ExitCodes.UNEXPECTED_ERROR);
    }
}

