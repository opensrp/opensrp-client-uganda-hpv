package org.smartregister.ug.hpv.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.joda.time.DateTime;
import org.smartregister.Context;
import org.smartregister.sync.DrishtiSyncScheduler;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.application.HpvApplication;

import util.UgandaHpvConstants;

import static org.smartregister.util.Log.logError;

public class LoginActivity extends AppCompatActivity {
    private EditText userNameEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.black)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Settings");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().toString().equalsIgnoreCase("Settings")) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeLoginFields();
        // TODO: finish implementation
    }

    private void initializeLoginFields() {
        userNameEditText = (EditText) findViewById(R.id.login_user_name_edit_text);
        passwordEditText = (EditText) findViewById(R.id.login_password_edit_text);
    }

    public void login(final View view) {
        getOpenSRPContext().allSharedPreferences().saveForceRemoteLogin(false); // TODO: remove this after testing
        login(view, !getOpenSRPContext().allSharedPreferences().fetchForceRemoteLogin());
    }

    public void login(final View view, boolean localLogin) {
        android.util.Log.i(getClass().getName(), "Hiding Keyboard " + DateTime.now().toString());
        hideKeyboard();
        view.setClickable(false);

        final String userName = userNameEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(password)) {
            if (localLogin) {
                localLogin(view, userName, password);
            } else {
                remoteLogin(view, userName, password);
            }
        } else {
            showErrorDialog(getResources().getString(R.string.unauthorized));
            view.setClickable(true);
        }
        android.util.Log.i(getClass().getName(), "Login result finished " + DateTime.now().toString());
    }

    private void localLogin(View view, String userName, String password) {
        view.setClickable(true);
        localLoginWith(userName, password);
        // TODO: uncomment this
//        if (getOpenSRPContext().userService().isUserInValidGroup(userName, password)
//                && (!UgandaHpvConstants.TIME_CHECK || TimeStatus.OK.equals(getOpenSRPContext().userService().validateStoredServerTimeZone()))) {
//            localLoginWith(userName, password);
//        } else {
//            login(findViewById(R.id.login_login_btn), false);
//        }
    }

    private void localLoginWith(String userName, String password) {
        // TODO: uncomment this
        // getOpenSRPContext().userService().localLogin(userName, password);
        goToHome(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                android.util.Log.i(getClass().getName(), "Starting DrishtiSyncScheduler " + DateTime.now().toString());
                DrishtiSyncScheduler.startOnlyIfConnectedToNetwork(getApplicationContext());
                android.util.Log.i(getClass().getName(), "Started DrishtiSyncScheduler " + DateTime.now().toString());
            }
        }).start();
    }

    private void remoteLogin(final View view, final String userName, final String password) {
        // TODO: implement this
    }

    private void goToHome(boolean remote) {
        if (remote) {
            // TODO: maybe add this:  Utils.startAsyncTask(new SaveTeamLocationsTask(), null);
        }
        HpvApplication.setCrashlyticsUser(getOpenSRPContext());
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(UgandaHpvConstants.IS_REMOTE_LOGIN, remote);
        startActivity(intent);

        finish();
    }

    private void showErrorDialog(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(org.smartregister.R.string.login_failed_dialog_title))
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();
        alertDialog.show();
    }

    private void hideKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            logError("Error encountered while hiding keyboard " + e);
        }
    }

    public static Context getOpenSRPContext() {
        return HpvApplication.getInstance().getContext();
    }
}
