package zelemon.zsx.dependencyInjection;

import android.arch.lifecycle.ViewModel;
import dagger.MapKey;

import java.lang.annotation.*;

/**
 * Class needed to differentiate ViewModels in ViewModeModule
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@MapKey
@interface ViewModelKey {
    Class<? extends ViewModel> value();
}
