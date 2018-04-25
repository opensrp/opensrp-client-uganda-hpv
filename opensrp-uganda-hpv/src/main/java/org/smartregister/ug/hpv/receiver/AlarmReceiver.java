package org.smartregister.ug.hpv.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.smartregister.configurableviews.service.PullConfigurableViewsIntentService;
import org.smartregister.service.ImageUploadSyncService;
import org.smartregister.ug.hpv.application.HpvApplication;
import org.smartregister.ug.hpv.service.PullUniqueIdsIntentService;
import org.smartregister.ug.hpv.service.SyncService;
import org.smartregister.ug.hpv.util.Constants;
import org.smartregister.ug.hpv.util.ServiceTools;
import org.smartregister.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = AlarmReceiver.class.getCanonicalName();

    private static final String serviceActionName = "org.smartregister.path.action.START_SERVICE_ACTION";
    private static final String serviceTypeName = "serviceType";
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onReceive(Context context, Intent alarmIntent) {
        int serviceType = alarmIntent.getIntExtra(serviceTypeName, 0);
        if (!HpvApplication.getInstance().getContext().IsUserLoggedOut()) {
            Intent serviceIntent = null;
            switch (serviceType) {
                case Constants.ServiceType.AUTO_SYNC:
                    android.util.Log.i(TAG, "Started AUTO_SYNC service at: " + dateFormatter.format(new Date()));
                    ServiceTools.startService(context, SyncService.class);
                    break;
                case Constants.ServiceType.PULL_UNIQUE_IDS:
                    serviceIntent = new Intent(context, PullUniqueIdsIntentService.class);
                    android.util.Log.i(TAG, "Started PULL_UNIQUE_IDS service at: " + dateFormatter.format(new Date()));
                    break;
                /*case Constants.ServiceType.VACCINE_SYNC_PROCESSING:
                    serviceIntent = new Intent(context, VaccineIntentService.class);
                    android.util.Log.i(TAG, "Started VACCINE_SYNC_PROCESSING service at: " + dateFormatter.format(new Date()));
                    break;*/
                case Constants.ServiceType.IMAGE_UPLOAD:
                    serviceIntent = new Intent(context, ImageUploadSyncService.class);
                    android.util.Log.i(TAG, "Started IMAGE_UPLOAD_SYNC service at: " + dateFormatter.format(new Date()));
                    break;
                case Constants.ServiceType.PULL_VIEW_CONFIGURATIONS:
                    serviceIntent = new Intent(context, PullConfigurableViewsIntentService.class);
                    android.util.Log.i(TAG, "Started VIEW_CONFIGS_SYNC service at: " + dateFormatter.format(new Date()));
                    break;
                default:
                    break;
            }

            if (serviceIntent != null)
                this.startService(context, serviceIntent);
        }

    }

    private void startService(Context context, Intent serviceIntent) {
        context.startService(serviceIntent);
    }

    /**
     * @param context
     * @param triggerIteration in minutes
     * @param taskType         a constant from pathconstants denoting the service type
     */
    public static void setAlarm(Context context, long triggerIteration, int taskType) {
        try {
            AlarmManager alarmManager;
            PendingIntent alarmIntent;

            long triggerAt;
            long triggerInterval;
            if (context == null) {
                throw new Exception("Unable to schedule service without app context");
            }

            // Otherwise schedule based on normal interval
            triggerInterval = TimeUnit.MINUTES.toMillis(triggerIteration);
            // set trigger time to be current device time + the interval (frequency). Probably randomize this a bit so that services not launch at exactly the same time
            triggerAt = System.currentTimeMillis() + triggerInterval;

            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent alarmReceiverIntent = new Intent(context, AlarmReceiver.class);

            alarmReceiverIntent.setAction(serviceActionName + taskType);
            alarmReceiverIntent.putExtra(serviceTypeName, taskType);
            alarmIntent = PendingIntent.getBroadcast(context, 0, alarmReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            try {
                alarmManager.cancel(alarmIntent);
            } catch (Exception e) {
                Log.logError(TAG, e.getMessage());
            }
            //Elapsed real time uses the "time since system boot" as a reference, and real time clock uses UTC (wall clock) time
            alarmManager.setRepeating(AlarmManager.RTC, triggerAt, triggerInterval, alarmIntent);
        } catch (Exception e) {
            Log.logError(TAG, "Error in setting service Alarm " + e.getMessage());
        }

    }

}
