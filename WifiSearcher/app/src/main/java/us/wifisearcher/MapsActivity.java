package us.wifisearcher;

import android.Manifest;
import android.app.AlertDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
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
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;
import us.wifisearcher.fragments.Card;
import us.wifisearcher.persistence.database.WifiNetwork;
import us.wifisearcher.services.BatteryLiveData;

public class MapsActivity extends DaggerAppCompatActivity implements OnMapReadyCallback,
        /*GoogleMap.OnMarkerClickListener,*/
        Card.OnCardFragmentInteractionListener,
        ClusterManager.OnClusterClickListener<WifiMarker> {
    private static final int PERMISSION_REQUEST_LOCATION = 0;
    private static boolean isStartupLaunch = true;
    private final Observer<List<WifiNetwork>> wifiToastObserver = this::foundNetworksToast;
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    private LiveData<List<WifiNetwork>> mWifiNetworks;
    private final Observer<List<WifiNetwork>> wifiCardObserver = this::showNetworksOnCard;
    private LatLng currentCoordinates;
    private Location markerLocation = new Location(LocationManager.GPS_PROVIDER);
    private WifiSearcherViewModel viewModel;
    private ClusterManager<WifiMarker> clusterManager;
    private final Observer<List<WifiNetwork>> mapWifiObserver = this::displayNetworksOnMap;
    private GoogleMap mMap;
    private final Observer<Location> locationObserver = this::updateCurrentLocationOnMap;

    private void updateCurrentLocationOnMap(Location location) {
        currentCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentCoordinates, 15));
    }

    private void foundNetworksToast(List<WifiNetwork> wifiNetworks) {
        if (!wifiNetworks.isEmpty()) {
            Toast.makeText(this, wifiNetworks.size() + " Wifi networks were found", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayNetworksOnMap(List<WifiNetwork> wifiNetworks) {
        clusterManager.clearItems();
        for (WifiNetwork wifiNetwork : wifiNetworks) {
            LatLng wifiLocation = new LatLng(wifiNetwork.getLocation().getLatitude(), wifiNetwork.getLocation().getLongitude());
            WifiMarker wifiMarker = new WifiMarker(wifiLocation);
            clusterManager.addItem(wifiMarker);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_LOCATION:
                if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    initializeActivityObservers();
                }
        }
    }

    private void initializeActivityObservers() {
        clusterManager = new ClusterManager<>(this, mMap);
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);
        clusterManager.setOnClusterClickListener(this);

        viewModel.getCurrentLocationWifiNetworksLiveData().observe(this, this.wifiToastObserver);
        viewModel.getLocationLiveData().observe(this, this.locationObserver);
        viewModel.getMapWifiNetworks().observe(this, this.mapWifiObserver);
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
        currentCoordinates = new LatLng(-34, 151);

        // Request Permission
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
        } else {
            initializeActivityObservers();
        }

        // Set a listener for marker click.
        //mMap.setOnMarkerClickListener(this);

    }

//    @Override
//    public boolean onMarkerClick(Marker marker) {
//
////        this.markerLocation.setLatitude(marker.getPosition().latitude);
////        this.markerLocation.setLongitude(marker.getPosition().longitude);
////
////        mWifiNetworks = viewModel.getWifiNetworksSurroundingLocation(this.markerLocation);
////        mWifiNetworks.observe(this, this.wifiCardObserver);
//
//        return true;
//    }

    @Override
    public boolean onClusterClick(Cluster<WifiMarker> cluster) {
        LatLng clusterPos = cluster.getPosition();
        double radius = 0;
        for (WifiMarker marker : cluster.getItems()) {
            LatLng markerPos = marker.getPosition();
            double distance = Math.sqrt(Math.pow(markerPos.latitude - clusterPos.latitude, 2) + Math.pow(markerPos.longitude - clusterPos.longitude, 2));
            if (distance > radius) {
                radius = distance;
            }
        }

        this.markerLocation.setLatitude(cluster.getPosition().latitude);
        this.markerLocation.setLongitude(cluster.getPosition().longitude);

        mWifiNetworks = viewModel.getWifiNetworksSurroundingLocation(this.markerLocation, (int) radius);
        mWifiNetworks.observe(this, this.wifiCardObserver);

        return true;

    }

    private void showNetworksOnCard(List<WifiNetwork> wifiNetworks) {
        if (wifiNetworks.size() > 1) {

            List<WifiNetwork> uniqueWififNetworkList = new ArrayList<>();
            HashMap<String, ArrayList<String>> uniqueSSIDMacAddressesMap = new HashMap<>();

            // Filter same SSID network
            for (WifiNetwork wififNetwork : wifiNetworks) {
                // If empty SSID skip it
                if (wififNetwork.getName().isEmpty()) {
                    continue;
                }
                if (uniqueSSIDMacAddressesMap.containsKey(wififNetwork.getName())) {
                    uniqueSSIDMacAddressesMap.get(wififNetwork.getName()).add(wififNetwork.getMacAddress());
                } else {
                    uniqueSSIDMacAddressesMap.put(wififNetwork.getName(), new ArrayList<>());
                    uniqueWififNetworkList.add(wififNetwork);
                }
            }
            String[] wifiNames = new String[uniqueWififNetworkList.size()];
            for (int i = 0; i < uniqueWififNetworkList.size(); i++) {
                WifiNetwork wifiNetwork = uniqueWififNetworkList.get(i);
                wifiNames[i] = wifiNetwork.getName();
                // Build MAC address string with list of MAC address from same SSID
                StringBuilder macAddressListString = new StringBuilder();
                macAddressListString.append(wifiNetwork.getMacAddress());
                for (String macAddress : uniqueSSIDMacAddressesMap.get(wifiNetwork.getName())) {
                    macAddressListString.append(",\n");
                    macAddressListString.append(macAddress);
                }
                wifiNetwork.setMacAddress(macAddressListString.toString());
            }
            // setup the alert builder
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.wifi_card_list_title));

            // add a list
            builder.setItems(wifiNames, (DialogInterface dialog, int index) -> {
                dialog.cancel();
                Card cardFragment = Card.newInstance(wifiNetworks.get(index));
                cardFragment.show(getFragmentManager(), "Card fragment");
            });

            // create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        } else if (!wifiNetworks.isEmpty()) {
            Card cardFragment = Card.newInstance(wifiNetworks.get(0));
            cardFragment.show(getFragmentManager(), "Card fragment");
        } else {
            Toast.makeText(this, " No Wifi networks were found", Toast.LENGTH_SHORT).show();
        }
        mWifiNetworks.removeObserver(this.wifiCardObserver);
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

    @Override
    public void onNavigationButtonPressed(WifiNetwork wifiNetwork) {
        Location location = wifiNetwork.getLocation();

        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + location.getLatitude() + "," + location.getLongitude());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }
}
