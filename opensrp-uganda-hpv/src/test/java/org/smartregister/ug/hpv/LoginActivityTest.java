package org.smartregister.ug.hpv;

import android.app.Activity;
import android.widget.EditText;

import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.smartregister.ug.hpv.activity.LoginActivity;
import static org.junit.Assert.*;

import java.lang.reflect.Field;

/**
 * Created by vkaruri on 24/04/2018.
 */

public class LoginActivityTest extends BaseUnitTest {
    private LoginActivity loginActivity = new LoginActivity();

    @Test
    public void testUserNameEditTextIsInitialized() {
        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class).create().start();
        loginActivity = controller.get();
        Field userNameEditTextField = getReflectedField(loginActivity, "userNameEditText");

        EditText userNameEditText = null;
        try {
            userNameEditText = (EditText) userNameEditTextField.get(loginActivity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        assertNotEquals(userNameEditText, null);
    }

    @Test
    public void testPasswordEditTextIsInitialize() {
        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class).create().start();
        loginActivity = controller.get();
        Field userPasswordEditTextField = getReflectedField(loginActivity, "passwordEditText");

        EditText userPasswordEditText = null;
        try {
            userPasswordEditText = (EditText) userPasswordEditTextField.get(loginActivity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        assertNotEquals(userPasswordEditText, null);
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
