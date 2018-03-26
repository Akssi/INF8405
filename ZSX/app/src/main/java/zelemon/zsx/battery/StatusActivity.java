package zelemon.zsx.battery;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import zelemon.zsx.R;

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
            StringBuilder batteryLevelText = new StringBuilder();
            batteryLevelText.append(batteryUsed);
            batteryLevelText.append(" %");
            batteryLevel.setText(batteryLevelText.toString());
        });
    }


}
