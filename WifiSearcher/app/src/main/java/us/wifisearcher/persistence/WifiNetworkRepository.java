package us.wifisearcher.persistence;

import us.wifisearcher.persistence.database.WifiNetwork;
import us.wifisearcher.persistence.database.WifiNetworkDao;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.Executor;

@Singleton
public class WifiNetworkRepository {
    private final WifiNetworkDao wifiNetworkDao;

    @Inject
    public WifiNetworkRepository(WifiNetworkDao wifiNetworkDao, Executor executor) {
        this.wifiNetworkDao = wifiNetworkDao;
    }

    public void saveNetwork(WifiNetwork wifiNetwork) {
        wifiNetworkDao.save(wifiNetwork);
    }
}
