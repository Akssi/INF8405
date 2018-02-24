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
                float currentBateryLevel = 100 * (level / (float) scale);
                setValue(startupBatteryLevel - currentBateryLevel);

                // &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& EXTRA FLUFF THAT WE MAY OR MAY NOT WANT &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
//                int temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
//                int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
//                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
//
//                String BStatus = "No Data";
//                if (status == BatteryManager.BATTERY_STATUS_CHARGING){BStatus = "Charging";}
//                if (status == BatteryManager.BATTERY_STATUS_DISCHARGING){BStatus = "Discharging";}
//                if (status == BatteryManager.BATTERY_STATUS_FULL){BStatus = "Full";}
//                if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING){BStatus = "Not Charging";}
//                if (status == BatteryManager.BATTERY_STATUS_UNKNOWN){BStatus = "Unknown";}
//
//                int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
//                String BattPowerSource = "No Data";
//                if (chargePlug == BatteryManager.BATTERY_PLUGGED_AC){BattPowerSource = "AC";}
//                if (chargePlug == BatteryManager.BATTERY_PLUGGED_USB){BattPowerSource = "USB";}
//
//                String BattLevel = String.valueOf(level);
//
//                int BHealth = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
//                String BatteryHealth = "No Data";
//                if (BHealth == BatteryManager.BATTERY_HEALTH_COLD){BatteryHealth = "Cold";}
//                if (BHealth == BatteryManager.BATTERY_HEALTH_DEAD){BatteryHealth = "Dead";}
//                if (BHealth == BatteryManager.BATTERY_HEALTH_GOOD){BatteryHealth = "Good";}
//                if (BHealth == BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE){BatteryHealth = "Over-Voltage";}
//                if (BHealth == BatteryManager.BATTERY_HEALTH_OVERHEAT){BatteryHealth = "Overheat";}
//                if (BHealth == BatteryManager.BATTERY_HEALTH_UNKNOWN){BatteryHealth = "Unknown";}
//                if (BHealth == BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE){BatteryHealth = "Unspecified Failure";}
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
