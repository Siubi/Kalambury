package it.polimi.deib.p2pchat.discovery.BitmapConverters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;

/**
 * Created by Cybul on 29.11.2017.
 */

public class StringToBitmapConverter {

    public static Bitmap Convert(String strBitmap)
    {
        try {
            byte[] encodeByte = Base64.decode(strBitmap, Base64.DEFAULT);

            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,
                    encodeByte.length);
            Bitmap large = getResizedBitmap(bitmap, 1080, 1080);
            return large;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    private static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
}
