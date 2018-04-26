package zelemon.zsx;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;
import zelemon.zsx.persistence.database.PictureTypeConverter;
import zelemon.zsx.persistence.database.Profile;

public class ProfileActivity extends DaggerAppCompatActivity {

    private static final int PERMISSION_REQUEST_LOCATION = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    private TronViewModel tronViewModel;
    private ImageView mProfilePhoto;
    private String currentDisplayName;
    private Location currentLocation;
    private Profile currentProfile;
    private final Observer<Location> locationObserver = this::updateCurrentLocation;
    private LiveData<Profile> currentProfileLiveData;
    private TextView mProfileName;
    private final Observer<Profile> currentProfileObserver = this::updateCurrentProfile;

    private void updateCurrentProfile(Profile profile) {
        if (profile != null) {
            currentProfile = profile;
            loadImageFromDatabase();
        } else {
            if (this.currentLocation != null) {
                this.currentProfile = new Profile(currentDisplayName, this.currentLocation, "");
                this.tronViewModel.saveProfile(this.currentProfile);
            }
        }
    }

    private void updateCurrentLocation(Location location) {
        this.currentLocation = location;
        if (this.currentProfile != null) {
            this.currentProfile.setLocation(location);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tronViewModel = ViewModelProviders.of(this, viewModelFactory).get(TronViewModel.class);

        // Request Permission
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
        } else {
            inititalizeObservers();
        }

        mProfilePhoto = findViewById(R.id.profile_photo);
        mProfileName = findViewById(R.id.profile_name);
        ImageButton choosePhotoButton = findViewById(R.id.button_choose_photo);
        choosePhotoButton.setOnClickListener(v -> {
            // Take existing image
            Intent takeImage = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            // Take a picture with hardware
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            //Used to make a menu so the user can select which option he likes
            Intent chooser = Intent.createChooser(takeImage, "Select image or take a picture");
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{
                    takePictureIntent
            });
            startActivityForResult(chooser, REQUEST_IMAGE_CAPTURE);
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_LOCATION:
                if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    inititalizeObservers();
                }
        }
    }


    private void inititalizeObservers() {
        tronViewModel.getLocationLiveData().observe(this, locationObserver);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (account != null) {
            currentDisplayName = account.getDisplayName();
            if (currentDisplayName == null) {
                currentDisplayName = "Name";
            }
        }
        currentProfileLiveData = tronViewModel.getProfile(currentDisplayName);
        currentProfileLiveData.observe(this, this.currentProfileObserver);

    }


    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (reqCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (data.getExtras() != null) {
                try {
                    Bundle extras = data.getExtras();
                    final Bitmap selectedImage = (Bitmap) extras.get("data");
                    mProfilePhoto.setImageBitmap(selectedImage);
                    saveToDatabase(selectedImage);
                } catch (Exception e) {
                    Log.e("IMG", "Can't load image from source");
                }
            } else {
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    mProfilePhoto.setImageBitmap(selectedImage);
                    saveToDatabase(selectedImage);
                } catch (IOException e) {
                    Log.e("IMG", "error while reading uri media");

                }
            }
        }
    }

    private void saveToDatabase(Bitmap bitmapImage) {
        if (this.currentProfile == null) {
            this.currentProfile = new Profile(currentDisplayName, this.currentLocation, PictureTypeConverter.toString(bitmapImage));
            this.tronViewModel.saveProfile(currentProfile);
        } else {
            this.currentProfile.setPicture(PictureTypeConverter.toString(bitmapImage));
            this.tronViewModel.saveProfile(currentProfile);
        }
    }

    private void loadImageFromDatabase() {
        Bitmap picture = PictureTypeConverter.toBitmap(this.currentProfile.getPicture());
        mProfilePhoto.setImageBitmap(picture);
        mProfileName.setText(currentProfile.getName());
    }

}
