package zelemon.zsx;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProfileActivity extends Activity {

    String NO_PHOTO = "none";
    ImageView mProfilePhoto;
    SharedPreferences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mProfilePhoto = findViewById(R.id.profile_photo);
        Button choosePhotoButton = findViewById(R.id.button_choose_photo);
        choosePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 0);
            }
        });

        mSharedPref = getApplication().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String photoPath = mSharedPref.getString(getString(R.string.profile_photo_path), NO_PHOTO);
        if(!photoPath.equals(NO_PHOTO)) {
            loadImageFromStorage(photoPath);
        }
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
                String path = saveToInternalStorage(selectedImage);
                mSharedPref.edit().putString(getString(R.string.profile_photo_path), path).apply();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    private void loadImageFromStorage(String path)
    {
        try {
            File f=new File(path, "profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            mProfilePhoto.setImageBitmap(b);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
