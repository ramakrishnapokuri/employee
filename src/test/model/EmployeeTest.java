package org.example.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.example.fixtures.EmployeeDataFixture.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Employee model class.
 */
@DisplayName("Employee Model Tests")
class EmployeeTest {

    @BeforeEach
    void setUp() {
        resetIdGenerator();
    }

    @Test
    @DisplayName("Should create employee with all properties")
    void shouldCreateEmployeeWithAllProperties() {
        Employee employee = employee()
                .withId(1)
                .withFirstName("John")
                .withLastName("Doe")
                .withSalary(50000)
                .reportingTo(100)
                .build();

        assertEquals(1, employee.getId());
        assertEquals("John", employee.getFirstName());
        assertEquals("Doe", employee.getLastName());
        assertEquals("John Doe", employee.getFullName());
        assertEquals(50000.0, employee.getSalary());
        assertEquals(100, employee.getManagerId());
    }

    @Test
    @DisplayName("Should identify CEO correctly (no manager)")
    void shouldIdentifyCeoCorrectly() {
        Employee ceoEmployee = ceo().withId(1).build();
        Employee regularEmp = regularEmployee().withId(2).reportingTo(1).build();

        assertTrue(ceoEmployee.isCeo());
        assertFalse(regularEmp.isCeo());
    }

    @Test
    @DisplayName("Should identify manager correctly (has subordinates)")
    void shouldIdentifyManagerCorrectly() {
        Employee managerEmployee = manager().withId(1).asCeo().build();
        Employee subordinate = regularEmployee().withId(2).reportingTo(1).build();

        subordinate.setManager(managerEmployee);

        assertTrue(managerEmployee.isManager());
        assertFalse(subordinate.isManager());
    }

    @Test
    @DisplayName("Should establish bidirectional manager-subordinate relationship")
    void shouldEstablishBidirectionalRelationship() {
        Employee managerEmployee = manager().withId(1).asCeo().build();
        Employee sub1 = regularEmployee().withId(2).withFirstName("John").reportingTo(1).build();
        Employee sub2 = regularEmployee().withId(3).withFirstName("Mary").reportingTo(1).build();

        sub1.setManager(managerEmployee);
        sub2.setManager(managerEmployee);

        assertEquals(managerEmployee, sub1.getManager());
        assertEquals(managerEmployee, sub2.getManager());
        assertEquals(2, managerEmployee.getDirectSubordinates().size());
        assertTrue(managerEmployee.getDirectSubordinates().contains(sub1));
        assertTrue(managerEmployee.getDirectSubordinates().contains(sub2));
    }

    @Test
    @DisplayName("Should return unmodifiable list of subordinates")
    void shouldReturnUnmodifiableSubordinatesList() {
        Employee managerEmployee = manager().withId(1).asCeo().build();
        Employee subordinate = regularEmployee().withId(2).reportingTo(1).build();

        subordinate.setManager(managerEmployee);

        assertThrows(UnsupportedOperationException.class, () -> {
            managerEmployee.getDirectSubordinates().add(
                    regularEmployee().withId(3).build()
            );
        });
    }

    @Test
    @DisplayName("Should implement equals based on ID")
    void shouldImplementEqualsBasedOnId() {
        Employee emp1 = employee().withId(1).withFirstName("John").withLastName("Doe").build();
        Employee emp2 = employee().withId(1).withFirstName("Jane").withLastName("Different").build();
        Employee emp3 = employee().withId(2).withFirstName("John").withLastName("Doe").build();

        assertEquals(emp1, emp2); // Same ID
        assertNotEquals(emp1, emp3); // Different ID
    }

    @Test
    @DisplayName("Should have consistent hashCode for equal employees")
    void shouldHaveConsistentHashCode() {
        Employee emp1 = employee().withId(1).withFirstName("John").withLastName("Doe").build();
        Employee emp2 = employee().withId(1).withFirstName("Jane").withLastName("Different").build();

        assertEquals(emp1.hashCode(), emp2.hashCode());
    }

    @Test
    @DisplayName("Should provide meaningful toString")
    void shouldProvideMeaningfulToString() {
        Employee emp = employee()
                .withId(123)
                .withFirstName("John")
                .withLastName("Doe")
                .withSalary(50000)
                .reportingTo(100)
                .build();

        String str = emp.toString();

        assertTrue(str.contains("123"));
        assertTrue(str.contains("John"));
        assertTrue(str.contains("Doe"));
        assertTrue(str.contains("50000"));
    }

    @Test
    @DisplayName("Should calculate average salary of subordinates")
    void shouldCalculateAvgSalaryOfSubordinates() {
        Employee mgrEmp = manager().withId(1).asCeo().build();
        Employee sub1 = regularEmployee().withId(2).withSalary(40000).reportingTo(1).build();
        Employee sub2 = regularEmployee().withId(3).withSalary(60000).reportingTo(1).build();

        sub1.setManager(mgrEmp);
        sub2.setManager(mgrEmp);

        assertEquals(50000, mgrEmp.getAvgSalaryOfSubordinates());
    }

