package zelemon.zsx.dependencyInjection;

import android.app.Application;
import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.support.AndroidSupportInjectionModule;
import zelemon.zsx.ZsxApplication;

import javax.inject.Singleton;

/**
 * Class that builds all the apps Dependencies
 */
@Singleton
@Component(modules = {MainActivityModule.class, AndroidInjectionModule.class, AppModule.class, AndroidSupportInjectionModule.class})
public interface AppComponent {
    void inject(ZsxApplication wifiSearcherApplication);

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }
}
