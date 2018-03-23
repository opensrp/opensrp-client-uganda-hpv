package org.smartregister.ug.hpv.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.application.HpvApplication;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button button = (Button) findViewById(R.id.register_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToHome();
            }
        });


        Button login = (Button) findViewById(R.id.omrs_ids_button);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               HpvApplication.getInstance().startPullUniqueIdsService();
            }
        });

    }

    private void goToHome() {
        Intent i = new Intent(this, HomeRegisterActivity.class);
        startActivity(i);
    }
}
