package org.smartregister.ug.hpv.fragment;


import android.view.View;

import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.helper.DBQueryHelper;
import org.smartregister.ug.hpv.util.Constants;
import org.smartregister.ug.hpv.view.LocationPickerView;


/**
 * Created by ndegwamartin on 14/03/2018.
 */

public class HomeRegisterFragment extends BaseRegisterFragment {

    @Override
    protected void populateClientListHeaderView(View view) {
        View headerLayout = getLayoutInflater(null).inflate(R.layout.register_home_list_header, null);
        populateClientListHeaderView(view, headerLayout, Constants.VIEW_CONFIGS.HOME_REGISTER_HEADER);
    }

    @Override
    protected String getMainCondition() {
        return DBQueryHelper.getHomePatientRegisterCondition();
    }

    @Override
    protected String[] getAdditionalColumns(String tableName) {
        return new String[]{};
    }

    public LocationPickerView getLocationPickerView() {
        return getFacilitySelection();
    }


}
