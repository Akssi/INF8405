package us.wifisearcher.persistence.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.location.Location;

import java.io.Serializable;

@Entity
public class WifiNetwork implements Serializable {
    @PrimaryKey
    private int id;

    private String name;

    private String passwordLockState;

    private String macAddress;

    private int signalStrength;
    private Location location;
    private String keyType;
    private String encryption;

    public WifiNetwork() {
        this.id = -1;
        this.name = "N/A";
        this.passwordLockState = "N/A";
        this.macAddress = "N/A";
        this.signalStrength = -1;
        this.keyType = "N/A";
        this.encryption = "N/A";
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

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
