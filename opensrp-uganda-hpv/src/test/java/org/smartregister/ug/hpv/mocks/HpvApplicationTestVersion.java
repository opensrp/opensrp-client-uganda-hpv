package org.smartregister.ug.hpv.mocks;

import android.util.Log;

import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.application.HpvApplication;

/**
 * Created by ndegwamartin on 05/04/2018.
 */

public class HpvApplicationTestVersion extends HpvApplication {
    private static final String TAG = HpvApplicationTestVersion.class.getCanonicalName();

    @Override
    public void onCreate() {
        try {
            super.onCreate();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }finally {

            setTheme(R.style.AppTheme);
        }

    }
}
