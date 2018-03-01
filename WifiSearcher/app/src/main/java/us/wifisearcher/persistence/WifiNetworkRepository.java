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

import static java.lang.Math.max;

/**
 * Class that queries the database.
 */
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

    /**
     * Saves a {@link WifiNetwork} in the database.
     *
     * @param wifiNetwork Network to save.
     */
    public void saveNetwork(WifiNetwork wifiNetwork) {
        this.refreshWifiNetwork(wifiNetwork);
    }

    private void refreshWifiNetwork(WifiNetwork wifiNetwork) {
        executor.execute(() -> {
            long id = wifiNetworkDao.save(wifiNetwork);
            if (id == -1) {
                WifiNetwork currentWifi = wifiNetworkDao.getNetworkByName(wifiNetwork.getName());
                // Favorite state unchanged. Take value currently in DB
                if (wifiNetwork.getFavorite() == -1) {
                    wifiNetwork.setFavorite(currentWifi.getFavorite());
                }
                // If network are close by save max signal strength
                if (wifiNetwork.getLocation().distanceTo(currentWifi.getLocation()) < SURROUNDING_WIFI_RANGE) {
                    wifiNetwork.setSignalStrength(max(wifiNetwork.getSignalStrength(), currentWifi.getSignalStrength()));
                }
                wifiNetworkDao.update(wifiNetwork);
            }
        });
    }

    /**
     * Gets the networks around a particular {@param location} given a radius.
     * @param location Location where to search.
     * @param radius Radius of the search.
     * @return The networks around a particular {@param location} given a {@param radius}.
     */
    public LiveData<List<WifiNetwork>> getSurroundingNetworks(Location location, int radius) {
        return getNetworksInsideRange(location, radius + SURROUNDING_WIFI_RANGE);
    }

    /**
     * Gets the networks around a particular {@param location} in a default radius.
     * @param location Location where to search.
     * @return The networks around a particular {@param location}.
     */
    public LiveData<List<WifiNetwork>> getSurroundingNetworks(Location location) {
        return getNetworksInsideRange(location, SURROUNDING_WIFI_RANGE);
    }

    /**
     * Gets the networks on the entire map.
     * @return The networks on the entire map.
     */
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
