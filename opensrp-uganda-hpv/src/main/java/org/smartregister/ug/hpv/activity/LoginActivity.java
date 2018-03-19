package org.smartregister.ug.hpv.activity.activity;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.smartregister.ug.hpv.activity.R;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.black)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Settings");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
}
