package us.wifisearcher.persistence.database;

import android.arch.persistence.room.Entity;
import android.location.Location;
import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Class that contains the information of a network. Defines Room database table
 */
@Entity(primaryKeys = {"name"})
public class WifiNetwork implements Serializable {

    @NonNull
    private String name;

    private String passwordLockState;

    @NonNull
    private String macAddress;

    private int signalStrength;


    private Location location;
    private String keyType;
    private String encryption;
    // -1 = Unchanged, 1 = Favorite, 0 = Not Favorite
    private int favorite = -1;

    public WifiNetwork() {
        this.name = "N/A";
        this.passwordLockState = "Unknown password state";
        this.macAddress = "N/A";
        this.signalStrength = -1;
        this.keyType = "N/A";
        this.encryption = "N/A";
    }

    public int getFavorite() {
        return favorite;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
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
