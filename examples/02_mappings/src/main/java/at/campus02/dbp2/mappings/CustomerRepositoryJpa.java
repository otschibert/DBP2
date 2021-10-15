package at.campus02.dbp2.mappings;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class CustomerRepositoryJpa implements CustomerRepository {

    private EntityManager manager;

    public CustomerRepositoryJpa(EntityManagerFactory factory) {
        this.manager = factory.createEntityManager();
    }

    @Override
    public boolean create(Customer customer) {
        if (customer == null) {
            return false;
        }
        if (customer.getId() != null) { //kein setter für ID --> wenn ID != null dann existiert der Customer schon
            return false;
        }
        manager.getTransaction().begin();
        manager.persist(customer);
        manager.getTransaction().commit();
        return true;
    }

    @Override
    public Customer read(Integer id) {
        if (id == null){
            return null;
        }
        return manager.find(Customer.class, id);
    }

    @Override
    public Customer update(Customer customer) {
        if (customer == null){
            return null;
        }
        if (read(customer.getId())== null){ //id ist null oder id existiert in der DB nicht
            throw new IllegalArgumentException("Customer does not exist, cannot update!");
        }
        manager.getTransaction().begin();
        Customer managed = manager.merge(customer);
        manager.getTransaction().commit();
        return managed;
    }

    @Override
    public boolean delete(Customer customer) {
        if (customer == null){
            return false;
        }
        if (read(customer.getId()) == null){ //id ist null oder id existiert in der DB nicht
            throw new IllegalArgumentException("Customer does not exist, cannot delete!");
        }
        manager.getTransaction().begin();
        manager.remove(manager.merge(customer));
        manager.getTransaction().commit();
        return true;
    }

    @Override
    public List<Customer> getAllCustomers() {
        TypedQuery<Customer> query =
                manager.createQuery(
                        "select c from Customer c " +
                                "order by c.registeredSince",
                        Customer.class
                );
        return query.getResultList();
    }

    @Override
    public List<Customer> findByLastname(String lastnamePart) {
        if (lastnamePart == null || lastnamePart.isEmpty()){
            return Collections.emptyList();
        }
        TypedQuery<Customer> query =
                manager.createNamedQuery("Customer.findByLastnamePart",
                        Customer.class
                );
        query.setParameter("lastnamePart", "%" + lastnamePart + "%");
        return query.getResultList();
    }

    @Override
    public List<Customer> findByAccountType(AccountType type) {
        TypedQuery<Customer> query =
                manager.createQuery(
                        "select c from Customer c " +
                                "where c.accountType = :accountType ",
                        Customer.class
                );
        query.setParameter("accountType", type);
        return query.getResultList();
    }

    @Override
    public List<Customer> findAllRegisteredAfter(LocalDate date) {
        TypedQuery<Customer> query =
                manager.createQuery(
                        "select c from Customer c " +
                                "where c.registeredSince > :regsteredAfter ",
                        Customer.class
                );
        query.setParameter("regsteredAfter", date);
        return query.getResultList();
    }
}
