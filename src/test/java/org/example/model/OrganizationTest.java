package org.example.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.example.fixtures.EmployeeDataFixture.*;
import static org.example.fixtures.OrganizationDataFixture.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Organization Tests")
class OrganizationTest {

    @BeforeEach
    void setUp() {
        resetIdGenerator();
    }

    @Test
    @DisplayName("Should create organization and convert list to map successfully")
    void testOrganization_ConvertsListToMap() {
        // Arrange & Act
        Organization organization = organization()
                .withEmployee(ceo().withId(1).withFirstName("John").withLastName("Doe"))
                .withEmployee(regularEmployee().withId(2).withFirstName("Jane").withLastName("Smith").reportingTo(1))
                .withEmployee(regularEmployee().withId(3).withFirstName("Bob").withLastName("Johnson").reportingTo(1))
                .build();

        // Assert
        assertEquals(3, organization.size());
        assertNotNull(organization.getEmployees());
    }

    @Test
    @DisplayName("Should retrieve employee by ID successfully")
    void testGetEmployeeById_ReturnsCorrectEmployee() {
        // Arrange
        Organization organization = organization()
                .withEmployee(ceo().withId(1).withFirstName("John").withLastName("Doe"))
                .withEmployee(regularEmployee().withId(2).withFirstName("Jane").withLastName("Smith").reportingTo(1))
                .build();

        // Act
        Employee result = organization.getEmployeeById(2);

        // Assert
        assertNotNull(result);
        assertEquals("Jane Smith", result.getFullName());
        assertEquals(2, result.getId());
    }

    @Test
    @DisplayName("Should return null when employee ID does not exist")
    void testGetEmployeeById_ReturnsNullForNonExistentId() {
        // Arrange
        Organization organization = singleCeoOrganization().build();

        // Act
        Employee result = organization.getEmployeeById(999);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should return collection from getEmployees")
    void testGetEmployees_ReturnsCollection() {
        // Arrange
        Organization organization = singleCeoOrganization().build();

        // Act
        Collection<Employee> employees = organization.getEmployees();

        // Assert
        assertNotNull(employees);
        assertEquals(1, employees.size());
    }

    @Test
    @DisplayName("Should throw exception when duplicate employee IDs exist")
    void testOrganization_ThrowsExceptionForDuplicateIds() {
        // Arrange & Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            organization()
                    .withEmployee(ceo().withId(1).withFirstName("John").withLastName("Doe"))
                    .withEmployee(ceo().withId(1).withFirstName("Jane").withLastName("Smith"))
                    .build();
        });
    }

    @Test
    @DisplayName("Should handle empty employee list")
    void testOrganization_HandlesEmptyList() {
        // Arrange & Act
        Organization organization = emptyOrganization().build();

        // Assert
        assertEquals(0, organization.size());
        assertTrue(organization.getEmployees().isEmpty());
    }

    @Test
    @DisplayName("Should build hierarchy correctly")
    void testBuildHierarchy_EstablishesManagerRelationships() {
        // Arrange
        Organization organization = basicOrganization().build();

        // Act
        organization.buildHierarchy();

        // Assert
        Employee ceoEmployee = organization.getEmployeeById(1);
        Employee managerEmployee = organization.getEmployeeById(2);
        Employee workerEmployee = organization.getEmployeeById(3);

        assertNotNull(ceoEmployee);
        assertNull(ceoEmployee.getManager());
        assertTrue(ceoEmployee.isManager());

        assertNotNull(managerEmployee);
        assertEquals(ceoEmployee, managerEmployee.getManager());

        assertNotNull(workerEmployee);
        assertEquals(managerEmployee, workerEmployee.getManager());
    }

    @Test
    @DisplayName("Should return CEO after building hierarchy")
    void testGetCeo_ReturnsCeoAfterBuildingHierarchy() {
        // Arrange
        Organization organization = basicOrganization().build();

        // Act
        organization.buildHierarchy();

        // Assert
        Employee ceoEmployee = organization.getCeo();
        assertNotNull(ceoEmployee);
        assertTrue(ceoEmployee.isCeo());
    }

    @Test
    @DisplayName("Should check if employee exists by ID")
    void testHasEmployee_ReturnsCorrectly() {
        // Arrange
        Organization organization = singleCeoOrganization().build();

        // Assert
        assertTrue(organization.hasEmployee(1));
        assertFalse(organization.hasEmployee(999));
    }
}
