package us.wifisearcher.dependencyInjection;


import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import us.wifisearcher.MapsActivity;
import us.wifisearcher.StatusActivity;
import us.wifisearcher.WifiListActivity;

/**
 * Class that defines the acitivites that compose the App
 */
@Module
public abstract class MainActivityModule {
    @ContributesAndroidInjector(modules = FragmentBuildersModule.class)
    abstract MapsActivity contributeMapsActivity();

    @ContributesAndroidInjector(modules = FragmentBuildersModule.class)
    abstract WifiListActivity contributeListActivity();

    @ContributesAndroidInjector(modules = FragmentBuildersModule.class)
    abstract StatusActivity contributeStatusActivity();
}
