import at.campus02.dbp2.relations.Animal;
import at.campus02.dbp2.relations.Species;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class OneToManyTests {

    private EntityManagerFactory factory;
    private EntityManager manager;

    @BeforeEach
    public void setup() {
        factory = Persistence.createEntityManagerFactory("persistenceUnitName");
        manager = factory.createEntityManager();
    }

    @AfterEach
    public void teardown() {
        if (manager.isOpen()) {
            manager.close();
        }
        if (factory.isOpen()) {
            factory.close();
        }
    }

    @Test
    public void persistStudentWithCascade() {
        //given
        Animal bunny = new Animal("Bunny");
        Animal dog = new Animal("Leo");
        Species mammals = new Species("Mammals");
        //Referenzen für FK in DB
        bunny.setSpecies(mammals);
        dog.setSpecies(mammals);
        //Referenzen für CASCADE
        mammals.getAnimals().add(bunny);
        mammals.getAnimals().add(dog);
        //when
        manager.getTransaction().begin();
        manager.persist(mammals);
        manager.getTransaction().commit();
        manager.clear();
        // then
        Species mammalsFromDb = manager.find(Species.class, mammals.getId());
        assertThat(mammalsFromDb.getAnimals().size(), is(2));
        assertThat(mammalsFromDb.getAnimals(), containsInAnyOrder(bunny, dog));
    }

    @Test
    @Disabled("Only works without orphanRemoval - enable after setting orphanRemoval to false")
    public void updateExampleWithCorrectingReferences(){
        // -------------------------------------
        //given
        Animal clownfish = new Animal("Nemo");
        Animal squirrel = new Animal("Puschel");
        Species fish = new Species("Fisch");
        //Referenzen für DB
        clownfish.setSpecies(fish);
        //FEHLER --> soll korrigiert werden
        squirrel.setSpecies(fish);
        //Referenzen für CASCADE
        fish.getAnimals().add(clownfish);
        fish.getAnimals().add(squirrel);
        //Speichern
        manager.getTransaction().begin();
        manager.persist(fish);
        manager.getTransaction().commit();
        manager.clear();

        //-----------------------------------------
        //when: Korrekturversuch - zum Scheitern verurteilt
        manager.getTransaction().begin();
        fish.getAnimals().remove(squirrel);
        manager.merge(fish);
        manager.getTransaction().commit();
        manager.clear();

        //-------------------------------------------
        //then
        //Squirrel existiert noch in DB
        Animal squirrelFromDb = manager.find(Animal.class, squirrel.getId());
        assertThat(squirrelFromDb, is(notNullValue()));
        //Squirrel ist immer noch ein Fisch - wir haben im Speicher die Fisch-Liste geändert,
        //aber species von squirrel zeigt nach wie vor auf fish - auch in der Datenbank
        assertThat(squirrelFromDb.getSpecies().getId(), is(fish.getId()));
        //auch wenn wir die Liste mittels "refresh" neu einlesen, wird die Referenz von
        //Squirrel auf fish (DB) neu eingelesen und squirrel ist wieder in der Liste drin.
        Species mergedFish = manager.merge(fish);
        manager.refresh((mergedFish));
        assertThat(mergedFish.getAnimals().size(), is(2));

    }

    @Test
    public void updateExampleWithCorrectingReferencesSecondTry(){
        // -------------------------------------
        //given
        Animal clownfish = new Animal("Nemo");
        Animal squirrel = new Animal("Puschel");
        Species fish = new Species("Fisch");
        //Referenzen für DB
        clownfish.setSpecies(fish);
        //FEHLER --> soll korrigiert werden
        squirrel.setSpecies(fish);
        //Referenzen für CASCADE
        fish.getAnimals().add(clownfish);
        fish.getAnimals().add(squirrel);
        //Speichern
        manager.getTransaction().begin();
        manager.persist(fish);
        manager.getTransaction().commit();
        manager.clear();

        //-----------------------------------------
        //when: Korrekturversuch
        manager.getTransaction().begin();
        squirrel.setSpecies(null);
        manager.merge(fish); //eigentlich (squirrel)
        manager.getTransaction().commit();
        manager.clear();

        //-------------------------------------------
        //then
        //Squirrel existiert noch in DB
        Animal squirrelFromDb = manager.find(Animal.class, squirrel.getId());
        assertThat(squirrelFromDb, is(notNullValue()));
        //Squirrel ist kein Fish mehr
        assertThat(squirrelFromDb.getSpecies(), is(nullValue()));
        //Squirrel nicht mehr in Liste
        Species mergedFish = manager.merge(fish);
        manager.refresh((mergedFish));
        assertThat(mergedFish.getAnimals().size(), is(1));
    }

    @Test
    public void orphanRemovalDeletesOrphansFromDatabase(){
        // -------------------------------------
        //given
        Animal clownfish = new Animal("Nemo");
        Animal squirrel = new Animal("Puschel");
        Species fish = new Species("Fisch");
        //Referenzen für DB
        clownfish.setSpecies(fish);
        //FEHLER --> soll korrigiert werden
        squirrel.setSpecies(fish);
        //Referenzen für CASCADE
        fish.getAnimals().add(clownfish);
        fish.getAnimals().add(squirrel);
        //Speichern
        manager.getTransaction().begin();
        manager.persist(fish);
        manager.getTransaction().commit();
        manager.clear();
        //when
        manager.getTransaction().begin();
        fish.getAnimals().remove(squirrel);
        manager.merge(fish);
        manager.getTransaction().commit();
        manager.clear();
        //then
        Animal squirrelFromDb = manager.find(Animal.class, squirrel.getId());
        //bei Verwendung von orphanRemoval wird squirrel aus der DB gelöscht
        assertThat(squirrelFromDb, is(Matchers.nullValue()));
        Species refreshedFish = manager.merge(fish);
        manager.refresh(refreshedFish);
        assertThat(refreshedFish.getAnimals().size(), is(1));
    }


}
