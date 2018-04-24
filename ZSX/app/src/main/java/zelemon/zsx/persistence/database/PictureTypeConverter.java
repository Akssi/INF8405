package zelemon.zsx.persistence.database;

import android.arch.persistence.room.TypeConverter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class PictureTypeConverter {

    @TypeConverter
    public static String toString(Bitmap bitmap) {
        if (bitmap == null) {
            return (null);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }

    @TypeConverter
    public static Bitmap toBitmap(String image) {
        if (image == null) {
            return (null);
        }

        byte[] decodedBytes = Base64.decode(
                image.substring(image.indexOf(',') + 1),
                Base64.DEFAULT
        );
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
