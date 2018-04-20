package org.smartregister.ug.hpv.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.View;
import android.widget.Button;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Alert;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.fragment.VaccinationDialogFragment;
import org.smartregister.immunization.listener.VaccinationActionListener;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.util.VaccinatorUtils;
import org.smartregister.immunization.view.VaccineGroup;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.application.HpvApplication;
import org.smartregister.ug.hpv.helper.LocationHelper;
import org.smartregister.ug.hpv.view.LocationPickerView;
import org.smartregister.util.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.UgandaHpvConstants;

import static org.smartregister.ug.hpv.activity.LoginActivity.getOpenSRPContext;

public class VaccineBaseActivity extends AppCompatActivity implements VaccinationActionListener {

    private ProgressDialog progressDialog;
    private LocationPickerView locationPickerView;
    private ArrayList<VaccineGroup> vaccineGroups;
    private boolean isChildActive = false;
    private CommonPersonObjectClient childDetails;

    private static final String EXTRA_CHILD_DETAILS = "child_details";
    private final String DIALOG_TAG = this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vaccine_base);

        vaccineGroups = new ArrayList<>();

        // Get child details from bundled data
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            Serializable serializable = extras.getSerializable(EXTRA_CHILD_DETAILS);
            if (serializable != null && serializable instanceof CommonPersonObjectClient) {
                childDetails = (CommonPersonObjectClient) serializable;
            }
        }

        initializeProgressDialog();
        locationPickerView = (LocationPickerView) findViewById(R.id.facility_selection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (vaccineGroups != null) {
            // TODO: might need to add some logic here (similar to Zeir)
            vaccineGroups = null;
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Serializable serializable = savedInstanceState.getSerializable(EXTRA_CHILD_DETAILS);
        if (serializable != null && serializable instanceof CommonPersonObjectClient) {
            childDetails = (CommonPersonObjectClient) serializable;
        }
    }

    @Override
    public void onVaccinateToday(ArrayList<VaccineWrapper> tags, View v) {
        if (tags != null && !tags.isEmpty()) {
            View view = getLastOpenedView();
            saveVaccine(tags, view);
        }
    }

    @Override
    public void onVaccinateEarlier(ArrayList<VaccineWrapper> tags, View v) {
        if (tags != null && !tags.isEmpty()) {
            View view = getLastOpenedView();
            saveVaccine(tags, view);
        }
    }

    @Override
    public void onUndoVaccination(VaccineWrapper tag, View v) {
        Utils.startAsyncTask(new UndoVaccineTask(tag, v), null);
    }

    private void saveVaccine(ArrayList<VaccineWrapper> tags, final View view){
        if (tags.isEmpty()) {
            return;
        }

        VaccineRepository vaccineRepository = HpvApplication.getInstance().vaccineRepository();

        VaccineWrapper[] arrayTags = tags.toArray(new VaccineWrapper[tags.size()]);
        SaveVaccinesTask backgroundTask = new SaveVaccinesTask();
        backgroundTask.setVaccineRepository(vaccineRepository);
        backgroundTask.setView(view);
        Utils.startAsyncTask(backgroundTask, arrayTags);
    }

    private void initializeProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getString(R.string.saving_dialog_title));
        progressDialog.setMessage(getString(R.string.please_wait_message));
    }

    protected void showProgressDialog(String title, String message) {
        if (progressDialog != null) {
            if (StringUtils.isNotBlank(title)) {
                progressDialog.setTitle(title);
            }

            if (StringUtils.isNotBlank(message)) {
                progressDialog.setMessage(message);
            }
            progressDialog.show();
        }
    }

    protected void showProgressDialog() {
        showProgressDialog(getString(R.string.saving_dialog_title), getString(R.string.please_wait_message));
    }

    protected void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void saveVaccine(VaccineRepository vaccineRepository, VaccineWrapper tag) {
        if (tag.getUpdatedVaccineDate() == null) {
            return;
        }

        Vaccine vaccine = new Vaccine();
        if (tag.getDbKey() != null) {
            vaccine = vaccineRepository.find(tag.getDbKey());
        }
        vaccine.setBaseEntityId(childDetails.entityId());
        vaccine.setName(tag.getName());
        vaccine.setDate(tag.getUpdatedVaccineDate().toDate());
        vaccine.setAnmId(getOpenSRPContext().allSharedPreferences().fetchRegisteredANM());

        locationPickerView = (LocationPickerView) findViewById(R.id.facility_selection);
        vaccine.setLocationId(LocationHelper.getInstance().getOpenMrsLocationId(locationPickerView.getSelectedItem()));

        String lastChar = vaccine.getName().substring(vaccine.getName().length() - 1);
        if (StringUtils.isNumeric(lastChar)) {
            vaccine.setCalculation(Integer.valueOf(lastChar));
        } else {
            vaccine.setCalculation(-1);
        }
        org.smartregister.ug.hpv.util.Utils.addVaccine(vaccineRepository, vaccine);
        tag.setDbKey(vaccine.getId());
    }

    private VaccineGroup getLastOpenedView() {
        if (vaccineGroups == null) {
            return null;
        }

        for (VaccineGroup vaccineGroup : vaccineGroups) {
            if (vaccineGroup.isModalOpen()) {
                return vaccineGroup;
            }
        }
        return null;
    }

    private void updateVaccinationViews(List<Vaccine> vaccineList, List<Alert> alerts) {
        if (vaccineGroups == null) {
            vaccineGroups = new ArrayList<>();
            List<org.smartregister.immunization.domain.jsonmapping.VaccineGroup> supportedVaccines = VaccinatorUtils.getSupportedVaccines(this);
            for (org.smartregister.immunization.domain.jsonmapping.VaccineGroup vaccineGroup : supportedVaccines) {
                addVaccineGroup(-1, vaccineGroup, vaccineList, alerts);
            }
        } else {
            for(VaccineGroup vaccineGroup : vaccineGroups) {
                vaccineGroup.setChildActive(isChildActive);
                vaccineGroup.updateChildsActiveStatus();
            }
        }
    }

    private void addVaccineGroup(int canvasId, org.smartregister.immunization.domain.jsonmapping.VaccineGroup vaccineGroupData, List<Vaccine> vaccineList, List<Alert> alerts) {
        // TODO: Add logic to add vaccine group to view (similar to Zeir)
        VaccineGroup curGroup = new VaccineGroup(this);
        vaccineGroups.add(curGroup);
    }

    private void addVaccinationDialogFragment(ArrayList<VaccineWrapper> vaccineWrappers, VaccineGroup vaccineGroup) {
        FragmentTransaction fragmentTransaction = this.getFragmentManager().beginTransaction();
        Fragment prev = this.getFragmentManager().findFragmentByTag(DIALOG_TAG);
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }

        fragmentTransaction.addToBackStack(null);
        vaccineGroup.setModalOpen(true);

        // set date of birth
        String dobString = Utils.getValue(childDetails.getColumnmaps(), UgandaHpvConstants.DOB, false);
        Date dob = org.smartregister.ug.hpv.util.Utils.dobStringToDate(dobString);
        if (dob == null) {
            dob = Calendar.getInstance().getTime();
        }

        List<Vaccine> vaccineList = HpvApplication.getInstance().vaccineRepository()
                .findByEntityId(childDetails.entityId());
        if (vaccineList == null) {
            vaccineList = new ArrayList<>();
        }

        VaccinationDialogFragment vaccinationDialogFragment =
                VaccinationDialogFragment.newInstance(dob, vaccineList, vaccineWrappers, true);
        vaccinationDialogFragment.show(fragmentTransaction, DIALOG_TAG);
    }

    private void addVaccineUndoDialogFragment(VaccineGroup vaccineGroup, VaccineWrapper vaccineWrapper) {
        vaccineGroup.setModalOpen(true);
    }

    private void updateVaccineGroupViews(View view, final ArrayList<VaccineWrapper> wrappers, List<Vaccine> vaccineList) {
        updateVaccineGroupViews(view, wrappers, vaccineList, false);
    }

    private void updateVaccineGroupViews(View view, final ArrayList<VaccineWrapper> wrappers, final List<Vaccine> vaccineList, final boolean undo) {
        if (view == null || !(view instanceof VaccineGroup)) {
            return;
        }
        final VaccineGroup vaccineGroup = (VaccineGroup) view;
        vaccineGroup.setModalOpen(false);

        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (undo) {
                vaccineGroup.setVaccineList(vaccineList);
                vaccineGroup.updateWrapperStatus(wrappers, UgandaHpvConstants.KEY.CHILD);
            }
            vaccineGroup.updateViews(wrappers);

        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (undo) {
                        vaccineGroup.setVaccineList(vaccineList);
                        vaccineGroup.updateWrapperStatus(wrappers, UgandaHpvConstants.KEY.CHILD);
                    }
                    vaccineGroup.updateViews(wrappers);
                }
            });
        }
    }


    //////////////////////////////// AsyncTasks ////////////////////////////////////

    private class SaveVaccinesTask extends AsyncTask<VaccineWrapper, Void, Pair<ArrayList<VaccineWrapper>, List<Vaccine>>> {

        private View view;
        private VaccineRepository vaccineRepository;
        private List<String> affectedVaccines;
        private List<Vaccine> vaccineList;

        public void setView(View view) {
            this.view = view;
        }

        public void setVaccineRepository(VaccineRepository vaccineRepository) {
            this.vaccineRepository = vaccineRepository;
            affectedVaccines = new ArrayList<>();
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        @Override
        protected void onPostExecute(Pair<ArrayList<VaccineWrapper>, List<Vaccine>> pair) {
            hideProgressDialog();
            updateVaccineGroupViews(view, pair.first, pair.second);
            // TODO: maybe add the show weight logic as in zeir
        }

        @Override
        protected Pair<ArrayList<VaccineWrapper>, List<Vaccine>> doInBackground(VaccineWrapper... vaccineWrappers) {

            ArrayList<VaccineWrapper> list = new ArrayList<>();
            if (vaccineRepository != null) {
                for (VaccineWrapper tag : vaccineWrappers) {
                    saveVaccine(vaccineRepository, tag);
                    list.add(tag);
                }
            }

            Pair<ArrayList<VaccineWrapper>, List<Vaccine>> pair = new Pair<>(list, vaccineList);
            String dobString = Utils.getValue(childDetails.getColumnmaps(), UgandaHpvConstants.DOB, false);
            DateTime dateTime = org.smartregister.ug.hpv.util.Utils.dobStringToDateTime(dobString);
            if (dateTime != null) {
                affectedVaccines = VaccineSchedule.updateOfflineAlerts(childDetails.entityId(), dateTime, UgandaHpvConstants.KEY.CHILD);
            }

            vaccineList = vaccineRepository.findByEntityId(childDetails.entityId());
            return pair;
        }
    }

    private class UndoVaccineTask extends AsyncTask<Void, Void, Void> {
        private final VaccineWrapper tag;
        private final View view;
        private final VaccineRepository vaccineRepository;
        private List<Vaccine> vaccineList;
        private List<String> affectedVaccines;

        public UndoVaccineTask(VaccineWrapper tag, View view) {
            this.tag = tag;
            this.view = view;
            vaccineRepository = HpvApplication.getInstance().vaccineRepository();
        }

        @Override
        protected  void onPreExecute() {
            showProgressDialog(getString(R.string.updating_dialog_title), null);
        }

        @Override
        protected Void doInBackground(Void... params) {
            if  (tag != null) {
                if (tag.getDbKey() != null) {
                    Long dbKey = tag.getDbKey();
                    vaccineRepository.deleteVaccine(dbKey);

                    String dobString = Utils.getValue(childDetails.getColumnmaps(), UgandaHpvConstants.DOB, false);
                    DateTime dateTime = org.smartregister.ug.hpv.util.Utils.dobStringToDateTime(dobString);
                    if (dateTime != null) {
                        affectedVaccines = VaccineSchedule.updateOfflineAlerts(childDetails.entityId(), dateTime, UgandaHpvConstants.KEY.CHILD);
                    }
                    vaccineList = vaccineRepository.findByEntityId(childDetails.entityId());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            hideProgressDialog();
            super.onPostExecute(params);

            // Refresh the vaccine group with the updated vaccine
            tag.setUpdatedVaccineDate(null, false);
            tag.setDbKey(null);

            View view = getLastOpenedView();

            ArrayList<VaccineWrapper> wrappers = new ArrayList<>();
            wrappers.add(tag);
            updateVaccineGroupViews(view, wrappers, vaccineList, true);
        }
    }
}
