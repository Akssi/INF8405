package us.wifisearcher.persistence.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.location.Location;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;

@Dao
public interface WifiNetworkDao {
    @Insert(onConflict = IGNORE)
    long save(WifiNetwork wifiNetwork);

    @Update(onConflict = IGNORE)
    void update(WifiNetwork wifiNetwork);

    @Query("SELECT * FROM wifinetwork WHERE name = :name")
    WifiNetwork getNetworkByName(String name);

    @Query("SELECT * FROM wifinetwork WHERE location = :location")
    LiveData<WifiNetwork> load(Location location);

    @Query("SELECT * FROM wifinetwork")
    LiveData<List<WifiNetwork>> getNetworks();
}
