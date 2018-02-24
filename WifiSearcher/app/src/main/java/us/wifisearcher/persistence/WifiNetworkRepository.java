package us.wifisearcher.persistence;

import android.location.Location;
import us.wifisearcher.persistence.database.WifiNetwork;
import us.wifisearcher.persistence.database.WifiNetworkDao;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WifiNetworkRepository {
    private final WifiNetworkDao wifiNetworkDao;

    @Inject
    public WifiNetworkRepository(WifiNetworkDao wifiNetworkDao) {
        this.wifiNetworkDao = wifiNetworkDao;
    }

    public void saveNetwork(WifiNetwork wifiNetwork) {
        wifiNetworkDao.save(wifiNetwork);
    }

    public WifiNetwork getNetwork(Location position) {
        return wifiNetworkDao.load(position).getValue();
    }
}
