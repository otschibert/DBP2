import at.campus02.dbp2.relations.Animal;
import at.campus02.dbp2.relations.Student;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class OneToOneTests {

    private EntityManagerFactory factory;
    private EntityManager manager;

    @BeforeEach
    public void setup(){
        factory = Persistence.createEntityManagerFactory("persistenceUnitName");
        manager = factory.createEntityManager();
    }

    @AfterEach
    public void teardown(){
        if (manager.isOpen()){
            manager.close();
        }
        if (factory.isOpen()){
            factory.close();
        }
    }

    @Test
    public void persistAnimalAndStudentStoresRelationInDatabase(){
        //given
        Student student = new Student("Hansi");
        Animal animal = new Animal("Flipper");
        //im Speicher selber um die Referenzen kümmern
        student.setPet(animal);
        animal.setOwner(student);
        //when
        manager.getTransaction().begin();
        manager.persist(student);
        manager.persist(animal);
        manager.getTransaction().commit();
        //then
        Animal flipperFromDB = manager.find(Animal.class, animal.getId());
        assertThat(flipperFromDB.getOwner(), is(student));
        Student ownerFromDB = manager.find(Student.class, student.getId());
        assertThat(ownerFromDB.getPet(), is(animal));
    }

    @Test
    public void persistStudentWithCascadeAlsoPersistsAnimal(){
        //given
        Student hansi = new Student("Hansi");
        Animal bunny = new Animal("Bunny");
        //Referenzen im Speicher verwalten
        //1) Owner setzen, um in der Datenbank die Relation zu schließen
        bunny.setOwner(hansi);
        //2) Pet setzen, damit CASCADE funktioniert
        hansi.setPet(bunny);
        //when
        manager.getTransaction().begin();
        manager.persist(hansi);
        // bunny soll durch cascade mit hansi mitgespeichert werden
        manager.getTransaction().commit();
        manager.clear();
        // then
        Animal bunnyFromDb = manager.find(Animal.class, bunny.getId());
        assertThat(bunnyFromDb.getOwner(), is(hansi));
        Student hansiFromDb = manager.find(Student.class, hansi.getId());
        assertThat(hansiFromDb.getPet(), is(bunny));
    }

    @Test
    public void refreshClosesReferencesNotHandledInMemory (){
        //given
        Student hansi = new Student("Hansi");
        Animal bunny = new Animal("Bunny");
        //Referenzen im Speicher verwalten
        //1) Owner setzen, um in der Datenbank die Relation zu schließen --> muss man immer machen!
        bunny.setOwner(hansi);
        //2) Pet absichtlich NICHT setzen, um refresh zu demonstieren
        //hansi.setPet(bunny);
        //when
        manager.getTransaction().begin();
        manager.persist(bunny);
        //nachdem an hansi kein pet gesetzt ist, reicht es nicht, hansi allein zu persistieren
        //Cascade kann nicht greifen --> wir müssen beide Entities persistieren
        //Reihenfolge innerhalb der Transaktion ist EGAL
        manager.persist(hansi);
        manager.getTransaction().commit();
        manager.clear();
        //then
        //1) Referenz von Animal auf Student ist gesetzt
        Animal bunnyFromDb = manager.find(Animal.class, bunny.getId());
        assertThat(bunnyFromDb.getOwner(), is(hansi));
        //2) ohne refresh wird die Referenz von hansi auf bunny nicht geschlossen
        //auch nicht nach manager.clear, welches den Level1 Cache leert - bei Relationen
        Student hansiFromDb = manager.find(Student.class, hansi.getId());
        assertThat(hansiFromDb.getPet(), is(nullValue()));
        //3) refresh erzwingt das Neu-Einlesen aus der Datenbank auch mit Relationen
        manager.refresh(hansiFromDb);
        assertThat(hansiFromDb.getPet(), is(bunny));
    }
}
