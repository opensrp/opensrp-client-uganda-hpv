package org.smartregister.ug.hpv.service.intent;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import org.smartregister.service.ActionService;
import org.smartregister.ug.hpv.application.HpvApplication;
import org.smartregister.ug.hpv.receiver.AlarmReceiver;
import org.smartregister.ug.hpv.util.NetworkUtils;
import org.smartregister.ug.hpv.util.ServiceTools;


public class ExtendedSyncIntentService extends IntentService {

    private Context context;
    private ActionService actionService;

    public ExtendedSyncIntentService() {
        super("ExtendedSyncIntentService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getBaseContext();
        actionService = HpvApplication.getInstance().getContext().actionService();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {

        boolean wakeup = workIntent.getBooleanExtra(SyncIntentService.WAKE_UP, false);

        if (NetworkUtils.isNetworkAvailable()) {
            actionService.fetchNewActions();

            startSyncValidation(wakeup);
        }

        AlarmReceiver.completeWakefulIntent(workIntent);
    }


    private void startSyncValidation(boolean wakeup) {
        ServiceTools.startService(context, ValidateIntentService.class, wakeup);
    }


}
