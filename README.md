# Employee Org Analyzer

A simple tool to analyze your company's org structure and spot salary/reporting issues.

## What it does

BIG COMPANY needed a way to check two things:
1. **Salary sanity check** – Every manager should earn 20-50% more than their direct reports' average. Not less (unfair to the manager), not more (budget bloat).
2. **Reporting line depth** – Nobody should have more than 4 managers between them and the CEO. Too many layers = bureaucracy nightmare.

## Quick Start

```bash
# Build it
mvn clean compile

# Run tests
mvn test

# Analyze your org
mvn exec:java -Dexec.mainClass="org.example.Main" -Dexec.args="path/to/employees.csv"
```

## Requirements

- Java 17+
- Maven 3.6+

## CSV Format

Pretty straightforward:

```csv
Id,firstName,lastName,salary,managerId
123,Joe,Doe,60000,
124,Martin,Chekov,45000,123
125,Bob,Ronstad,47000,123
```

- `managerId` empty = that's your CEO
- IDs must be unique
- No fancy quoted fields or weird delimiters – just plain CSV

## What You'll See

```
══════════════════════════════════════════════════════════════════════
        EMPLOYEE ORGANIZATIONAL STRUCTURE ANALYSIS REPORT
══════════════════════════════════════════════════════════════════════

SUMMARY
──────────────────────────────────────────────────────────────────────
Total employees analyzed: 31
Total managers: 13
Issues found: 5

MANAGERS WHO EARN LESS THAN THEY SHOULD
──────────────────────────────────────────────────────────────────────
  • Sarah Johnson (ID: 101)
      Current salary: $120,000.00
      Subordinates' average: $72,333.33
      Expected range: $86,800.00 - $108,500.00
      Underpaid by: $6,800.00
```

## How It Works

### The Flow

1. **Read** – Parse the CSV into Employee objects
2. **Validate** – Check for problems (missing managers, circular refs, duplicate IDs, etc.)
3. **Build** – Wire up the manager/subordinate relationships
4. **Analyze** – Find salary and depth issues
5. **Report** – Print everything nicely

### Project Layout

```
src/main/java/org/example/
├── Main.java                    # Entry point
├── model/
│   ├── Employee.java            # The core entity
│   ├── Organization.java        # Holds everyone, builds the tree
│   ├── AnalysisReport.java      # Collects all the findings
│   └── *Issue.java              # Different issue types
├── service/
│   ├── CsvEmployeeReader.java   # Parses CSV
│   ├── SalaryAnalyzer.java      # Checks the 20-50% rule
│   ├── ReportingDepthAnalyzer.java  # Checks depth
│   └── OrganizationAnalyzer.java    # Coordinates everything
└── validators/
    ├── BasicEmployeeRecordValidator.java  # Empty list? Duplicate IDs?
    ├── InvalidManagerValidator.java       # Manager exists?
    ├── OrganizationCeoValidator.java      # Exactly one CEO?
    ├── CircularReferenceValidator.java    # A reports to B reports to A?
    └── CompositeValidator.java            # Runs all validators together
```

### Design Choices

Nothing fancy, just tried to keep things clean:

- **Strategy pattern** for validators – easy to add new validation rules
- **Composite pattern** for running multiple validators as one
- **Facade** in OrganizationAnalyzer – hides the complexity of coordinating analyzers
- **Constructor injection** everywhere – makes testing way easier

| Pattern | Where | Why |
|---------|-------|-----|
| Strategy | Validators | Swap validation logic without changing callers |
| Composite | CompositeValidator | Treat one validator or many the same way |
| Facade | OrganizationAnalyzer | One method to run all analysis |
| Repository | Organization | Central place for employee lookups |

## Validation Rules

The app will yell at you (and exit) if:

- ❌ No employees in the file
- ❌ Duplicate employee IDs
- ❌ Manager ID points to nobody
- ❌ Zero or multiple CEOs
- ❌ Circular reporting (A → B → C → A)

## Business Rules

**Salary:**
- Manager must earn ≥ 20% above their direct reports' average
- Manager must earn ≤ 50% above their direct reports' average
- "Manager" = has at least one direct report

**Depth:**
- Max 4 managers between anyone and the CEO
- CEO is depth 0, their reports are depth 1, etc.

## Tests

```bash
mvn test
```

Covers the basics:
- CSV parsing (happy path + errors)
- Validation edge cases
- Salary boundary conditions
- Depth calculations
- End-to-end scenarios

## Ideas for Later

If this ever needs to grow:
- [ ] Support quoted CSV fields
- [ ] Make thresholds configurable (not hardcoded 20%/50%)
- [ ] JSON/CSV export
- [ ] Handle larger files efficiently

---

Built as a coding exercise. Nothing production-critical here.
