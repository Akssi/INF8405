package us.wifisearcher;

import android.Manifest;
import android.app.AlertDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
//import android.arch.lifecycle.ViewModelProvider;
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
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;
import us.wifisearcher.fragments.Card;
import us.wifisearcher.persistence.database.SerializableWifiNetwork;
import us.wifisearcher.persistence.database.WifiNetwork;
import us.wifisearcher.services.BatteryLiveData;

public class MapsActivity extends DaggerAppCompatActivity implements OnMapReadyCallback,
        Card.OnCardFragmentInteractionListener,
        ClusterManager.OnClusterClickListener<WifiMarker>, GoogleMap.OnMarkerClickListener {
    private static final int PERMISSION_REQUEST_LOCATION = 0;
    private static final String LOCKED_CHAR = "\uD83D\uDD12";
    private static final String UNLOCKED_CHAR = "\uD83D\uDD13";
    private static final String FAVORITE_CHAR = "\u2605";
    private static final String NOT_FAVORITE_CHAR = "\u2606";
    private static final String OPEN = "Open";
    private static boolean isStartupLaunch = true;
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

    // From StackOverflow : https://stackoverflow.com/questions/837872/calculate-distance-in-meters-when-you-know-longitude-and-latitude-in-java
    private static float distFrom(float lat1, float lng1, float lat2, float lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);

        return dist;
    }

    /**
     * Allows for camera to follow user position
     *
     * @param location
     */
    private void updateCurrentLocationOnMap(Location location) {
        currentCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentCoordinates, 15));
    }

    /**
     * Adds markers and clusters to the map given a list of known network
     * @param wifiNetworks known network
     */
    private void displayNetworksOnMap(List<WifiNetwork> wifiNetworks) {
        clusterManager.clearItems();
        for (WifiNetwork wifiNetwork : wifiNetworks) {
            LatLng wifiLocation = new LatLng(wifiNetwork.getLocation().getLatitude(), wifiNetwork.getLocation().getLongitude());
            WifiMarker wifiMarker = new WifiMarker(wifiLocation);
            clusterManager.addItem(wifiMarker);
        }
        clusterManager.cluster();
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
        clusterManager.setOnClusterClickListener(this);

        viewModel.initializeCurrentLocationWifiNetworkLiveData();
        viewModel.getLocationLiveData().observe(this, this.locationObserver);
        viewModel.getMapWifiNetworks().observe(this, this.mapWifiObserver);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get view model
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(WifiSearcherViewModel.class);

        // Runs only once at startup
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

    /**
     * This function starts the WifiListActivity
     *
     * @param view The view of the button.
     */
    public void switchToListView(View view) {
        Intent intent = new Intent(this, WifiListActivity.class);
        startActivity(intent);
    }

    /**
     * This function starts the StatusActivity
     * @param view The view of the button.
     */
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
        mMap.setOnMarkerClickListener(this);
    }

    /**
     * This function is called when a marker is clicked.
     * It queries the database for networks to ultimately show them to the user.
     * @param marker The marker that was clicked on.
     * @return true if the listener has consumed the event (i.e., the default behavior should not occur); false otherwise
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        this.markerLocation.setLatitude(marker.getPosition().latitude);
        this.markerLocation.setLongitude(marker.getPosition().longitude);
        mWifiNetworks = viewModel.getWifiNetworksSurroundingLocation(this.markerLocation);
        mWifiNetworks.observe(this, this.wifiCardObserver);
        return true;
    }

    /**
     * This function is called when a cluster is clicked.
     * It queries the database for networks to ultimately show them to the user.
     * @param cluster The cluster that was clicked on.
     * @return true if the listener has consumed the event (i.e., the default behavior should not occur); false otherwise
     */
    @Override
    public boolean onClusterClick(Cluster<WifiMarker> cluster) {
        LatLng clusterPos = cluster.getPosition();
        double radius = 0;
        for (WifiMarker marker : cluster.getItems()) {
            LatLng markerPos = marker.getPosition();
            float distance = distFrom((float) markerPos.latitude, (float) markerPos.longitude, (float) clusterPos.latitude, (float) clusterPos.longitude);
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

    /**
     * Opens a card containing the list of wifi on the marker/cluster selected
     * @param wifiNetworks
     */
    private void showNetworksOnCard(List<WifiNetwork> wifiNetworks) {
        if (wifiNetworks.size() > 1) {

            String[] wifiNames = new String[wifiNetworks.size()];
            for (int i = 0; i < wifiNetworks.size(); i++) {
                WifiNetwork wifiNetwork = wifiNetworks.get(i);

                // Add favorite character
                if (wifiNetwork.getFavorite() == 1) {
                    wifiNames[i] = FAVORITE_CHAR;
                } else {
                    wifiNames[i] = NOT_FAVORITE_CHAR;
                }

                // Add lock character
                if (wifiNetwork.getKeyType().equals(OPEN)) {
                    wifiNames[i] += UNLOCKED_CHAR;
                } else {
                    wifiNames[i] += LOCKED_CHAR;
                }

                wifiNames[i] += "\r" + wifiNetwork.getName();
            }
            // setup the alert builder
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.wifi_card_list_title));

            // add a list
            builder.setItems(wifiNames, (DialogInterface dialog, int index) -> {
                dialog.cancel();
                Card cardFragment = Card.newInstance(new SerializableWifiNetwork(wifiNetworks.get(index)));
                cardFragment.show(getFragmentManager(), "Card fragment");
            });

            // create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        } else if (!wifiNetworks.isEmpty()) {
            Card cardFragment = Card.newInstance(new SerializableWifiNetwork(wifiNetworks.get(0)));
            cardFragment.show(getFragmentManager(), "Card fragment");
        } else {
            Toast.makeText(this, " No Wifi networks were found", Toast.LENGTH_SHORT).show();
        }
        mWifiNetworks.removeObserver(this.wifiCardObserver);
    }

    /**
     * Shares the name and position of a network.
     * @param wifiNetwork The network to share.
     */
    @Override
    public void onShareButtonPressed(SerializableWifiNetwork wifiNetwork) {
        Resources res = getResources();
        String textToSend = res.getString(R.string.wifi_sharing_text) + wifiNetwork.getName() + "\n" + res.getString(R.string.wifi_position_descriptor) + wifiNetwork.getLatitude() + "," + wifiNetwork.getLongitude();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, textToSend);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    /**
     * Starts the google map navigation with the location of a network
     * @param wifiNetwork The network to navigate to.
     */
    @Override
    public void onNavigationButtonPressed(SerializableWifiNetwork wifiNetwork) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + wifiNetwork.getLatitude() + "," + wifiNetwork.getLongitude());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }


    /**
     * Sets a particular network as favorite.
     * @param serializableWifiNetwork The network to be set as favorite.
     */
    @Override
    public void onFavoriteButtonPressed(SerializableWifiNetwork serializableWifiNetwork) {
        Location location = new Location(new Location(LocationManager.GPS_PROVIDER));
        location.setLatitude(serializableWifiNetwork.getLatitude());
        location.setLongitude(serializableWifiNetwork.getLongitude());
        WifiNetwork wifiNetwork = new WifiNetwork();
        wifiNetwork.setLocation(location);
        wifiNetwork.setPasswordLockState(serializableWifiNetwork.getPasswordLockState());
        wifiNetwork.setKeyType(serializableWifiNetwork.getKeyType());
        wifiNetwork.setEncryption(serializableWifiNetwork.getEncryption());
        wifiNetwork.setMacAddress(serializableWifiNetwork.getMacAddress());
        wifiNetwork.setName(serializableWifiNetwork.getName());
        wifiNetwork.setFavorite(serializableWifiNetwork.getFavorite());
        viewModel.updateWifiNetwork(wifiNetwork);
    }
}
