package org.smartregister.ug.hpv;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowConnectivityManager;
import org.robolectric.shadows.ShadowNetworkInfo;
import org.smartregister.ug.hpv.activity.HomeRegisterActivity;
import org.smartregister.ug.hpv.activity.LoginActivity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by vkaruri on 24/04/2018.
 */

public class LoginActivityTest extends BaseUnitTest {

    private LoginActivity loginActivity = new LoginActivity();
    private ActivityController<LoginActivity> controller;
    private ConnectivityManager connectivityManager;
    private ShadowNetworkInfo shadowOfActiveNetworkInfo;
    private ShadowConnectivityManager shadowConnectivityManager;

    @Before
    public void setUp() {
        controller = Robolectric.buildActivity(LoginActivity.class).create().start();
        loginActivity = controller.get();
        connectivityManager = (ConnectivityManager) RuntimeEnvironment.application.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Test
    public void testUserNameEditTextIsInitialized() {

        Field userNameEditTextField = getReflectedField(loginActivity, "userNameEditText");
        EditText userNameEditText = null;
        try {
            userNameEditText = (EditText) userNameEditTextField.get(loginActivity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        assertNotNull(userNameEditText);
    }

    @Test
    public void testPasswordEditTextIsInitialized() {

        Field userPasswordEditTextField = getReflectedField(loginActivity, "passwordEditText");
        EditText userPasswordEditText = null;
        try {
            userPasswordEditText = (EditText) userPasswordEditTextField.get(loginActivity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        assertNotNull(userPasswordEditText);
    }


    @Test
    public void testShowPasswordCheckBoxIsInitialized() {
        Field showPasswordCheckBoxField = getReflectedField(loginActivity, "showPasswordCheckBox");
        CheckBox showPasswordCheckBox = null;

        try {
            showPasswordCheckBox = (CheckBox) showPasswordCheckBoxField.get(loginActivity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        assertNotNull(showPasswordCheckBox);
    }

    @Test
    public void testProgressDialogIsInitialized() {
        Field progressDialogField = getReflectedField(loginActivity, "progressDialog");
        ProgressDialog progressDialog = null;

        try {
            progressDialog = (ProgressDialog) progressDialogField.get(loginActivity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        assertNotNull(progressDialog);
    }

    @Test
    public void testInitializeBuildDetails() {
        TextView buildDetails = (TextView) loginActivity.findViewById(R.id.login_build_text_view);
        assertNotEquals(buildDetails.getText(), null);
    }


    private void testRemoteLogin_validCredentials_shouldLogin() {

        loginActivity = Robolectric.setupActivity(LoginActivity.class);
        Button loginButton = (Button) loginActivity.findViewById(R.id.login_login_btn);

        Field userNameEditTextField = getReflectedField(loginActivity, "userNameEditText");
        EditText userNameEditText = null;
        try {
            userNameEditText = (EditText) userNameEditTextField.get(loginActivity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        Field userPasswordEditTextField = getReflectedField(loginActivity, "passwordEditText");
        EditText userPasswordEditText = null;
        try {
            userPasswordEditText = (EditText) userPasswordEditTextField.get(loginActivity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        userNameEditText.setText("demo");
        userPasswordEditText.setText("Amani123");

        loginButton.performClick();

        assertActivityStarted(loginActivity);
    }

    @Test
    public void testUnauthorizedUserDialogIsDisplayed() {
        loginActivity = Robolectric.setupActivity(LoginActivity.class);
        Button loginButton = (Button) loginActivity.findViewById(R.id.login_login_btn);

        Field userNameEditTextField = getReflectedField(loginActivity, "userNameEditText");
        EditText userNameEditText = null;
        try {
            userNameEditText = (EditText) userNameEditTextField.get(loginActivity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        Field userPasswordEditTextField = getReflectedField(loginActivity, "passwordEditText");
        EditText userPasswordEditText = null;
        try {
            userPasswordEditText = (EditText) userPasswordEditTextField.get(loginActivity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        userNameEditText.setText("");
        userPasswordEditText.setText("");

        try {
            Whitebox.invokeMethod(loginActivity, LoginActivity.class, "login", loginButton, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertNotNull(ShadowAlertDialog.getLatestAlertDialog());
    }

    @Test
    public void testGoToHome() {
        try {
            Whitebox.invokeMethod(loginActivity, LoginActivity.class, "goToHome", false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertActivityStarted(loginActivity);
    }

    private void assertActivityStarted(Activity activity) {

        Intent expectedIntent = new Intent(activity, HomeRegisterActivity.class);
        Intent actual = ShadowApplication.getInstance().getNextStartedActivity();
        assertEquals(expectedIntent.getComponent(), actual.getComponent());
    }

    public Field getReflectedField(Activity activity, String fieldName) {
        Field field = null;
        try{
            field = activity.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        field.setAccessible(true);
        return field;
    }
}
