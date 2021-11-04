package at.campus02.dbp2.relations;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Country {

    @Id
    private String name;

    @ManyToMany (mappedBy = "countries", cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private List<Animal> animals = new ArrayList<>();

    public Country(String name){
        this.name = name;
    }

    public Country(){};

    public String getName() {
        return name;
    }

    public List<Animal> getAnimals() {
        return animals;
    }
}
