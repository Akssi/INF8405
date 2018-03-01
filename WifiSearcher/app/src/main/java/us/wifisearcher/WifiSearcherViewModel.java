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

public class WifiSearcherViewModel extends AndroidViewModel {

    private WifiNetworkRepository networkRepository;
    private BatteryLiveData batteryLiveData;
    private WifiLiveData wifiLiveData;
    private LocationLiveData locationLiveData;
    private Location currentLocation;
    private List<WifiNetwork> wifiNetworks;

    @Inject
    public WifiSearcherViewModel(@NonNull Application application, @NonNull WifiNetworkRepository wifiNetworkRepository, @NonNull LocationLiveData locationLiveData, @NonNull WifiLiveData wifiLiveData, @NonNull BatteryLiveData batteryLiveData) {
        super(application);
        this.networkRepository = wifiNetworkRepository;
        this.locationLiveData = locationLiveData;
        this.wifiLiveData = wifiLiveData;
        this.batteryLiveData = batteryLiveData;
        this.wifiNetworks = new ArrayList<>();
        initializeCurrentLocationWifiNetworkLiveData();
    }

    private void initializeCurrentLocationWifiNetworkLiveData() {
        this.locationLiveData.observeForever(location -> {
            this.currentLocation = location;
            wifiLiveData.executeScan();
        });
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

    public LocationLiveData getLocationLiveData() {
        return locationLiveData;
    }

    public BatteryLiveData getBatteryLiveData() {
        return batteryLiveData;
    }


    public LiveData<List<WifiNetwork>> getCurrentLocationWifiNetworks() {
        return Transformations.switchMap(locationLiveData, location -> {
            this.currentLocation = location;
            return this.networkRepository.getSurroundingNetworks(this.currentLocation);
        });
    }

    public LiveData<List<WifiNetwork>> getMapWifiNetworks() {
        return Transformations.switchMap(locationLiveData, location -> {
            this.currentLocation = location;
            return this.networkRepository.getMapNetworks(this.currentLocation);
        });
    }

    public LiveData<List<WifiNetwork>> getWifiNetworksSurroundingLocation(Location location, int radius) {
        return this.networkRepository.getSurroundingNetworks(location, radius);
    }

    public LiveData<List<WifiNetwork>> getWifiNetworksSurroundingLocation(Location location) {
        return this.networkRepository.getSurroundingNetworks(location);
    }


}
