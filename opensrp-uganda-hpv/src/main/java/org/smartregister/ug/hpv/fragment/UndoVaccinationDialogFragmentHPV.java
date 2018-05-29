package org.smartregister.ug.hpv.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.fragment.UndoVaccinationDialogFragment;
import org.smartregister.ug.hpv.R;

/**
 * Created by vkaruri on 23/05/2018.
 */

public class UndoVaccinationDialogFragmentHPV extends UndoVaccinationDialogFragment {

    public static final String WRAPPER_TAG = "tag";

    public static UndoVaccinationDialogFragment newInstance(VaccineWrapper tag) {

        UndoVaccinationDialogFragmentHPV undoVaccinationDialogFragment = new UndoVaccinationDialogFragmentHPV();

        Bundle args = new Bundle();
        args.putSerializable(WRAPPER_TAG, tag);
        undoVaccinationDialogFragment.setArguments(args);

        return undoVaccinationDialogFragment;
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
