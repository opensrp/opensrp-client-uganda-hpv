package org.smartregister.ug.hpv.application;

import org.smartregister.ug.hpv.R;

/**
 * Created by ndegwamartin on 17/05/2018.
 */

public class TestHpvApplication extends HpvApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        setTheme(R.style.Theme_AppCompat); //or just R.style.Theme_AppCompat
    }
}
