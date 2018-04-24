package zelemon.zsx.dependencyInjection;


import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import zelemon.zsx.MapsActivity;
import zelemon.zsx.ProfileActivity;
import zelemon.zsx.battery.StatusActivity;

/**
 * Class that defines the acitivites that compose the App
 */
@Module
public abstract class MainActivityModule {
    @ContributesAndroidInjector(modules = FragmentBuildersModule.class)
    abstract MapsActivity contributeMapsActivity();

    @ContributesAndroidInjector(modules = FragmentBuildersModule.class)
    abstract ProfileActivity contributeProfileActivity();

    @ContributesAndroidInjector(modules = FragmentBuildersModule.class)
    abstract StatusActivity contributeStatusActivity();
}
