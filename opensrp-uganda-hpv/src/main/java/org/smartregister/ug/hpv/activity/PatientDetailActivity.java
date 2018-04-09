package org.smartregister.ug.hpv.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.ug.hpv.application.HpvApplication;
import org.smartregister.ug.hpv.fragment.PatientDetailsFragment;
import org.smartregister.ug.hpv.util.Constants;
import org.smartregister.ug.hpv.util.JsonFormUtils;

import java.util.HashMap;

/**
 * Created by ndegwamartin on 09/10/2017.
 */

public class PatientDetailActivity extends BasePatientDetailActivity {
    private static final String TAG = PatientDetailActivity.class.getCanonicalName();
    private static final int REQUEST_CODE_GET_JSON = 3432;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected Fragment getDetailFragment() {
        PatientDetailsFragment mBaseFragment = new PatientDetailsFragment();
        patientDetails = (HashMap<String, String>) getIntent().getSerializableExtra(Constants.INTENT_KEY.PATIENT_DETAIL_MAP);
        mBaseFragment.setPatientDetails(patientDetails);

        mBaseFragment.setClient((CommonPersonObjectClient) getIntent().getSerializableExtra(Constants.INTENT_KEY.CLIENT_OBJECT));
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
        if (requestCode == REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra("json");
                Log.d("JSONResult", jsonString);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);

                JSONObject form = new JSONObject(jsonString);
                if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.Remove)) {

                    JsonFormUtils.saveForm(this, HpvApplication.getInstance().getContext(), jsonString, allSharedPreferences.fetchRegisteredANM());
                }
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }

        }
    }

}
