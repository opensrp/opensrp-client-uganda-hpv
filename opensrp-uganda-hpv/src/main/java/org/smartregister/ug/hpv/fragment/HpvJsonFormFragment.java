package org.smartregister.ug.hpv.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.presenters.JsonFormFragmentPresenter;

import org.smartregister.Context;
import org.smartregister.ug.hpv.HpvJsonFormInteractor;
import org.smartregister.ug.hpv.application.HpvApplication;
import org.smartregister.ug.hpv.util.DBConstants;
import org.smartregister.ug.hpv.viewstates.HpvJsonFormFragmentViewState;


/**
 * Created by ndegwamartin on 19/03/2018.
 */
public class HpvJsonFormFragment extends JsonFormFragment {

    public static HpvJsonFormFragment getFormFragment(String stepName) {
        HpvJsonFormFragment jsonFormFragment = new HpvJsonFormFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DBConstants.KEY.STEPNAME, stepName);
        jsonFormFragment.setArguments(bundle);
        return jsonFormFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected HpvJsonFormFragmentViewState createViewState() {
        return new HpvJsonFormFragmentViewState();
    }

    @Override
    protected JsonFormFragmentPresenter createPresenter() {
        return new JsonFormFragmentPresenter(this, HpvJsonFormInteractor.getInstance());
    }

    public Context context() {
        return HpvApplication.getInstance().getContext();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);

    }


}


