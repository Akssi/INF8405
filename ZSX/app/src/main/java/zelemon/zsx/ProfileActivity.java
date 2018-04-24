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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Button;
import android.widget.ImageView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import dagger.android.support.DaggerAppCompatActivity;
import zelemon.zsx.persistence.database.PictureTypeConverter;
import zelemon.zsx.persistence.database.Profile;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ProfileActivity extends DaggerAppCompatActivity {

    private static final int PERMISSION_REQUEST_LOCATION = 0;
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    private TronViewModel tronViewModel;
    private ImageView mProfilePhoto;
    private String currentDisplayName;
    private Location currentLocation;
    private Profile currentProfile;
    private final Observer<Location> locationObserver = this::updateCurrentLocation;
    private final Observer<Profile> currentProfileObserver = this::updateCurrentProfile;
    private LiveData<Profile> currentProfileLiveData;

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
        Button choosePhotoButton = findViewById(R.id.button_choose_photo);
        choosePhotoButton.setOnClickListener(v -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, 0);
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
        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                mProfilePhoto.setImageBitmap(selectedImage);
                saveToDatabase(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
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
    }

}
