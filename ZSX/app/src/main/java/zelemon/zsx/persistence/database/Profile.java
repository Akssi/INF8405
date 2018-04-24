package zelemon.zsx.persistence.database;

import android.arch.persistence.room.Entity;
import android.location.Location;
import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Class that contains the information of a network. Defines Room database table
 */
@Entity(primaryKeys = {"name"})
public class Profile implements Serializable {

    @NonNull
    private String name;
    private Location location;
    private String picture;


    public Profile() {
        this.name = "N/A";
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
