package org.smartregister.ug.hpv.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.ug.hpv.application.HpvApplication;
import org.smartregister.ug.hpv.event.PictureUpdatedEvent;
import org.smartregister.ug.hpv.fragment.PatientDetailsFragment;
import org.smartregister.ug.hpv.util.Constants;
import org.smartregister.ug.hpv.util.JsonFormUtils;
import org.smartregister.ug.hpv.util.Utils;

/**
 * Created by ndegwamartin on 09/10/2017.
 */

public class PatientDetailActivity extends BasePatientDetailActivity {
    private static final String TAG = PatientDetailActivity.class.getCanonicalName();
    private static final int REQUEST_CODE_GET_JSON = 3432;
    private CommonPersonObjectClient commonPersonObjectClient;
    private static final int REQUEST_TAKE_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected Fragment getDetailFragment() {
        PatientDetailsFragment mBaseFragment = new PatientDetailsFragment();
        commonPersonObjectClient = (CommonPersonObjectClient) getIntent().getSerializableExtra(Constants.INTENT_KEY.CLIENT_OBJECT);
        mBaseFragment.setClient(commonPersonObjectClient);
        return mBaseFragment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        AllSharedPreferences allSharedPreferences = getOpenSRPContext().allSharedPreferences();
        if (requestCode == REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            processFormDetailsSave(data, allSharedPreferences);

        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            processPhotoUpload(allSharedPreferences);

        }
    }

    protected void processFormDetailsSave(Intent data, AllSharedPreferences allSharedPreferences) {
        try {
            String jsonString = data.getStringExtra("json");
            Log.d("JSONResult", jsonString);

            JSONObject form = new JSONObject(jsonString);
            if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.REMOVE) || form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.UPDATE_REGISTRATION)) {

                JsonFormUtils.saveForm(this, HpvApplication.getInstance().getContext(), jsonString, allSharedPreferences.fetchRegisteredANM());
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    protected void processPhotoUpload(AllSharedPreferences allSharedPreferences) {
        try {
            String imageLocation = currentfile.getAbsolutePath();

            JsonFormUtils.saveImage(this, allSharedPreferences.fetchRegisteredANM(), commonPersonObjectClient.entityId(), imageLocation);

            Utils.postStickyEvent(new PictureUpdatedEvent());

        } catch (Exception e) {
            Utils.showToast(this, "Error occurred saving image...");
            Log.e(TAG, e.getMessage());
        }
    }
}
