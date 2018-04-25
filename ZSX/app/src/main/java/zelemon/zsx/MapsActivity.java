package zelemon.zsx;

import android.Manifest;
import android.app.AlertDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;
import zelemon.zsx.Fragments.CardFragment;
import zelemon.zsx.dependencyInjection.TronViewModelFactory;
import zelemon.zsx.persistence.database.Profile;

public class MapsActivity extends DaggerAppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,ClusterManager.OnClusterClickListener<PlayerMarker> {

    private static final int PERMISSION_REQUEST_LOCATION = 0;
    @Inject
    TronViewModelFactory viewModelFactory;
    private GoogleMap mMap;
    private LiveData<List<Profile>> mProfiles;
    private final Observer<List<Profile>> profileCardObserver = this::showProfilesOnCard;
    private LatLng currentCoordinates;
    private final Observer<Location> locationObserver = this::updateCurrentLocationOnMap;
    private TronViewModel tronViewModel;
    private Location markerLocation = new Location(LocationManager.GPS_PROVIDER);
    private ClusterManager<PlayerMarker> clusterManager;
    private final Observer<List<Profile>> mapProfileObserver = this::displayProfilesOnMap;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // Get view model
        tronViewModel = ViewModelProviders.of(this, viewModelFactory).get(TronViewModel.class);
    }

    private void initializeObserver() {
        tronViewModel.getLocationLiveData().observe(this, this.locationObserver);
        tronViewModel.getMapProfiles().observe(this, this.mapProfileObserver);

        clusterManager = new ClusterManager<>(this, mMap);
        mMap.setOnCameraIdleListener(clusterManager);
        clusterManager.setOnClusterClickListener(this);
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
     *
     * @param profileList list of profile to display on map.
     */
    private void displayProfilesOnMap(List<Profile> profileList) {
        clusterManager.clearItems();
        for (Profile profile : profileList) {
            LatLng playerLocation = new LatLng(profile.getLocation().getLatitude(), profile.getLocation().getLongitude());
            PlayerMarker playerMarker = new PlayerMarker(playerLocation);
            clusterManager.addItem(playerMarker);
        }
        clusterManager.cluster();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     *
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // Request Permission
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
        } else {
            initializeObserver();
        }
        mMap.setOnMarkerClickListener(this);
    }

    /**
     * This function is called when a marker is clicked.
     * It queries the database for players to ultimately show them to the user.
     * @param marker The marker that was clicked on.
     * @return true if the listener has consumed the event (i.e., the default behavior should not occur); false otherwise
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        this.markerLocation.setLatitude(marker.getPosition().latitude);
        this.markerLocation.setLongitude(marker.getPosition().longitude);
        mProfiles = tronViewModel.getProfilesSurroundingLocation(this.markerLocation);
        mProfiles.observe(this, this.profileCardObserver);
        return true;
    }

    /**
     * This function is called when a cluster is clicked.
     * It queries the database for players to ultimately show them to the user.
     * @param cluster The cluster that was clicked on.
     * @return true if the listener has consumed the event (i.e., the default behavior should not occur); false otherwise
     */
    @Override
    public boolean onClusterClick(Cluster<PlayerMarker> cluster) {
        LatLng clusterPos = cluster.getPosition();
        double radius = 0;
        for (PlayerMarker marker : cluster.getItems()) {
            LatLng markerPos = marker.getPosition();
            float distance = distFrom((float) markerPos.latitude, (float) markerPos.longitude, (float) clusterPos.latitude, (float) clusterPos.longitude);
            if (distance > radius) {
                radius = distance;
            }
        }
        this.markerLocation.setLatitude(cluster.getPosition().latitude);
        this.markerLocation.setLongitude(cluster.getPosition().longitude);
        mProfiles = tronViewModel.getProfilesSurroundingLocation(this.markerLocation, (int)radius);
        mProfiles.observe(this, this.profileCardObserver);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_LOCATION:
                if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    initializeObserver();
                }
        }
    }


    /**
     * Opens a card containing the list of wifi on the marker/cluster selected
     */
    private void showProfilesOnCard(List<Profile> profileList) {

        if(profileList.size() > 1){

            // setup the alert builder
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.player_profile_title));
            String[] profileNames = new String[profileList.size()];
            for(int i = 0; i < profileList.size(); i++)
                profileNames[i] = profileList.get(i).getName();

            // add a list
            builder.setItems(profileNames, (DialogInterface dialog, int index) -> {
                dialog.cancel();
                CardFragment cardFragment = CardFragment.newInstance(profileList.get(index));
                cardFragment.show(getFragmentManager(), "Card fragment");
            });

            // create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        } else if (!profileList.isEmpty()) {
            CardFragment cardFragment = CardFragment.newInstance(profileList.get(0));
            cardFragment.show(getFragmentManager(), "Card fragment");
        } else {
            Toast.makeText(this, " No profiles found", Toast.LENGTH_SHORT).show();
        }
        mProfiles.removeObserver(this.profileCardObserver);
    }

}
