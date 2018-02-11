package us.wifisearcher.persistence.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface WifiNetworkDao {
    @Insert(onConflict = REPLACE)
    void save(WifiNetwork wifiNetwork);

    @Query("SELECT * FROM wifinetwork WHERE id = :id")
    LiveData<WifiNetwork> load(String id);
}
