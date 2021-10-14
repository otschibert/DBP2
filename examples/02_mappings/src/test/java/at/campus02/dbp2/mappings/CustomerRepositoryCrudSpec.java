package at.campus02.dbp2.mappings;

import at.campus02.dbp2.mappings.AccountType;
import at.campus02.dbp2.mappings.Customer;
import at.campus02.dbp2.mappings.CustomerRepository;
import at.campus02.dbp2.mappings.CustomerRepositoryJpa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerRepositoryCrudSpec {

    //#region test data
    private final String firstname = "Firstname";
    private final String lastname = "Lastname";
    private final AccountType accountType = AccountType.BASIC;
    private final LocalDate registeredSince = LocalDate.now();

    private Customer initDefaultCustomer() {
        Customer customer = new Customer();
        customer.setFirstname(firstname);
        customer.setLastname(lastname);
        customer.setAccountType(accountType);
        customer.setRegisteredSince(registeredSince);
        return customer;
    }
    //#endregion


    //#region setup / tear down
    private EntityManagerFactory factory;
    private CustomerRepository repository;
    private EntityManager manager;

    @BeforeEach
    public void beforeEach() {
        factory = Persistence.createEntityManagerFactory("persistenceUnitName");
        manager = factory.createEntityManager();
        repository = new CustomerRepositoryJpa(factory);
    }

    @AfterEach
    public void afterEach() {
        if (manager.isOpen()) {
            manager.close();
        }
        if (factory.isOpen()) {
            factory.close();
        }
    }
    //#endregion


    //#region CRUD:create

    @Test
    public void createNullAsCustomerReturnsFalse() {
        //when
        boolean result = repository.create(null);
        //then
        assertFalse(result);
    }

    @Test
    public void createPersistCustomerInDatabaseAndReturnsTrue() {
        //given
        Customer toCreate = initDefaultCustomer();
        //when
        boolean result = repository.create(toCreate);
        //then
        assertTrue(result);
        //Kontrolle auch in der Datenbank
        Customer fromDB = manager.find(Customer.class, toCreate.getId());
        assertEquals(firstname, fromDB.getFirstname());
        assertEquals(lastname, fromDB.getLastname());
        assertEquals(accountType, fromDB.getAccountType());
        assertEquals(registeredSince, fromDB.getRegisteredSince());
    }

    @Test
    public void createExistingCustomerReturnsFalse() {
        //given
        Customer toCreate = initDefaultCustomer();
        manager.getTransaction().begin();
        manager.persist(toCreate);
        manager.getTransaction().commit();
        //when
        boolean result = repository.create(toCreate);
        //then
        assertFalse(result);
    }
    //#endregion

}
