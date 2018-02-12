package us.wifisearcher.services;

import android.arch.lifecycle.LiveData;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.List;

public class WifiLiveData extends LiveData<List<ScanResult>> {
    private WifiManager wifiManager;
    private Context context;
    private BroadcastReceiver broadcastReceiver;

    public WifiLiveData(Context context) {
        this.context = context;
        this.wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
        this.wifiManager.setWifiEnabled(true);
        this.broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onScanResults(context, intent);
            }
        };
    }

    private void onScanResults(Context context, Intent intent) {
        List<ScanResult> discoveredNetworks = wifiManager.getScanResults();
        if (discoveredNetworks.isEmpty()) {
            setValue(null);
        } else {
            setValue(discoveredNetworks);
        }
    }

    private void executeScan() {
        wifiManager.startScan();
    }

    @Override
    protected void onActive() {
        super.onActive();
        this.context.registerReceiver(this.broadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        executeScan();
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        this.context.unregisterReceiver(broadcastReceiver);
    }
}
