package us.wifisearcher;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MediatorLiveData;
import android.location.Location;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import us.wifisearcher.persistence.WifiNetworkRepository;
import us.wifisearcher.persistence.database.WifiNetwork;
import us.wifisearcher.services.LocationLiveData;
import us.wifisearcher.services.WifiLiveData;

public class WifiSearcherViewModel extends AndroidViewModel {

    private WifiNetworkRepository networkRepository;
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
            wifiNetworks.clear();
            for (WifiNetwork wifiNetwork : scanResults) {
                wifiNetwork.setLocation(this.currentLocation);
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

    public LocationLiveData getLocationLiveData() {
        return locationLiveData;
    }

    //public WifiNetwork getWifiNetwork(LatLng position) { return networkRepository.getNetwork(position); }
}
