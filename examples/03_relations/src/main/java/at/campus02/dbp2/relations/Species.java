package at.campus02.dbp2.relations;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Species {



    @Id
    @GeneratedValue
    private Integer id;

    public Integer getId() {
        return id;
    }
}
