package zelemon.zsx.dependencyInjection;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import zelemon.zsx.TronViewModel;

/**
 * Module that binds the custom ViewModelFactory the Apps ViewModels
 */
@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(TronViewModel.class)
    abstract ViewModel bindSearchViewModel(TronViewModel wifiSearcherViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(TronViewModelFactory tronViewModelFactory);
}