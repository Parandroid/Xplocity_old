package Classes;

/**
 * Created by dmitry on 04.08.17.
 */

public class LocationCategory {

    public int id;
    public String name;
    public String description;

    public boolean selected;

    public LocationCategory() {

    }

    public LocationCategory(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;

    }
}
