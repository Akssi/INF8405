package us.wifisearcher;

import android.arch.lifecycle.ViewModelProviders;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import us.wifisearcher.persistence.database.WifiNetwork;

public class WifiListActivity extends AppCompatActivity {

    private WifiSearcherViewModel viewModel;
    private RecyclerViewAdapter recyclerViewAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_list);

        recyclerView = findViewById(R.id.wifi_list_view);
        recyclerViewAdapter = new RecyclerViewAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerViewAdapter);

        viewModel = ViewModelProviders.of(this).get(WifiSearcherViewModel.class);

        // TEMP %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        WifiNetwork w1 = new WifiNetwork();
        w1.setName("Wifi test 1");
        w1.setMacAddress("10:10:10:10");
        w1.setSignalStrength(5);
        WifiNetwork w2 = new WifiNetwork();
        w2.setName("Wifi test 2");
        w2.setMacAddress("20:20:20:20");
        w2.setSignalStrength(3);
        WifiNetwork w3 = new WifiNetwork();
        w3.setName("Wifi test 3");
        w3.setMacAddress("30:30:30:30");
        w3.setSignalStrength(4);

        List<WifiNetwork> defaultWifi = new ArrayList<>();
        defaultWifi.add(w1);
        defaultWifi.add(w2);
        defaultWifi.add(w3);
        recyclerViewAdapter.addItems(defaultWifi);
        // ^^^^ %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

        Resources res = getResources();
        recyclerViewAdapter.setWifi_mac_address(res.getString(R.string.wifi_mac_address));
        recyclerViewAdapter.setWifi_key_type(res.getString(R.string.wifi_key_type));
        recyclerViewAdapter.setWifi_encryption(res.getString(R.string.wifi_encryption));

        viewModel.getNetworkLiveData().observe(WifiListActivity.this, wifiNetworks -> recyclerViewAdapter.addItems(wifiNetworks));

    }
}
