package us.wifisearcher.persistence.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class WifiNetwork {
    @PrimaryKey
    private int id;

    private String name;

    private String passwordLockState;

    private String macAddress;

    private int signalStrength;

//    private LatLng position;

    private String keyType;

    private String encryption;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPasswordLockState() {
        return passwordLockState;
    }

    public void setPasswordLockState(String passwordLockState) {
        this.passwordLockState = passwordLockState;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(int signalStrength) {
        this.signalStrength = signalStrength;
    }

//    public LatLng getPosition() {
//        return position;
//    }

//    public void setPosition(LatLng position) {
//        this.position = position;
//    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getEncryption() {
        return encryption;
    }

    public void setEncryption(String encryption) {
        this.encryption = encryption;
    }
}
