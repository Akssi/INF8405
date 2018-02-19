package us.wifisearcher.services;

import android.arch.lifecycle.LiveData;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.List;

import us.wifisearcher.persistence.database.WifiNetwork;

import static android.net.wifi.WifiManager.calculateSignalLevel;

public class WifiLiveData extends LiveData<List<WifiNetwork>> {
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
            List<WifiNetwork> networks = new ArrayList<>();
            for (ScanResult scanResult : discoveredNetworks) {
                WifiNetwork wifiNetwork = new WifiNetwork();
                wifiNetwork.setName(scanResult.SSID);
                wifiNetwork.setMacAddress(scanResult.BSSID);
                wifiNetwork.setSignalStrength(calculateSignalLevel(scanResult.level, 5));
                wifiNetwork.setEncryption(scanResult.capabilities);
            }

            setValue(networks);
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
