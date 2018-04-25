package zelemon.zsx;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.location.Location;
import android.support.annotation.NonNull;
import zelemon.zsx.battery.BatteryLiveData;
import zelemon.zsx.persistence.ProfileRepository;
import zelemon.zsx.persistence.database.Profile;
import zelemon.zsx.services.LocationLiveData;

import javax.inject.Inject;
import java.util.List;

/**
 * View model that holds part of the logic of the app.
 */
public class TronViewModel extends AndroidViewModel {

    private LocationLiveData locationLiveData;
    private BatteryLiveData batteryLiveData;
    private ProfileRepository profileRepository;
    private Location currentLocation;

    @Inject
    public TronViewModel(@NonNull Application application, @NonNull LocationLiveData locationLiveData, @NonNull BatteryLiveData batteryLiveData, @NonNull ProfileRepository profileRepository) {
        super(application);
        this.locationLiveData = locationLiveData;
        this.batteryLiveData = batteryLiveData;
        this.profileRepository = profileRepository;
    }

    /**
     * Gets the current location.
     *
     * @return The current location.
     */
    public LocationLiveData getLocationLiveData() {
        return locationLiveData;
    }

    /**
     * Gets the current battery status.
     *
     * @return The battery status.
     */
    public BatteryLiveData getBatteryLiveData() {
        return batteryLiveData;
    }

    public void saveProfile(Profile profile) {
        this.profileRepository.saveProfile(profile);
    }

    public LiveData<Profile> getProfile(String name) {
        return this.profileRepository.getProfile(name);
    }

    public LiveData<List<Profile>> getCurrentLocationProfiles() {
        return Transformations.switchMap(locationLiveData, location -> {
            this.currentLocation = location;
            return this.profileRepository.getSurroundingProfiles(this.currentLocation);
        });
    }


    public LiveData<List<Profile>> getMapProfiles() {
        return Transformations.switchMap(locationLiveData, location -> {
            this.currentLocation = location;
            return this.profileRepository.getMapProfiles(this.currentLocation);
        });
    }

    public LiveData<List<Profile>> getAllProfilesWithoutOurDisplayName(String displayName) {
        return this.profileRepository.getAllProfilesWithoutDisplayName(displayName);
    }

    public LiveData<List<Profile>> getProfilesSurroundingLocation(Location location) {
        return this.profileRepository.getSurroundingProfiles(location);
    }

    public LiveData<List<Profile>> getProfilesSurroundingLocation(Location location, int radius) {
        return this.profileRepository.getSurroundingProfiles(location, radius);
    }
}
