package us.wifisearcher.dependencyInjection;

import android.app.Application;
import android.arch.persistence.room.Room;
import dagger.Module;
import dagger.Provides;
import us.wifisearcher.persistence.database.WifiDatabase;
import us.wifisearcher.persistence.database.WifiNetworkDao;
import us.wifisearcher.services.LocationLiveData;
import us.wifisearcher.services.WifiLiveData;

import javax.inject.Singleton;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Module(includes = ViewModelModule.class)
class AppModule {

    @Singleton
    @Provides
    WifiDatabase provideWifiDatabase(Application application) {
        return Room.databaseBuilder(application, WifiDatabase.class, "wifi.db").fallbackToDestructiveMigration().build();
    }

    @Singleton
    @Provides
    WifiNetworkDao provideWifiNetworkDao(WifiDatabase wifiDatabase) {
        return wifiDatabase.getWifiNetworkDao();
    }

    @Provides
    @Singleton
    Executor provideSingleThreadExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @Singleton
    @Provides
    WifiLiveData provideWifiLiveData(Application application) {
        return new WifiLiveData(application.getApplicationContext());
    }

    @Singleton
    @Provides
    LocationLiveData provideLocationLiveData(Application application) {
        return new LocationLiveData(application.getApplicationContext());
    }
}
