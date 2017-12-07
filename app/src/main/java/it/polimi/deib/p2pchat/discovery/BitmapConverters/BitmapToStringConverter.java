package it.polimi.deib.p2pchat.discovery.BitmapConverters;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by Cybul on 29.11.2017.
 */

public class BitmapToStringConverter  {

    public static String Convert(Bitmap bitmapStr) {
        Bitmap small = Scaler.getResizedBitmap(bitmapStr, 100, 100);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        small.compress(Bitmap.CompressFormat.PNG, 10, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);

    }
}