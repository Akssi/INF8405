package us.wifisearcher;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Transformations;
import android.location.Location;
import android.support.annotation.NonNull;
import us.wifisearcher.persistence.WifiNetworkRepository;
import us.wifisearcher.persistence.database.WifiNetwork;
import us.wifisearcher.services.LocationLiveData;
import us.wifisearcher.services.WifiLiveData;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class WifiSearcherViewModel extends AndroidViewModel {

    private WifiNetworkRepository networkRepository;
    private WifiLiveData wifiLiveData;
    private LocationLiveData locationLiveData;
    private MediatorLiveData<List<WifiNetwork>> networkLiveData;
    private Location currentLocation;
    private List<WifiNetwork> wifiNetworks;

    @Inject
    public WifiSearcherViewModel(@NonNull Application application, @NonNull WifiNetworkRepository wifiNetworkRepository, @NonNull LocationLiveData locationLiveData, @NonNull WifiLiveData wifiLiveData) {
        super(application);
        this.networkRepository = wifiNetworkRepository;
        this.locationLiveData = locationLiveData;
        this.wifiLiveData = wifiLiveData;
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
            for (WifiNetwork wifiNetwork : scanResults) {
                wifiNetwork.setLocation(this.currentLocation);
                networkRepository.saveNetwork(wifiNetwork);
            }
            networkLiveData.postValue(wifiNetworks);

        });
        this.networkLiveData.addSource(this.getCurrentNetworks(), closeByWifiNetworks -> networkLiveData.postValue(closeByWifiNetworks));
    }

    public MediatorLiveData<List<WifiNetwork>> getNetworkLiveData() {
        return networkLiveData;
    }

    public LocationLiveData getLocationLiveData() {
        return locationLiveData;
    }


    private LiveData<List<WifiNetwork>> getCurrentNetworks() {
        return Transformations.switchMap(locationLiveData, location -> {
            this.currentLocation = location;
            return this.networkRepository.getNetworks(this.currentLocation);
        });
    }

}
