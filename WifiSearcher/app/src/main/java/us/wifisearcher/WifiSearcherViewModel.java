package us.wifisearcher;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MediatorLiveData;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;
import us.wifisearcher.persistence.WifiNetworkRepository;
import us.wifisearcher.persistence.database.WifiNetwork;
import us.wifisearcher.services.LocationLiveData;
import us.wifisearcher.services.WifiLiveData;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static android.net.wifi.WifiManager.calculateSignalLevel;

public class WifiSearcherViewModel extends AndroidViewModel {

    @Inject
    WifiNetworkRepository networkRepository;
    private WifiLiveData wifiLiveData;
    private LocationLiveData locationLiveData;
    private MediatorLiveData<List<WifiNetwork>> networkLiveData;
    private Location currentLocation;
    private List<WifiNetwork> wifiNetworks;

    @Inject
    public WifiSearcherViewModel(@NonNull Application application, WifiNetworkRepository wifiNetworkRepository) {
        super(application);
        networkRepository = wifiNetworkRepository;
        locationLiveData = new LocationLiveData(application.getApplicationContext());
        wifiLiveData = new WifiLiveData(application.getApplicationContext());
        this.networkLiveData = new MediatorLiveData<>();
        initializeNetworkLiveData();
    }

    public WifiSearcherViewModel(@NonNull Application application) {
        super(application);
        locationLiveData = new LocationLiveData(application.getApplicationContext());
        wifiLiveData = new WifiLiveData(application.getApplicationContext());
        this.networkLiveData = new MediatorLiveData<>();
        this.wifiNetworks = new ArrayList<>();
        initializeNetworkLiveData();

    }

    private void initializeNetworkLiveData() {
        this.networkLiveData.addSource(locationLiveData, location -> {
            this.currentLocation = location;
            networkLiveData.postValue(wifiNetworks);
        });
        this.networkLiveData.addSource(wifiLiveData, scanResults -> {
            for (ScanResult network : scanResults) {
                WifiNetwork wifiNetwork = new WifiNetwork();
                wifiNetwork.setName(network.SSID);
                wifiNetwork.setMacAddress(network.BSSID);
                wifiNetwork.setSignalStrength(calculateSignalLevel(network.level, 5));
                wifiNetwork.setEncryption(network.capabilities);
                wifiNetwork.setPosition(this.currentLocation.getLatitude() + "," + this.currentLocation.getLongitude());
                //TODO Need to solve dependency injection issue to save to database
//                networkRepository.saveNetwork(wifiNetwork);
                wifiNetworks.add(wifiNetwork);
            }
            networkLiveData.postValue(wifiNetworks);

        });
    }

    public MediatorLiveData<List<WifiNetwork>> getNetworkLiveData() {
        return networkLiveData;
    }
}
