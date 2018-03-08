package us.wifisearcher.dependencyInjection;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import us.wifisearcher.fragments.Card;

/**
 * Class that builds the required Fragments
 */
@Module
public abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract Card cardFragment();
}
