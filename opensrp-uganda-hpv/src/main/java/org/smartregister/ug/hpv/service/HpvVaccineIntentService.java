package org.smartregister.ug.hpv.service;

import android.content.Intent;
import android.util.Log;

import org.smartregister.immunization.service.intent.VaccineIntentService;
import org.smartregister.ug.hpv.receiver.AlarmReceiver;

/**
 * Created by vkaruri on 27/04/2018.
 */

public class HpvVaccineIntentService extends VaccineIntentService {
    private static final String TAG = HpvVaccineIntentService.class.getCanonicalName();

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

    @Override
    protected String getEventType() {
        return "HPV Vaccination";
    }
}