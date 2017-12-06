package it.polimi.deib.p2pchat.discovery.BitmapConverters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }
}
