package org.smartregister.ug.hpv.sync;

import android.content.Context;
import android.content.Intent;

import org.smartregister.configurableviews.service.PullConfigurableViewsIntentService;
import org.smartregister.ug.hpv.service.SyncService;
import org.smartregister.ug.hpv.util.ServiceTools;

import static org.smartregister.util.Log.logInfo;

/**
 * Created by ndegwamartin on 15/03/2018.
 */

public class UserConfigurableViewsSyncHelper {

    private final Context context;

    public UserConfigurableViewsSyncHelper(Context context) {
        this.context = context;
    }

    public void syncFromServer() {
        logInfo("starting syncing From Server");
        startPullConfigurableViewsIntentService();
    }

    private void startPullConfigurableViewsIntentService() {
        Intent intent = new Intent(context, PullConfigurableViewsIntentService.class);
        context.startService(intent);

        ServiceTools.startService(context, SyncService.class);
    }
}