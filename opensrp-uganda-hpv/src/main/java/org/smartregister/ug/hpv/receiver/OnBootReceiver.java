package org.smartregister.ug.hpv.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.smartregister.ug.hpv.application.HpvApplication;

public class OnBootReceiver extends BroadcastReceiver {
    private final Intent serviceIntent = new Intent();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            serviceIntent.putExtra(Intent.ACTION_BOOT_COMPLETED, true);
            this.restartAlarms(context);
        }
    }

    private void restartAlarms(Context context) {
        HpvApplication.setAlarms(context);
    }

}
