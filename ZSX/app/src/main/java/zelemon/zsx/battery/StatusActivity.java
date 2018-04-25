package zelemon.zsx.battery;

import android.net.TrafficStats;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import zelemon.zsx.R;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Activity that shows the battery consumption of the app.
 */
public class StatusActivity extends AppCompatActivity {


    private TextView batteryLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BatteryLiveData batteryLiveData = new BatteryLiveData(this);
        setContentView(R.layout.activity_status);
        batteryLevel = findViewById(R.id.battery_since_startup);
        batteryLiveData.observe(this, batteryUsed -> {
            String batteryLevelText = String.valueOf(batteryUsed) +
                    " %";
            batteryLevel.setText(batteryLevelText);
        });
    }

    /**
     * Read UID Rx Bytes
     *
     * @param uid
     * @return rxBytes
     */
    public Long getUidRxBytes(int uid) {
        BufferedReader reader;
        Long rxBytes = 0L;
        try {
            reader = new BufferedReader(new FileReader("/proc/uid_stat/" + uid
                    + "/tcp_rcv"));
            rxBytes = Long.parseLong(reader.readLine());
            reader.close();
        } catch (FileNotFoundException e) {
            rxBytes = TrafficStats.getUidRxBytes(uid);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rxBytes;
    }

    /**
     * Read UID Tx Bytes
     *
     * @param uid
     * @return txBytes
     */
    public Long getUidTxBytes(int uid) {
        BufferedReader reader;
        Long txBytes = 0L;
        try {
            reader = new BufferedReader(new FileReader("/proc/uid_stat/" + uid
                    + "/tcp_snd"));
            txBytes = Long.parseLong(reader.readLine());
            reader.close();
        } catch (FileNotFoundException e) {
            txBytes = TrafficStats.getUidTxBytes(uid);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return txBytes;
    }


}
