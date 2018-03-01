package us.wifisearcher;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.location.Location;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import us.wifisearcher.persistence.WifiNetworkRepository;
import us.wifisearcher.persistence.database.WifiNetwork;
import us.wifisearcher.services.BatteryLiveData;
import us.wifisearcher.services.LocationLiveData;
import us.wifisearcher.services.WifiLiveData;

/**
 * View model that holds part of the logic of the app.
 */
public class WifiSearcherViewModel extends AndroidViewModel {

    private WifiNetworkRepository networkRepository;
    private BatteryLiveData batteryLiveData;
    private WifiLiveData wifiLiveData;
    private LocationLiveData locationLiveData;
    private Location currentLocation;
    private List<WifiNetwork> wifiNetworks;
    private boolean isInitialized = false;

    @Inject
    public WifiSearcherViewModel(@NonNull Application application, @NonNull WifiNetworkRepository wifiNetworkRepository, @NonNull LocationLiveData locationLiveData, @NonNull WifiLiveData wifiLiveData, @NonNull BatteryLiveData batteryLiveData) {
        super(application);
        this.networkRepository = wifiNetworkRepository;
        this.locationLiveData = locationLiveData;
        this.wifiLiveData = wifiLiveData;
        this.batteryLiveData = batteryLiveData;
        this.wifiNetworks = new ArrayList<>();
    }

    /**
     * Initializes the observers for the {@link WifiLiveData} and the {@link LocationLiveData}
     */
    public void initializeCurrentLocationWifiNetworkLiveData() {
        // Prevent re-initialization of observers
        if (isInitialized) {
            return;
        }
        isInitialized = true;
        wifiLiveData.executeScan();

        // Triggers a scan when location changes
        this.locationLiveData.observeForever(location -> {
            this.currentLocation = location;
            wifiLiveData.executeScan();
        });

        // Save the networks found after a scan into the database
        this.wifiLiveData.observeForever(scanResults -> {
            if (this.currentLocation != null) {
                for (WifiNetwork wifiNetwork : scanResults) {
                    if (!wifiNetwork.getName().isEmpty()) {
                        wifiNetwork.setLocation(this.currentLocation);
                        networkRepository.saveNetwork(wifiNetwork);
                    }
                }
            }
        });
    }

    /**
     * Adds a network to the database.
     *
     * @param wifiNetwork Network to be added.
     */
    public void updateWifiNetwork(WifiNetwork wifiNetwork) {
        networkRepository.saveNetwork(wifiNetwork);
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

    /**
     * Gets the networks around the current location.
     * @return List of networks around the current location.
     */
    public LiveData<List<WifiNetwork>> getCurrentLocationWifiNetworks() {
        return Transformations.switchMap(locationLiveData, location -> {
            this.currentLocation = location;
            return this.networkRepository.getSurroundingNetworks(this.currentLocation);
        });
    }

    /**
     * Gets the networks on the entire map.
     * @return The networks on the entire map.
     */
    public LiveData<List<WifiNetwork>> getMapWifiNetworks() {
        return Transformations.switchMap(locationLiveData, location -> {
            this.currentLocation = location;
            return this.networkRepository.getMapNetworks(this.currentLocation);
        });
    }

    /**
     * Gets the networks around a particular {@param location} given a radius.
     * @param location Location where to search.
     * @param radius Radius of the search.
     * @return The networks around a particular {@param location} given a {@param radius}.
     */
    public LiveData<List<WifiNetwork>> getWifiNetworksSurroundingLocation(Location location, int radius) {
        return this.networkRepository.getSurroundingNetworks(location, radius);
    }

    /**
     * Gets the networks around a particular {@param location} in a default radius.
     * @param location Location where to search.
     * @return The networks around a particular {@param location}.
     */
    public LiveData<List<WifiNetwork>> getWifiNetworksSurroundingLocation(Location location) {
        return this.networkRepository.getSurroundingNetworks(location);
    }


}
