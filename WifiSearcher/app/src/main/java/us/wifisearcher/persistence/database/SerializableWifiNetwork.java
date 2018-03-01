package us.wifisearcher.persistence.database;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by olivier on 2018-02-28.
 */

public class SerializableWifiNetwork implements Serializable {

    @NonNull
    private String name;
    private String passwordLockState;

    @NonNull
    private String macAddress;
    private int signalStrength;

    private double latitude;
    private double longitude;
    private String keyType;
    private String encryption;

    public SerializableWifiNetwork(WifiNetwork wifiNetwork) {
        this.name = wifiNetwork.getName();
        this.passwordLockState = wifiNetwork.getPasswordLockState();
        this.macAddress = wifiNetwork.getMacAddress();
        this.signalStrength = wifiNetwork.getSignalStrength();
        this.keyType = wifiNetwork.getKeyType();
        this.encryption = wifiNetwork.getEncryption();
        this.latitude = wifiNetwork.getLocation().getLatitude();
        this.longitude = wifiNetwork.getLocation().getLongitude();
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public String getPasswordLockState() {
        return passwordLockState;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public String getKeyType() {
        return keyType;
    }

    public String getEncryption() {
        return encryption;
    }

}
