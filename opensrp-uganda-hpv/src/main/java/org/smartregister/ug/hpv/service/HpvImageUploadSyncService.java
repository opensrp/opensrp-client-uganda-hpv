package org.smartregister.ug.hpv.service;

import android.content.Intent;
import android.util.Log;

import org.smartregister.service.ImageUploadSyncService;
import org.smartregister.ug.hpv.receiver.AlarmReceiver;

/**
 * Created by ndegwamartin on 23/05/2018.
 */

public class HpvImageUploadSyncService extends ImageUploadSyncService {
    private static final String TAG = HpvImageUploadSyncService.class.getCanonicalName();

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            super.onHandleIntent(intent);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            AlarmReceiver.completeWakefulIntent(intent);
        }
    }
}

