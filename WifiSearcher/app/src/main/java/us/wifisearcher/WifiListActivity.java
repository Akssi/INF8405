package us.wifisearcher;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;

public class WifiListActivity extends DaggerAppCompatActivity {

    @Inject
    ViewModelProvider.Factory viewModelFactory;
    private WifiSearcherViewModel viewModel;
    private RecyclerViewAdapter recyclerViewAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_list);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(WifiSearcherViewModel.class);

        recyclerView = findViewById(R.id.wifi_list_view);
        recyclerViewAdapter = new RecyclerViewAdapter(new ArrayList<>(), viewModel);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerViewAdapter);


        Resources res = getResources();
        recyclerViewAdapter.setWifi_mac_address(res.getString(R.string.wifi_mac_address));
        recyclerViewAdapter.setWifi_key_type(res.getString(R.string.wifi_key_type));
        recyclerViewAdapter.setWifi_encryption(res.getString(R.string.wifi_encryption));

        viewModel.getCurrentLocationWifiNetworks().observe(WifiListActivity.this, wifiNetworks -> recyclerViewAdapter.addItems(wifiNetworks));

    }
}
