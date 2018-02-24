package us.wifisearcher.dependencyInjection;


import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import us.wifisearcher.MapsActivity;
import us.wifisearcher.StatusActivity;
import us.wifisearcher.WifiListActivity;

@Module
public abstract class MainActivityModule {
    @ContributesAndroidInjector(modules = FragmentBuildersModule.class)
    abstract MapsActivity contributeMapsActivity();

    @ContributesAndroidInjector(modules = FragmentBuildersModule.class)
    abstract WifiListActivity contributeListActivity();

    @ContributesAndroidInjector(modules = FragmentBuildersModule.class)
    abstract StatusActivity contributeStatusActivity();
}
