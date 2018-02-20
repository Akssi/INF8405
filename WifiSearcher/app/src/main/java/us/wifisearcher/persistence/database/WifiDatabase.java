package us.wifisearcher.persistence.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

@Database(entities = {WifiNetwork.class}, version = 1)
@TypeConverters({LocationTypeConverter.class})
public abstract class WifiDatabase extends RoomDatabase {
}
