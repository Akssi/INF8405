package us.wifisearcher;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
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
    private MediatorLiveData<List<WifiNetwork>> currentLocationWifiNetworksLiveData;
    private Location currentLocation;
    private List<WifiNetwork> wifiNetworks;

    @Inject
    public WifiSearcherViewModel(@NonNull Application application, @NonNull WifiNetworkRepository wifiNetworkRepository, @NonNull LocationLiveData locationLiveData, @NonNull WifiLiveData wifiLiveData) {
        super(application);
        this.networkRepository = wifiNetworkRepository;
        this.locationLiveData = locationLiveData;
        this.wifiLiveData = wifiLiveData;
        this.batteryLiveData = new BatteryLiveData(application.getApplicationContext());
        this.currentLocationWifiNetworksLiveData = new MediatorLiveData<>();
        this.wifiNetworks = new ArrayList<>();
        initializeCurrentLocationWifiNetworkLiveData();
    }

    private void initializeCurrentLocationWifiNetworkLiveData() {
        this.currentLocationWifiNetworksLiveData.addSource(locationLiveData, location -> {
            this.currentLocation = location;
            currentLocationWifiNetworksLiveData.postValue(wifiNetworks);
        });
        this.currentLocationWifiNetworksLiveData.addSource(wifiLiveData, scanResults -> {
            for (WifiNetwork wifiNetwork : scanResults) {
                wifiNetwork.setLocation(this.currentLocation);
                networkRepository.saveNetwork(wifiNetwork);
            }
            currentLocationWifiNetworksLiveData.postValue(wifiNetworks);

        });
        this.currentLocationWifiNetworksLiveData.addSource(this.getCurrentLocationWifiNetworks(), closeByWifiNetworks -> currentLocationWifiNetworksLiveData.postValue(closeByWifiNetworks));
    }

    public MediatorLiveData<List<WifiNetwork>> getCurrentLocationWifiNetworksLiveData() {
        return currentLocationWifiNetworksLiveData;
    }

    public LocationLiveData getLocationLiveData() {
        return locationLiveData;
    }

    public BatteryLiveData getBatteryLiveData() {
        return batteryLiveData;
    }


    private LiveData<List<WifiNetwork>> getCurrentLocationWifiNetworks() {
        return Transformations.switchMap(locationLiveData, location -> {
            this.currentLocation = location;
            return this.networkRepository.getNetworks(this.currentLocation);
        });
    }

}
