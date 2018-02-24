package us.wifisearcher.persistence.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.location.Location;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface WifiNetworkDao {
    @Insert(onConflict = REPLACE)
    void save(WifiNetwork wifiNetwork);

    @Query("SELECT * FROM wifinetwork WHERE location = :location")
    LiveData<WifiNetwork> load(Location location);

    @Query("SELECT * FROM wifinetwork")
    LiveData<List<WifiNetwork>> getNetworks();
}
