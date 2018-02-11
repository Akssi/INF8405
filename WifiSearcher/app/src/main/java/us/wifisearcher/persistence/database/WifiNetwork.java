package us.wifisearcher.persistence.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import com.google.android.gms.maps.model.LatLng;

@Entity
public class WifiNetwork {
    @PrimaryKey
    private int id;

    private String name;

    private String passwordLockState;

    private String macAdress;

    private String signalStregth;

    private LatLng position;

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

    public String getMacAdress() {
        return macAdress;
    }

    public void setMacAdress(String macAdress) {
        this.macAdress = macAdress;
    }

    public String getSignalStregth() {
        return signalStregth;
    }

    public void setSignalStregth(String signalStregth) {
        this.signalStregth = signalStregth;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
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
