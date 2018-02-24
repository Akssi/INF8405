package us.wifisearcher.dependencyInjection;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import us.wifisearcher.fragments.Card;

@Module
public abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract Card cardFragment();
}
