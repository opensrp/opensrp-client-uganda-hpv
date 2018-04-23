package org.smartregister.ug.hpv.helper;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;

import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.fragment.VaccinationDialogFragment;
import org.smartregister.immunization.view.VaccineGroup;
import org.smartregister.ug.hpv.application.HpvApplication;
import org.smartregister.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import util.UgandaHpvConstants;

/**
 * Created by vkaruri on 23/04/2018.
 */

public class VaccinationHelper {

    private CommonPersonObjectClient commonPersonObjectClient;
    private Activity activity;
    private final String DIALOG_TAG = VaccinationHelper.class.getName();

    public VaccinationHelper(Activity activity, CommonPersonObjectClient commonPersonObjectClient) {
        this.activity = activity;
        this.commonPersonObjectClient = commonPersonObjectClient;
    }

    public void addVaccinationDialogFragment(ArrayList<VaccineWrapper> vaccineWrappers, VaccineGroup vaccineGroup) {
        FragmentTransaction fragmentTransaction = activity.getFragmentManager().beginTransaction();
        Fragment prev = activity.getFragmentManager().findFragmentByTag(DIALOG_TAG);
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }

        fragmentTransaction.addToBackStack(null);
        vaccineGroup.setModalOpen(true);

        // set date of birth
        String dobString = Utils.getValue(commonPersonObjectClient.getColumnmaps(), UgandaHpvConstants.DOB, false);
        Date dob = org.smartregister.ug.hpv.util.Utils.dobStringToDate(dobString);
        if (dob == null) {
            dob = Calendar.getInstance().getTime();
        }

        List<Vaccine> vaccineList = HpvApplication.getInstance().vaccineRepository()
                .findByEntityId(commonPersonObjectClient.entityId());
        if (vaccineList == null) {
            vaccineList = new ArrayList<>();
        }

        VaccinationDialogFragment vaccinationDialogFragment =
                VaccinationDialogFragment.newInstance(dob, vaccineList, vaccineWrappers, true);
        vaccinationDialogFragment.show(fragmentTransaction, DIALOG_TAG);
    }

    public void addVaccineUndoDialogFragment(VaccineGroup vaccineGroup, VaccineWrapper vaccineWrapper) {
        vaccineGroup.setModalOpen(true);
    }

}
