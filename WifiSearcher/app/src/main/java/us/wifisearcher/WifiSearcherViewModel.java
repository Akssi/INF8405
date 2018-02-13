package us.wifisearcher;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import javax.inject.Inject;

import us.wifisearcher.persistence.WifiNetworkRepository;
import us.wifisearcher.persistence.database.WifiNetwork;
import us.wifisearcher.services.LocationLiveData;
import us.wifisearcher.services.WifiLiveData;

import static android.net.wifi.WifiManager.calculateSignalLevel;

public class WifiSearcherViewModel extends AndroidViewModel {

    private WifiNetworkRepository networkRepository;
    private WifiLiveData wifiLiveData;
    private LocationLiveData locationLiveData;
    private Location currentLocation;
    private Observer<Location> locationObserver = new Observer<Location>() {
        @Override
        public void onChanged(@Nullable Location location) {
            currentLocation = location;
        }
    };
    private Observer<List<ScanResult>> wifiObserver = new Observer<List<ScanResult>>() {
        @Override
        public void onChanged(@Nullable List<ScanResult> discoveredNetworks) {
            if (discoveredNetworks == null) {
                return;
            }
            for (ScanResult network : discoveredNetworks) {
                WifiNetwork wifiNetwork = new WifiNetwork();
                wifiNetwork.setName(network.SSID);
                wifiNetwork.setMacAddress(network.BSSID);
                wifiNetwork.setSignalStrength(calculateSignalLevel(network.level, 5));
                wifiNetwork.setEncryption(network.capabilities);
//                networkRepository.saveNetwork(wifiNetwork);
            }

        }
    };

    public WifiSearcherViewModel(@NonNull Application application) {
        super(application);
        locationLiveData = new LocationLiveData(application.getApplicationContext());
        wifiLiveData = new WifiLiveData(application.getApplicationContext());
        wifiLiveData.observeForever(wifiObserver);
    }


    @Inject
    public WifiSearcherViewModel(@NonNull Application application, WifiNetworkRepository wifiNetworkRepository) {
        super(application);
        networkRepository = wifiNetworkRepository;
        locationLiveData = new LocationLiveData(application.getApplicationContext());
        wifiLiveData = new WifiLiveData(application.getApplicationContext());
        wifiLiveData.observeForever(wifiObserver);
        locationLiveData.observeForever(locationObserver);
    }

    public LiveData<Location> getLocation() {
        return locationLiveData;
    }
}
