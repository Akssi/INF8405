package us.wifisearcher.services;

import android.arch.lifecycle.LiveData;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import us.wifisearcher.persistence.database.WifiNetwork;

import java.util.ArrayList;
import java.util.List;

import static android.net.wifi.WifiManager.calculateSignalLevel;

public class WifiLiveData extends LiveData<List<WifiNetwork>> {
    private WifiManager wifiManager;
    private Context context;
    private BroadcastReceiver broadcastReceiver;
    // Constants used for different security types
    private static final String WPA2 = "WPA2";
    private static final String WPA = "WPA";
    private static final String WEP = "WEP";
    private static final String OPEN = "Open";
    /* For EAP Enterprise fields */
    private static final String WPA_EAP = "WPA-EAP";
    private static final String IEEE8021X = "IEEE8021X";

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

    /**
     * @return The security of a given {@link ScanResult}.
     */
    public static String getScanResultSecurity(ScanResult scanResult) {
        final String cap = scanResult.capabilities;
        final String[] securityModes = {WEP, WPA, WPA2, WPA_EAP, IEEE8021X};
        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }

        return OPEN;
    }

    public void executeScan() {
        wifiManager.startScan();
    }

    @Override
    protected void onActive() {
        super.onActive();
        this.context.registerReceiver(this.broadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
//        executeScan();
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        this.context.unregisterReceiver(broadcastReceiver);
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
                wifiNetwork.setEncryption(getScanResultSecurity(scanResult));
                networks.add(wifiNetwork);
            }

            setValue(networks);
        }
    }
}
