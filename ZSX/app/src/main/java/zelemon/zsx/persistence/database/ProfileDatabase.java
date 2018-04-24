package zelemon.zsx.persistence.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

/**
 * Defines the database instance
 */
@Database(entities = {Profile.class}, version = 1)
@TypeConverters({LocationTypeConverter.class})
public abstract class ProfileDatabase extends RoomDatabase {
    public abstract ProfileDao getProfileDao();
}
