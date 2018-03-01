package us.wifisearcher.services;

import android.arch.lifecycle.LiveData;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;


public class BatteryLiveData extends LiveData<Float> {
    private static float startupBatteryLevel = -1;
    private final Context context;
    private BroadcastReceiver batteryEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            try {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float currentBatteryLevel = 100 * (level / (float) scale);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                if (status != BatteryManager.BATTERY_STATUS_DISCHARGING) {
                    startupBatteryLevel = currentBatteryLevel;
                    setValue(0.0f);
                } else {
                    float batteryDelta = (startupBatteryLevel - currentBatteryLevel);
                    if (batteryDelta >= 0) {
                        setValue(batteryDelta);
                    }
                }
            } catch (Exception e) {
                Log.v("Battery", "Battery Info Error: \n".concat(e.getMessage()));
            }
        }
    };

    public BatteryLiveData(Context context) {
        this.context = context;
        if (startupBatteryLevel == -1) {
            InitializeBatteryStatus(context);
        }
        context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    public static void InitializeBatteryStatus(Context context) {

        Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = 1;
        if (batteryStatus != null) {
            level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        }
        int scale = 1;
        if (batteryStatus != null) {
            scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        }

        startupBatteryLevel = 100 * (level / (float) scale);
    }

    @Override
    protected void onActive() {
        super.onActive();
        context.registerReceiver(batteryEventReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onInactive() {
        context.unregisterReceiver(batteryEventReceiver);
    }
}
