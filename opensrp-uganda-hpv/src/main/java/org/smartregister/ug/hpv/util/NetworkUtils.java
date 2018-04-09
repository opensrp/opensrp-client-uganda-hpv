package org.smartregister.ug.hpv.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.smartregister.ug.hpv.application.HpvApplication;

/**
 * Created by ndegwamartin on 15/03/2018.
 */

public class NetworkUtils {
    private static final String TAG = NetworkUtils.class.getName();

    public static boolean isNetworkAvailable() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) HpvApplication
                    .getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();

        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return false;
    }
}
