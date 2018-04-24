package zelemon.zsx.persistence.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.location.Location;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Specifies the available database queries
 */
@Dao
public interface ProfileDao {
    @Insert(onConflict = REPLACE)
    long save(Profile profile);

    @Query("SELECT * FROM Profile WHERE name = :name")
    LiveData<Profile> getProfileByName(String name);

    @Query("SELECT * FROM Profile WHERE location = :location")
    LiveData<Profile> loadProfileFromLocation(Location location);

    @Query("SELECT * FROM Profile")
    LiveData<List<Profile>> getProfiles();
}
