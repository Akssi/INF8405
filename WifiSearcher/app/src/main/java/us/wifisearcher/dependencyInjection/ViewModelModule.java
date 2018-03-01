package us.wifisearcher.dependencyInjection;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import us.wifisearcher.WifiSearcherViewModel;

/**
 * Module that binds the custom ViewModelFactory the Apps ViewModels
 */
@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(WifiSearcherViewModel.class)
    abstract ViewModel bindSearchViewModel(WifiSearcherViewModel wifiSearcherViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(WifiSearcherViewModelFactory wifiSearcherViewModelFactory);
}