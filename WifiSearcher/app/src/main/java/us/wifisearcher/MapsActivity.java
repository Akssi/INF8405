package us.wifisearcher;

import android.Manifest;
import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;
import us.wifisearcher.fragments.Card;
import us.wifisearcher.persistence.database.WifiNetwork;
import us.wifisearcher.services.BatteryLiveData;

public class MapsActivity extends DaggerAppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        Card.OnCardFragmentInteractionListener {
    private static final int PERMISSION_REQUEST_LOCATION = 0;
    private static boolean isStartupLaunch = true;
    private final Observer<List<WifiNetwork>> wifiNetworksObserver = wifiNetworks -> {
        if (!wifiNetworks.isEmpty()) {
            Toast.makeText(this, wifiNetworks.size() + " Wifi networks were found", Toast.LENGTH_SHORT).show();
        }
    };

    @Inject
    ViewModelProvider.Factory viewModelFactory;
    private LatLng currentLocation;
    private WifiSearcherViewModel viewModel;
    private GoogleMap mMap;
    private final Observer<Location> locationObserver = location -> {
        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(this.currentLocation).title("Home"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_LOCATION:
                if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    viewModel.getCurrentLocationWifiNetworksLiveData().observe(this, this.wifiNetworksObserver);
                    viewModel.getLocationLiveData().observe(this, this.locationObserver);
                }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get view model
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(WifiSearcherViewModel.class);

        if (isStartupLaunch) {
            BatteryLiveData.InitializeBatteryStatus(getApplicationContext());
            isStartupLaunch = false;
        }
        System.out.println("Creating activity");
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    public void switchToListView(View view) {
        Intent intent = new Intent(this, WifiListActivity.class);
        startActivity(intent);
    }

    public void switchToStatus(View view) {
        Intent intent = new Intent(this, StatusActivity.class);
        startActivity(intent);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        currentLocation = new LatLng(-34, 151);

        // Request Permission
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
        } else {
            viewModel.getCurrentLocationWifiNetworksLiveData().observe(this, this.wifiNetworksObserver);
            viewModel.getLocationLiveData().observe(this, this.locationObserver);
        }

        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(this);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        viewModel.getCurrentLocationWifiNetworksLiveData().observe(this, wifiNetworks -> {

            if (!wifiNetworks.isEmpty()) {
                String[] wifiNames = new String[wifiNetworks.size()];
                for (int i = 0; i < wifiNetworks.size(); i++) {
                    wifiNames[i] = wifiNetworks.get(i).getName();
                }
                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getResources().getString(R.string.wifi_card_list_title));

                // add a list
                builder.setItems(wifiNames, (DialogInterface dialog, int index) -> {
                    Card cardFragment = Card.newInstance(wifiNetworks.get(index));
                    cardFragment.show(getFragmentManager(), "Card fragment");
                });

                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                Toast.makeText(this, " No Wifi networks were found", Toast.LENGTH_SHORT).show();
            }

        });
        return true;
    }

    @Override
    public void onShareButtonPressed(WifiNetwork wifiNetwork) {

        Resources res = getResources();
        String textToSend = res.getString(R.string.wifi_sharing_text) + wifiNetwork.getName() + "\n" + " (dummy location)";
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, textToSend);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);

    }
}
