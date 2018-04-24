package zelemon.zsx;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
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

    @Inject
    ViewModelProvider.Factory viewModelFactory;
    private TronViewModel tronViewModel;
    private ImageView mProfilePhoto;
    private String currentDisplayName;
    private Location currentLocation;
    private final Observer<Location> locationObserver = this::updateCurrentLocationOnMap;
    private Profile currentProfile;
    private final Observer<Profile> currentProfileObserver = this::updateCurrentProfile;
    private LiveData<Profile> currentProfileLiveData;

    private void updateCurrentProfile(Profile profile) {
        if (profile != null) {
            currentProfile = profile;
            loadImageFromDatabase();
        } else {
            this.currentProfile = new Profile(currentDisplayName, this.currentLocation, "");
            this.tronViewModel.saveProfile(this.currentProfile);
        }
    }

    private void updateCurrentLocationOnMap(Location location) {
        this.currentLocation = location;
    }


    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (account != null) {
            currentDisplayName = account.getEmail();
            if (currentDisplayName == null) {
                currentDisplayName = "Name";
            }
        }
        currentProfileLiveData = tronViewModel.getProfile(currentDisplayName);
        currentProfileLiveData.observe(this, currentProfileObserver);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tronViewModel = ViewModelProviders.of(this, viewModelFactory).get(TronViewModel.class);
        tronViewModel.getLocationLiveData().observe(this, locationObserver);

        mProfilePhoto = findViewById(R.id.profile_photo);
        Button choosePhotoButton = findViewById(R.id.button_choose_photo);
        choosePhotoButton.setOnClickListener(v -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, 0);
        });
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
        this.currentProfile.setPicture(PictureTypeConverter.toString(bitmapImage));
        this.tronViewModel.saveProfile(currentProfile);
    }

    private void loadImageFromDatabase() {
        Bitmap picture = PictureTypeConverter.toBitmap(this.currentProfile.getPicture());
        mProfilePhoto.setImageBitmap(picture);
    }

}
