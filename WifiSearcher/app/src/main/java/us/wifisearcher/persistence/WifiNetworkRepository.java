package us.wifisearcher.persistence;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.location.Location;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

import us.wifisearcher.persistence.database.WifiNetwork;
import us.wifisearcher.persistence.database.WifiNetworkDao;

@Singleton
public class WifiNetworkRepository {
    private static final int SURROUNDING_WIFI_RANGE = 100;
    private static final int WIFI_MAP_RANGE = 50000;
    private final WifiNetworkDao wifiNetworkDao;
    private final Executor executor;

    @Inject
    public WifiNetworkRepository(WifiNetworkDao wifiNetworkDao, Executor executor) {
        this.wifiNetworkDao = wifiNetworkDao;
        this.executor = executor;
    }

    public void saveNetwork(WifiNetwork wifiNetwork) {
        this.refreshWifiNetwork(wifiNetwork);
    }

    private void refreshWifiNetwork(WifiNetwork wifiNetwork) {
        executor.execute(() -> {
            long id = wifiNetworkDao.save(wifiNetwork);
            if (id == -1) {
                // Favorite state unchanged. Take value currently in DB
                if (wifiNetwork.getFavorite() == -1) {
                    WifiNetwork currentWifi = wifiNetworkDao.getNetworkByName(wifiNetwork.getName());
                    wifiNetwork.setFavorite(currentWifi.getFavorite());
                }
                wifiNetworkDao.update(wifiNetwork);
            }
        });
    }

    public LiveData<List<WifiNetwork>> getSurroundingNetworks(Location location, int radius) {
        return getNetworksInsideRange(location, radius + SURROUNDING_WIFI_RANGE);
    }

    public LiveData<List<WifiNetwork>> getSurroundingNetworks(Location location) {
        return getNetworksInsideRange(location, SURROUNDING_WIFI_RANGE);
    }

    public LiveData<List<WifiNetwork>> getMapNetworks(Location location) {
        return getNetworksInsideRange(location, WIFI_MAP_RANGE);
    }

    @NonNull
    private LiveData<List<WifiNetwork>> getNetworksInsideRange(Location location, int range) {
        return Transformations.map(wifiNetworkDao.getNetworks(), networks -> {
            List<WifiNetwork> closeByNetworks = new ArrayList<>();
            if (location != null) {
                for (WifiNetwork wifinetwork : networks) {
                    if (wifinetwork.getLocation().distanceTo(location) < range) {
                        closeByNetworks.add(wifinetwork);
                    }
                }
            }
            return closeByNetworks;
        });
    }
}
