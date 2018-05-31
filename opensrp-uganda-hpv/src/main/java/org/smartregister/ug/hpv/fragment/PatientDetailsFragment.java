package org.smartregister.ug.hpv.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.util.Constants;
import org.smartregister.ug.hpv.view.LocationPickerView;

/**
 * Created by ndegwamartin on 24/11/2017.
 */


public class PatientDetailsFragment extends BasePatientDetailsFragment implements View.OnClickListener {

    private LocationPickerView locationPickerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_patient_detail, container, false);
        Toolbar toolbar = rootView.findViewById(R.id.patient_details_toolbar);
        AppCompatActivity activity = ((AppCompatActivity) getActivity());
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle(activity.getIntent().getStringExtra(Constants.INTENT_KEY.REGISTER_TITLE));
        setupViews(rootView);

        locationPickerView = rootView.findViewById(R.id.facility_selection);
        return rootView;
    }

    public void setupViews(View rootView) {

        super.setupViews(rootView);
        processViewConfigurations(rootView);
    }

    @Override
    public void setClient(CommonPersonObjectClient commonPersonObjectClient) {
        this.commonPersonObjectClient = commonPersonObjectClient;
    }

    @Override
    protected String getViewConfigurationIdentifier() {
        return Constants.CONFIGURATION.PATIENT_DETAILS;
    }

    @Override
    protected void onResumption() {
        //Overrides
    }

    public LocationPickerView getLocationPickerView() {
        return locationPickerView;
    }
}