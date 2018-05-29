package org.smartregister.ug.hpv.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.fragment.VaccinationDialogFragment;
import org.smartregister.ug.hpv.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by vkaruri on 26/04/2018.
 */

public class VaccinationDialogFragmentHPV extends VaccinationDialogFragment {

    public static VaccinationDialogFragmentHPV newInstance(Date dateOfBirth,
                                                           List<Vaccine> issuedVaccines,
                                                           ArrayList<VaccineWrapper> tags) {

        VaccinationDialogFragmentHPV vaccinationDialogFragment = new VaccinationDialogFragmentHPV();

        Bundle args = new Bundle();
        args.putSerializable(WRAPPER_TAG, tags);
        vaccinationDialogFragment.setArguments(args);
        vaccinationDialogFragment.setDateOfBirth(dateOfBirth);
        vaccinationDialogFragment.setIssuedVaccines(issuedVaccines);
        vaccinationDialogFragment.setDisableConstraints(false);

        return vaccinationDialogFragment;
    }

    public static VaccinationDialogFragmentHPV newInstance(Date dateOfBirth,
                                                           List<Vaccine> issuedVaccines,
                                                           ArrayList<VaccineWrapper> tags, boolean disableConstraints) {

        VaccinationDialogFragmentHPV vaccinationDialogFragment = new VaccinationDialogFragmentHPV();

        Bundle args = new Bundle();
        args.putSerializable(WRAPPER_TAG, tags);
        vaccinationDialogFragment.setArguments(args);
        vaccinationDialogFragment.setDateOfBirth(dateOfBirth);
        vaccinationDialogFragment.setIssuedVaccines(issuedVaccines);
        vaccinationDialogFragment.setDisableConstraints(disableConstraints);

        return vaccinationDialogFragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        setDefaultImageResourceID(R.drawable.ic_african_girl);
        setDefaultErrorImageResourceID(R.drawable.ic_african_girl);

        ViewGroup dialogView = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);

        return dialogView;
    }
}
