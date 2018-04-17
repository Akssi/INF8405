package zelemon.zsx;

import android.app.Activity;
import android.app.Application;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import zelemon.zsx.dependencyInjection.AppInjector;

import javax.inject.Inject;

public class ZsxApplication extends Application implements HasActivityInjector {

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;

    @Override
    public void onCreate() {
        super.onCreate();
        AppInjector.init(this);
    }

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }

}
