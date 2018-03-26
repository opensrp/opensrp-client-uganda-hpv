package org.smartregister.ug.hpv.application;

import android.content.Intent;

import com.crashlytics.android.Crashlytics;

import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.ug.hpv.BuildConfig;
import org.smartregister.view.activity.DrishtiApplication;
import org.smartregister.view.activity.LoginActivity;

/**
 * Created by vkaruri on 19/03/2018.
 */

public class HpvApplication extends DrishtiApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        context = Context.getInstance();
        context.updateApplicationContext(getApplicationContext());

        //Initialize Modules
        CoreLibrary.init(getContext());
    }

    @Override
    public void logoutCurrentUser() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getApplicationContext().startActivity(intent);
        context.userService().logoutSession();
    }

    public static synchronized HpvApplication getInstance() {
        return (HpvApplication) mInstance;
    }

    public Context getContext() {
        return context;
    }

    /**
     * This method sets the Crashlytics user to whichever username was used to log in last. It only
     * does so if the app is not built for debugging
     *
     * @param context The user's context
     */
    public static void setCrashlyticsUser(Context context) {
        if (!BuildConfig.DEBUG && context != null && context.userService() != null
                && context.userService().getAllSharedPreferences() != null) {
            Crashlytics.setUserName(context.userService().getAllSharedPreferences().fetchRegisteredANM());
        }
    }
}
