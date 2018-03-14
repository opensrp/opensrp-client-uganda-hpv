package org.smartregister.ug.hpv.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by ndegwamartin on 14/03/2018.
 */

public class Utils {
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

    }

}