    @Test
    @DisplayName("Should return 0 for avg salary when no subordinates")
    void shouldReturnZeroForAvgSalaryWithNoSubordinates() {
        Employee emp = employee().withId(1).build();
        assertEquals(0.0, emp.getAvgSalaryOfSubordinates());
    }

    @Test
    @DisplayName("Should calculate depth correctly")
    void shouldCalculateDepth() {
        Employee ceoEmp = ceo().withId(1).build();
        Employee mgrEmp = manager().withId(2).reportingTo(1).build();
        Employee workerEmp = regularEmployee().withId(3).reportingTo(2).build();

        mgrEmp.setManager(ceoEmp);
        workerEmp.setManager(mgrEmp);

        assertEquals(0, ceoEmp.findDepth());
        assertEquals(1, mgrEmp.findDepth());
        assertEquals(2, workerEmp.findDepth());
    }

    @Test
    @DisplayName("Should get reporting chain")
    void shouldGetReportingChain() {
        Employee ceoEmp = ceo().withId(1).build();
        Employee mgrEmp = manager().withId(2).reportingTo(1).build();
        Employee workerEmp = regularEmployee().withId(3).reportingTo(2).build();

        mgrEmp.setManager(ceoEmp);
        workerEmp.setManager(mgrEmp);

        List<Employee> chain = workerEmp.getReportingChain();

        assertEquals(2, chain.size());
        assertEquals(mgrEmp, chain.get(0));
        assertEquals(ceoEmp, chain.get(1));
    }

    @Test
    @DisplayName("Should return empty chain for CEO")
    void shouldReturnEmptyChainForCeo() {
        Employee ceoEmp = ceo().withId(1).build();
        assertTrue(ceoEmp.getReportingChain().isEmpty());
    }

    @Test
    @DisplayName("Should handle changing managers")
    void shouldHandleChangingManagers() {
        Employee mgr1 = manager().withId(1).asCeo().build();
        Employee mgr2 = manager().withId(2).asCeo().build();
        Employee worker = regularEmployee().withId(3).build();

        worker.setManager(mgr1);
        assertEquals(mgr1, worker.getManager());
        assertTrue(mgr1.getDirectSubordinates().contains(worker));

        // Change manager
        worker.setManager(mgr2);
        assertEquals(mgr2, worker.getManager());
        assertTrue(mgr2.getDirectSubordinates().contains(worker));
        assertFalse(mgr1.getDirectSubordinates().contains(worker));
    }

    @Test
    @DisplayName("Should not add duplicate subordinates")
    void shouldNotAddDuplicateSubordinates() {
        Employee mgrEmp = manager().withId(1).asCeo().build();
        Employee worker = regularEmployee().withId(2).build();

        worker.setManager(mgrEmp);
        worker.setManager(mgrEmp); // Set same manager again

        assertEquals(1, mgrEmp.getDirectSubordinates().size());
    }

    @Test
    @DisplayName("Should handle setting null manager")
    void shouldHandleSettingNullManager() {
        Employee mgrEmp = manager().withId(1).asCeo().build();
        Employee worker = regularEmployee().withId(2).build();

        worker.setManager(mgrEmp);
        worker.setManager(null);

        assertNull(worker.getManager());
    }

    @Test
    @DisplayName("Should handle equals with null and different class")
    void shouldHandleEqualsEdgeCases() {
        Employee emp = employee().withId(1).build();

        assertNotEquals(emp, null);
        assertNotEquals(emp, "not an employee");
        assertEquals(emp, emp); // Same object
    }

    @Test
    @DisplayName("Should find depth and chain in single traversal")
    void shouldFindDepthAndChain() {
        Employee ceoEmp = ceo().withId(1).build();
        Employee mgrEmp = manager().withId(2).reportingTo(1).build();
        Employee workerEmp = regularEmployee().withId(3).reportingTo(2).build();

        mgrEmp.setManager(ceoEmp);
        workerEmp.setManager(mgrEmp);

        Employee.DepthAndChain result = workerEmp.findDepthAndChain();

        assertEquals(2, result.depth());
        assertEquals(2, result.chain().size());
        assertEquals(mgrEmp, result.chain().get(0));
        assertEquals(ceoEmp, result.chain().get(1));
    }

    @Test
    @DisplayName("Should return empty chain for CEO in findDepthAndChain")
    void shouldReturnEmptyChainForCeoInDepthAndChain() {
        Employee ceoEmp = ceo().withId(1).build();

        Employee.DepthAndChain result = ceoEmp.findDepthAndChain();

        assertEquals(0, result.depth());
        assertTrue(result.chain().isEmpty());
    }
}
