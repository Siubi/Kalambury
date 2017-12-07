package it.polimi.deib.p2pchat.discovery.BitmapConverters;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by Cybul on 29.11.2017.
 */

public class BitmapToStringConverter  {

    public static String Convert(Bitmap bitmapStr) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmapStr.compress(Bitmap.CompressFormat.PNG, 10, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);

    }
}
