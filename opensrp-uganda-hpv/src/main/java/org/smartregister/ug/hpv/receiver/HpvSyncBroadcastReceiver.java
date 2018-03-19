package org.smartregister.ug.hpv.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import org.smartregister.ug.hpv.service.SyncService;
import org.smartregister.ug.hpv.sync.UserConfigurableViewsSyncTask;
import org.smartregister.ug.hpv.util.ServiceTools;

import static org.smartregister.util.Log.logInfo;

/**
 * Created by ndegwamartin on 15/03/2018.
 */
public class HpvSyncBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent alarmIntent) {
        logInfo("Sync alarm triggered. Trying to Sync.");

        ServiceTools.startService(context, SyncService.class);

        UserConfigurableViewsSyncTask hpvUpdateActionsTask = new UserConfigurableViewsSyncTask(context);

        hpvUpdateActionsTask.syncFromServer();
    }


}
