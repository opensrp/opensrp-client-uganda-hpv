package org.smartregister.ug.hpv.service;

import android.content.Intent;

import org.smartregister.service.ImageUploadSyncService;
import org.smartregister.ug.hpv.receiver.AlarmReceiver;

/**
 * Created by keyman on 4/16/2018.
 */

public class HpvImageUploadSyncService extends ImageUploadSyncService {

    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);

        AlarmReceiver.completeWakefulIntent(intent);
    }
}

