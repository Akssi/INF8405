package zelemon.zsx.persistence.database;

import android.location.Location;

import java.io.Serializable;

public class SerializableProfile implements Serializable {

    private String name;
    private String location;
    private String picture;

    public SerializableProfile(Profile profile) {
        this.name = profile.getName();
        this.location = LocationTypeConverter.toString(profile.getLocation());
        this.picture = profile.getPicture();
    }

    public Location getLocation() {
        return LocationTypeConverter.toLocation(location);
    }

    public String getName() {
        return name;
    }

    public String getPicture() {
        return picture;
    }

}
