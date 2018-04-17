package zelemon.zsx.dependencyInjection;

import android.app.Application;
import dagger.Module;
import dagger.Provides;
import zelemon.zsx.battery.BatteryLiveData;
import zelemon.zsx.services.LocationLiveData;

import javax.inject.Singleton;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Class that provides the required dependencies for the App
 */
@Module(includes = ViewModelModule.class)
class AppModule {

    @Provides
    @Singleton
    Executor provideSingleThreadExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @Singleton
    @Provides
    LocationLiveData provideLocationLiveData(Application application) {
        return new LocationLiveData(application.getApplicationContext());
    }

    @Singleton
    @Provides
    BatteryLiveData provideBatteryLiveData(Application application) {
        return new BatteryLiveData(application.getApplicationContext());
    }
}
