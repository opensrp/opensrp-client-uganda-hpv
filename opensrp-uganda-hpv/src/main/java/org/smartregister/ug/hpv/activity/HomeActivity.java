package org.smartregister.ug.hpv.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.smartregister.ug.hpv.R;

/**
 * Created by vkaruri on 20/03/2018.
 */

// TODO: maybe change the appcompatactivity to some baseactivity as in tbr
public class HomeActivity extends AppCompatActivity {
    private static final String TAG = HomeActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
