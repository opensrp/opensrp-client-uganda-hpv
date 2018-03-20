package org.smartregister.ug.hpv.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import org.smartregister.domain.jsonmapping.LoginResponseData;

import org.joda.time.DateTime;
import org.smartregister.Context;
import org.smartregister.domain.LoginResponse;
import org.smartregister.domain.TimeStatus;
import org.smartregister.event.Listener;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.sync.DrishtiSyncScheduler;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.application.HpvApplication;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import util.UgandaHpvConstants;

import static org.smartregister.domain.LoginResponse.NO_INTERNET_CONNECTIVITY;
import static org.smartregister.domain.LoginResponse.UNAUTHORIZED;
import static org.smartregister.domain.LoginResponse.UNKNOWN_RESPONSE;
import static org.smartregister.util.Log.logError;

public class LoginActivity extends AppCompatActivity {
    private EditText userNameEditText;
    private EditText passwordEditText;
    private ProgressDialog progressDialog;
    private RemoteLoginTask remoteLoginTask;
    private android.content.Context appContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.black)));

        appContext = this;
        initializeLoginFields();
        initializeProgressDialog();
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
        if (!getOpenSRPContext().IsUserLoggedOut()) {
            goToHome(false);
        }
    }

    private void initializeLoginFields() {
        userNameEditText = (EditText) findViewById(R.id.login_user_name_edit_text);
        passwordEditText = (EditText) findViewById(R.id.login_password_edit_text);
    }

    private void initializeProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getString(org.smartregister.R.string.loggin_in_dialog_title));
        progressDialog.setMessage(getString(org.smartregister.R.string.loggin_in_dialog_message));
    }

    public void login(final View view) {
        // getOpenSRPContext().allSharedPreferences().saveForceRemoteLogin(false); // TODO: remove this after testing
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
       if (!getOpenSRPContext().allSharedPreferences().fetchBaseURL("").isEmpty()) {
            tryRemoteLogin(userName, password, new Listener<LoginResponse>(){
                public void onEvent(LoginResponse loginResponse) {
                    view.setClickable(true);
                    if (loginResponse == LoginResponse.SUCCESS) {
                        if (getOpenSRPContext().userService().isUserInPioneerGroup(userName)) {
                            TimeStatus timeStatus = getOpenSRPContext().userService().validateDeviceTime(
                                    loginResponse.payload(), UgandaHpvConstants.MAX_SERVER_TIME_DIFFERENCE
                            );
                            if (!UgandaHpvConstants.TIME_CHECK || timeStatus.equals(TimeStatus.OK)) {
                                remoteLoginWith(userName, password, loginResponse.payload());
                                // TODO: uncomment this code
                                // Intent intent = new Intent(appContext, PullUniqueIdsIntentService.class);
                                // appContext.startService(intent);
                            } else {
                                if (timeStatus.equals(TimeStatus.TIMEZONE_MISMATCH)) {
                                    TimeZone serverTimeZone = getOpenSRPContext().userService()
                                            .getServerTimeZone(loginResponse.payload());
                                    showErrorDialog(getString(timeStatus.getMessage(),
                                            serverTimeZone.getDisplayName()));
                                } else {
                                    showErrorDialog(getString(timeStatus.getMessage()));
                                }
                            }
                        } else {
                            // Valid user from wrong group trying to log in
                            showErrorDialog(getString(R.string.unauthorized_group));
                        }
                    } else {
                        if (loginResponse == null) {
                            showErrorDialog("Sorry, your login failed. Please try again");
                        } else {
                            if (loginResponse == NO_INTERNET_CONNECTIVITY) {
                                showErrorDialog(getResources().getString(R.string.no_internet_connectivity));
                            } else if (loginResponse == UNKNOWN_RESPONSE) {
                                showErrorDialog(getResources().getString(R.string.unknown_response));
                            } else if (loginResponse == UNAUTHORIZED) {
                                showErrorDialog(getResources().getString(R.string.unauthorized));
                            } else {
                                showErrorDialog(loginResponse.message());
                            }
                        }
                    }
                }
            });
       } else {
           view.setClickable(true);
           showErrorDialog("OpenSRP Base URL is missing. Please add it in Setting and try again");
       }
    }

    private void tryRemoteLogin(final String userName, final String password, final Listener<LoginResponse> afterLogincheck) {
        if (remoteLoginTask != null && !remoteLoginTask.isCancelled()) {
            remoteLoginTask.cancel(true);
        }
        remoteLoginTask = new RemoteLoginTask(userName, password, afterLogincheck);
        remoteLoginTask.execute();
    }

    private void remoteLoginWith(String userName, String password, LoginResponseData userInfo) {
        getOpenSRPContext().userService().remoteLogin(userName, password, userInfo);
        goToHome(true);
        DrishtiSyncScheduler.startOnlyIfConnectedToNetwork(getApplicationContext()); // TODO: maybe change to path version
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


    /**
     *  ============================ AsyncTasks =====================================
     */
    private class RemoteLoginTask extends AsyncTask<Void, Void, LoginResponse> {
        private final String username;
        private final String password;
        private final Listener<LoginResponse> afterLoginCheck;

        private RemoteLoginTask(String username, String password, Listener<LoginResponse> afterLoginCheck) {
            this.username = username;
            this.password = password;
            this.afterLoginCheck = afterLoginCheck;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected LoginResponse doInBackground(Void... params) {
            return getOpenSRPContext().userService().isValidRemoteLogin(username, password);
        }

        @Override
        protected void onPostExecute(LoginResponse loginResponse) {
            super.onPostExecute(loginResponse);
            if (!isDestroyed()) {
                progressDialog.dismiss();
                afterLoginCheck.onEvent(loginResponse);
            }
        }
    }
}


