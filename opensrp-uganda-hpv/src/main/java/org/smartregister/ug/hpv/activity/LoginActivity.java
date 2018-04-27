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
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.smartregister.Context;
import org.smartregister.domain.LoginResponse;
import org.smartregister.domain.TimeStatus;
import org.smartregister.domain.jsonmapping.LoginResponseData;
import org.smartregister.event.Listener;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.sync.DrishtiSyncScheduler;
import org.smartregister.ug.hpv.BuildConfig;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.application.HpvApplication;
import org.smartregister.ug.hpv.receiver.AlarmReceiver;
import org.smartregister.ug.hpv.util.Constants;
import org.smartregister.ug.hpv.util.NetworkUtils;
import org.smartregister.util.Utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import util.UgandaHpvConstants;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static org.smartregister.domain.LoginResponse.NO_INTERNET_CONNECTIVITY;
import static org.smartregister.domain.LoginResponse.UNAUTHORIZED;
import static org.smartregister.domain.LoginResponse.UNKNOWN_RESPONSE;
import static org.smartregister.util.Log.logError;
import static util.UgandaHpvConstants.CONFIGURATION.LOGIN;
import static util.UgandaHpvConstants.VIEW_CONFIGURATION_PREFIX;

public class LoginActivity extends AppCompatActivity {
    private EditText userNameEditText;
    private EditText passwordEditText;
    private CheckBox showPasswordCheckBox;
    private ProgressDialog progressDialog;
    private RemoteLoginTask remoteLoginTask;
    private android.content.Context appContext;
    private static final String TAG = LoginActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.black)));

        appContext = this;
        positionViews();
        initializeLoginFields();
        initializeBuildDetails();
        setDoneActionHandlerOnPasswordField();
        setListenerOnShowPasswordCheckbox();
        initializeProgressDialog();
        setLanguage();
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

    private void setDoneActionHandlerOnPasswordField() {
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    login(findViewById(R.id.login_login_btn));
                }
                return false;
            }
        });
    }

    private void setListenerOnShowPasswordCheckbox() {
        showPasswordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
    }

    private void initializeLoginFields() {
        userNameEditText = (EditText) findViewById(R.id.login_user_name_edit_text);
        passwordEditText = (EditText) findViewById(R.id.login_password_edit_text);
        showPasswordCheckBox = (CheckBox) findViewById(R.id.login_show_password_checkbox);
    }

    private void initializeProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getString(org.smartregister.R.string.loggin_in_dialog_title));
        progressDialog.setMessage(getString(org.smartregister.R.string.loggin_in_dialog_message));
    }

    private void initializeBuildDetails() {
        TextView buildDetailsView = (TextView) findViewById(R.id.login_build_text_view);
        try {
            buildDetailsView.setText("Version " + getVersion() + ", Built on: " + getBuildDate());
        } catch (Exception e) {
            logError("Error fetching build details: " + e);
        }
    }

    public void login(final View view) {
        login(view, !getOpenSRPContext().allSharedPreferences().fetchForceRemoteLogin());
    }

    private void login(final View view, boolean localLogin) {
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
        if (getOpenSRPContext().userService().isUserInValidGroup(userName, password)
                && (!UgandaHpvConstants.TIME_CHECK || TimeStatus.OK.equals(getOpenSRPContext().userService().validateStoredServerTimeZone()))) {
            localLoginWith(userName, password);
        } else {
            login(findViewById(R.id.login_login_btn), false);
        }
    }

    private void localLoginWith(String userName, String password) {

        getOpenSRPContext().userService().localLogin(userName, password);
        goToHome(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                android.util.Log.i(getClass().getName(), "Starting DrishtiSyncScheduler " + DateTime.now().toString());
                DrishtiSyncScheduler.startOnlyIfConnectedToNetwork(getApplicationContext());
                if (NetworkUtils.isNetworkAvailable()) {
                    AlarmReceiver.setAlarm(getApplicationContext(), BuildConfig.AUTO_SYNC_DURATION, Constants.ServiceType.AUTO_SYNC);
                }
                android.util.Log.i(getClass().getName(), "Started DrishtiSyncScheduler " + DateTime.now().toString());
            }
        }).start();
    }

    private void remoteLogin(final View view, final String userName, final String password) {

        try {
            if (!getOpenSRPContext().allSharedPreferences().fetchBaseURL("").isEmpty()) {
                tryRemoteLogin(userName, password, new Listener<LoginResponse>() {

                    public void onEvent(LoginResponse loginResponse) {
                        view.setClickable(true);
                        if (loginResponse == LoginResponse.SUCCESS) {
                            if (getOpenSRPContext().userService().isUserInPioneerGroup(userName)) {
                                TimeStatus timeStatus = getOpenSRPContext().userService().validateDeviceTime(
                                        loginResponse.payload(), UgandaHpvConstants.MAX_SERVER_TIME_DIFFERENCE
                                );
                                if (!UgandaHpvConstants.TIME_CHECK || timeStatus.equals(TimeStatus.OK)) {
                                    remoteLoginWith(userName, password, loginResponse.payload());
                                    HpvApplication.getInstance().startPullUniqueIdsService();
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
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());

            showErrorDialog("Error occurred trying to login in. Please try again...");
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
        DrishtiSyncScheduler.startOnlyIfConnectedToNetwork(getApplicationContext());
        if (NetworkUtils.isNetworkAvailable()) {
            AlarmReceiver.setAlarm(getApplicationContext(), BuildConfig.AUTO_SYNC_DURATION, Constants.ServiceType.AUTO_SYNC);
        }
    }

    private void goToHome(boolean remote) {
        if (remote) {
            //  Utils.startAsyncTask(new SaveTeamLocationsTask(), null); // TODO: remove this
        }
        Intent intent = new Intent(this, HomeRegisterActivity.class);
        intent.putExtra(UgandaHpvConstants.IS_REMOTE_LOGIN, remote);
        startActivity(intent);

        finish();
    }

    private void processViewCustomizations() {
        try {
            String jsonString = Utils.getPreference(this, VIEW_CONFIGURATION_PREFIX + LOGIN, null);
            if (jsonString == null) return;
            // TODO: add remaining lines here
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
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

    private String getVersion() throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        return packageInfo.versionName;
    }

    private String getBuildDate() throws PackageManager.NameNotFoundException, IOException {
        ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(getPackageName(), 0);
        ZipFile zipFile = new ZipFile(applicationInfo.sourceDir);
        ZipEntry zipEntry = zipFile.getEntry("classes.dex");
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(zipEntry.getTime()));
    }

    public static void setLanguage() {
        AllSharedPreferences allSharedPreferences = new AllSharedPreferences(getDefaultSharedPreferences(getOpenSRPContext().applicationContext()));
        String preferredLocale = allSharedPreferences.fetchLanguagePreference();
        Resources resources = getOpenSRPContext().applicationContext().getResources();

        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = new Locale(preferredLocale);
        resources.updateConfiguration(configuration, displayMetrics);
    }

    public static String switchLanguagePreference() {
        AllSharedPreferences allSharedPreferences = new AllSharedPreferences(getDefaultSharedPreferences(getOpenSRPContext().applicationContext()));

        String preferredLocal = allSharedPreferences.fetchLanguagePreference();
        if (UgandaHpvConstants.URDU_LOCALE.equals(preferredLocal)) {
            allSharedPreferences.saveLanguagePreference(UgandaHpvConstants.URDU_LOCALE);
            Resources resources = getOpenSRPContext().applicationContext().getResources();
            // Change locale settings in app
            DisplayMetrics displayMetrics = resources.getDisplayMetrics();
            Configuration configuration = resources.getConfiguration();
            configuration.locale = new Locale(UgandaHpvConstants.URDU_LOCALE);
            resources.updateConfiguration(configuration, displayMetrics);
            return UgandaHpvConstants.URDU_LANGUAGE;
        } else {
            allSharedPreferences.saveLanguagePreference(UgandaHpvConstants.ENGLISH_LANGUAGE);
            Resources resources = getOpenSRPContext().applicationContext().getResources();
            // change locale settings in the app
            DisplayMetrics displayMetrics = resources.getDisplayMetrics();
            Configuration configuration = resources.getConfiguration();
            configuration.locale = new Locale(UgandaHpvConstants.ENGLISH_LOCALE);
            resources.updateConfiguration(configuration, displayMetrics);
            return UgandaHpvConstants.ENGLISH_LANGUAGE;
        }
    }

    public static Context getOpenSRPContext() {
        return HpvApplication.getInstance().getContext();
    }

    private void positionViews() {
        final ScrollView canvasSV = (ScrollView) findViewById(R.id.canvasSV);
        final RelativeLayout canvasRL = (RelativeLayout) findViewById(R.id.login_layout);
        final LinearLayout logoCanvasLL = (LinearLayout) findViewById(R.id.bottom_section);
        final LinearLayout credentialsCanvasLL = (LinearLayout) findViewById(R.id.middle_section);

        canvasSV.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                canvasSV.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int windowHeight = canvasSV.getHeight();
                int topMargin = (windowHeight / 2)
                        - (credentialsCanvasLL.getHeight() / 2)
                        - logoCanvasLL.getHeight();
                topMargin = topMargin / 2;

                RelativeLayout.LayoutParams logoCanvasLP = (RelativeLayout.LayoutParams) logoCanvasLL.getLayoutParams();
                logoCanvasLP.setMargins(0, topMargin, 0, 0);
                logoCanvasLL.setLayoutParams(logoCanvasLP);

                canvasRL.setMinimumHeight(windowHeight);
            }
        });
    }


    /**
     * ============================ AsyncTasks =====================================
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

    // TODO: uncomment this
//    private class SaveTeamLocationsTask extends AsyncTask<Void, Void, Void> {
//        @Override
//        protected Void doInBackground(Void... params) {
//            LocationHelper.getInstance().locationsIdsFromHeirarchy();
//            return null;
//        }
//    }
}

