package us.wifisearcher.persistence;

import android.location.Location;
import us.wifisearcher.persistence.database.WifiNetwork;
import us.wifisearcher.persistence.database.WifiNetworkDao;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.Executor;

@Singleton
public class WifiNetworkRepository {
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
        executor.execute(() -> wifiNetworkDao.save(wifiNetwork));
    }

    public WifiNetwork getNetwork(Location position) {
        return wifiNetworkDao.load(position).getValue();
    }
}
