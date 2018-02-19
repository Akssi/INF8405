package us.wifisearcher.persistence;

import javax.inject.Inject;
import javax.inject.Singleton;

import us.wifisearcher.persistence.database.WifiNetwork;
import us.wifisearcher.persistence.database.WifiNetworkDao;

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
}