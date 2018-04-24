package zelemon.zsx.persistence;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.location.Location;
import android.support.annotation.NonNull;
import zelemon.zsx.persistence.database.Profile;
import zelemon.zsx.persistence.database.ProfileDao;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Class that queries the database.
 */
@Singleton
public class ProfileRepository {
    private static final int SURROUNDING_PROFILE_RANGE = 100;
    private static final int PROFILE_MAP_RANGE = 50000;
    private final ProfileDao profileDao;
    private final Executor executor;

    @Inject
    public ProfileRepository(ProfileDao wifiNetworkDao, Executor executor) {
        this.profileDao = wifiNetworkDao;
        this.executor = executor;
    }


    public void saveProfile(Profile profile) {
        this.refreshProfile(profile);
    }

    private void refreshProfile(Profile profile) {
        executor.execute(() -> profileDao.update(profile));
    }


    public LiveData<List<Profile>> getSurroundingProfiles(Location location, int radius) {
        return getProfilesInsideRange(location, radius + SURROUNDING_PROFILE_RANGE);
    }


    public LiveData<List<Profile>> getSurroundingProfiles(Location location) {
        return getProfilesInsideRange(location, SURROUNDING_PROFILE_RANGE);
    }


    public LiveData<List<Profile>> getMapProfiles(Location location) {
        return getProfilesInsideRange(location, PROFILE_MAP_RANGE);
    }

    @NonNull
    private LiveData<List<Profile>> getProfilesInsideRange(Location location, int range) {
        return Transformations.map(profileDao.getProfiles(), profiles -> {
            List<Profile> closeByProfiles = new ArrayList<>();
            if (location != null) {
                for (Profile profile : profiles) {
                    if (profile.getLocation().distanceTo(location) < range) {
                        closeByProfiles.add(profile);
                    }
                }
            }
            return closeByProfiles;
        });
    }
}
