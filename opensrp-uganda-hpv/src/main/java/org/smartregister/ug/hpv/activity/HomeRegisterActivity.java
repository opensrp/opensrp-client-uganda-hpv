package org.smartregister.ug.hpv.activity;

import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONObject;
import org.smartregister.ug.hpv.fragment.HomeRegisterFragment;

import java.util.Arrays;
import java.util.List;

import static org.smartregister.ug.hpv.util.Constants.VIEW_CONFIGS.HOME_REGISTER;

/**
 * Created by ndegwamartin on 14/03/2018.
 */

public class HomeRegisterActivity extends BaseRegisterActivity {

    @Override
    protected Fragment getRegisterFragment() {
        return new HomeRegisterFragment();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void savePartialFormData(String formData, String id, String formName, JSONObject fieldOverrides) {
        Toast.makeText(this, formName + " partially submitted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public List<String> getViewIdentifiers() {
        return Arrays.asList(HOME_REGISTER);
    }
}
