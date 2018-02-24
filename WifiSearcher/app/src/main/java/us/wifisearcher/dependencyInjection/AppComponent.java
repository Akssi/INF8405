package us.wifisearcher.dependencyInjection;

import android.app.Application;
import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.support.AndroidSupportInjectionModule;
import us.wifisearcher.WifiSearcherApplication;

import javax.inject.Singleton;

@Singleton
@Component(modules = {MainActivityModule.class, AndroidInjectionModule.class, AppModule.class, AndroidSupportInjectionModule.class})
public interface AppComponent {
    void inject(WifiSearcherApplication wifiSearcherApplication);

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }
}
