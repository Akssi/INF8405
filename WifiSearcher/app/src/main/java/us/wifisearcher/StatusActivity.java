package us.wifisearcher;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class StatusActivity extends AppCompatActivity {

    private WifiSearcherViewModel viewModel;
    private TextView batteryLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        batteryLevel = findViewById(R.id.battery_since_startup);
        viewModel = ViewModelProviders.of(this).get(WifiSearcherViewModel.class);
        viewModel.getBatteryLiveData().observe(this, new Observer<Float>() {
            @Override
            public void onChanged(@Nullable Float batteryUsed) {
                StringBuilder batteryLevelText = new StringBuilder();
                batteryLevelText.append(batteryUsed);
                batteryLevelText.append(" %");
                batteryLevel.setText(batteryLevelText.toString());
            }
        });
    }


}
