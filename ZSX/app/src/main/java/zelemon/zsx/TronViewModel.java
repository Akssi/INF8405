package zelemon.zsx;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import zelemon.zsx.battery.BatteryLiveData;
import zelemon.zsx.services.LocationLiveData;

/**
 * View model that holds part of the logic of the app.
 */
public class TronViewModel extends AndroidViewModel {

    private LocationLiveData locationLiveData;
    private BatteryLiveData batteryLiveData;

    @Inject
    public TronViewModel(@NonNull Application application, @NonNull LocationLiveData locationLiveData, @NonNull BatteryLiveData batteryLiveData) {
        super(application);
        this.locationLiveData = locationLiveData;
        this.batteryLiveData = batteryLiveData;
    }

    /**
     * Gets the current location.
     * @return The current location.
     */
    public LocationLiveData getLocationLiveData() {
        return locationLiveData;
    }

    /**
     * Gets the current battery status.
     * @return The battery status.
     */
    public BatteryLiveData getBatteryLiveData() {
        return batteryLiveData;
    }
}
