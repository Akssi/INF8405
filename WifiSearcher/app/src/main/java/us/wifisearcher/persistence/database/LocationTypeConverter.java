package us.wifisearcher.persistence.database;

import android.arch.persistence.room.TypeConverter;
import android.location.Location;

import java.util.Locale;

/**
 * Allows for conversion of un-serializable type to save in DB
 */
public class LocationTypeConverter {

    @TypeConverter
    public static String toString(Location location) {
        if (location == null) {
            return (null);
        }

        return (String.format(Locale.US, "%f,%f", location.getLatitude(), location.getLongitude()));
    }

    @TypeConverter
    public static Location toLocation(String locationString) {
        if (locationString == null) {
            return (null);
        }

        String[] pieces = locationString.split(",");
        Location location = new Location("");

        location.setLatitude(Double.parseDouble(pieces[0]));
        location.setLongitude(Double.parseDouble(pieces[1]));

        return location;
    }
}
