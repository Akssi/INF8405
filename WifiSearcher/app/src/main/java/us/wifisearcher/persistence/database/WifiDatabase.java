package us.wifisearcher.persistence.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {WifiNetwork.class}, version = 1)
public abstract class WifiDatabase extends RoomDatabase {
}
