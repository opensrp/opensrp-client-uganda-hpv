package org.smartregister.ug.hpv.helper.view;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Photo;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.view.VaccineGroup;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.activity.BasePatientDetailActivity;
import org.smartregister.ug.hpv.application.HpvApplication;
import org.smartregister.ug.hpv.domain.DoseStatus;
import org.smartregister.ug.hpv.helper.VaccinationHelper;
import org.smartregister.ug.hpv.repository.PatientRepository;
import org.smartregister.ug.hpv.util.Constants;
import org.smartregister.ug.hpv.util.DBConstants;
import org.smartregister.ug.hpv.util.ImageUtils;
import org.smartregister.ug.hpv.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.smartregister.util.Utils.getName;
import static org.smartregister.util.Utils.getValue;

/**
 * Created by ndegwamartin on 09/04/2018.
 */

public class RenderPatientFollowupCardHelper extends BaseRenderHelper implements View.OnClickListener {

    private VaccineRepository vaccineRepository = null;
    private static final String TAG = RenderPatientFollowupCardHelper.class.getCanonicalName();
    private VaccinationHelper vaccinationHelper;
    private RenderPatientFollowupCardHelper helperContext;
    private View view;

    public RenderPatientFollowupCardHelper(Context context, CommonPersonObjectClient client) {
        super(context, client);
        vaccinationHelper = new VaccinationHelper((BasePatientDetailActivity) context, client);
    }

    @Override
    public void renderView(final View view) {
        helperContext = this;
        this.view = view;
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                try {
                    final Map<String, String> patientDetails = PatientRepository.getPatientVaccinationDetails(commonPersonObjectClient.entityId());
                    renderHPVVaccineDueCore(patientDetails, view, helperContext);
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
                // launch vaccination dialog
                DoseStatus doseStatus = Utils.getCurrentDoseStatus(commonPersonObjectClient);
                Constants.State doseState = Utils.getRegisterViewButtonStatus(doseStatus);
                boolean launchDialog = ((BasePatientDetailActivity) context).getIntent().getBooleanExtra(Constants.INTENT_KEY.LAUNCH_VACCINE_DIALOG, false);
                if (launchDialog && (!doseState.equals(Constants.State.INACTIVE) || StringUtils.isBlank(doseStatus.getDateDoseOneGiven()))) {
                    showVaccinationDialog();
                    ((BasePatientDetailActivity) context).getIntent().removeExtra(Constants.INTENT_KEY.LAUNCH_VACCINE_DIALOG);
                }
            }

        });
    }

    private void renderHPVVaccineDueCore(Map<String, String> patientDetails, View view, RenderPatientFollowupCardHelper helperContext) {

        Button followUpView = view.findViewById(R.id.follow_up_button);
        followUpView.setAllCaps(false);
        String dateDoseOneGiven = patientDetails.get(DBConstants.KEY.DATE_DOSE_ONE_GIVEN);
        String nextVisitDate = StringUtils.isBlank(dateDoseOneGiven) ? patientDetails.get(DBConstants.KEY.DOSE_ONE_DATE) : patientDetails.get(DBConstants.KEY.DOSE_TWO_DATE);

        updateCommonPersonObjectClient(patientDetails);

        String dateDoseTwoGiven = patientDetails.get(DBConstants.KEY.DATE_DOSE_TWO_GIVEN);

        renderFollowupButton(helperContext, followUpView, StringUtils.isBlank(dateDoseOneGiven), StringUtils.isNotBlank(dateDoseTwoGiven), nextVisitDate);

        TextView doseOneGivenTextView = view.findViewById(R.id.dateDoseOneGivenTextView);
        TextView locationTextView = view.findViewById(R.id.locationVaccineOneGivenTextView);
        Button undoVaccineButton =  view.findViewById(R.id.undo_vaccine_btn);
        if (StringUtils.isNotBlank(dateDoseOneGiven) && StringUtils.isBlank(dateDoseTwoGiven)) {
            doseOneGivenTextView.setText(String.format(context.getString(R.string.dose_given_date), Constants.HPV_DOSE.NUMBER_1, Utils.formatDate(dateDoseOneGiven)));
            doseOneGivenTextView.setVisibility(View.VISIBLE);
            followUpView.setVisibility(View.VISIBLE);

            String locationDoseOne = patientDetails.get(DBConstants.KEY.DOSE_ONE_GIVEN_LOCATION);
            if (StringUtils.isNotBlank(locationDoseOne)) {
                locationTextView.setVisibility(View.VISIBLE);
                locationTextView.setText(String.format(context.getString(R.string.patient_location), StringUtils.capitalize(LocationHelper.getInstance().getOpenMrsLocationName(locationDoseOne))));
            }
            renderUndoVaccinationButton(true, undoVaccineButton);
        } else if (StringUtils.isNotBlank(dateDoseOneGiven)) {
            doseOneGivenTextView.setText(String.format(context.getString(R.string.dose_given_date), Constants.HPV_DOSE.NUMBER_1, Utils.formatDate(dateDoseOneGiven)));
            doseOneGivenTextView.setVisibility(View.VISIBLE);

            String locationDoseOne = patientDetails.get(DBConstants.KEY.DOSE_ONE_GIVEN_LOCATION);
            if (StringUtils.isNotBlank(locationDoseOne)) {
                locationTextView.setVisibility(View.VISIBLE);
                locationTextView.setText(String.format(context.getString(R.string.patient_location), StringUtils.capitalize(LocationHelper.getInstance().getOpenMrsLocationName(locationDoseOne))));
            }
            renderUndoVaccinationButton(true, undoVaccineButton);
        } else if (StringUtils.isBlank(dateDoseOneGiven)) {
            doseOneGivenTextView.setVisibility(View.GONE);
            locationTextView.setVisibility(View.GONE);
            followUpView.setEnabled(true);
            renderUndoVaccinationButton(false, undoVaccineButton);
        }

        TextView doseTwoGivenTextView = view.findViewById(R.id.dateDoseTwoGivenTextView);

        TextView locationTwoTextView = view.findViewById(R.id.locationVaccineTwoGivenTextView);
        if (StringUtils.isNotBlank(dateDoseTwoGiven)) {
            doseTwoGivenTextView.setText(String.format(context.getString(R.string.dose_given_date), Constants.HPV_DOSE.NUMBER_2, Utils.formatDate(dateDoseTwoGiven)));
            doseTwoGivenTextView.setVisibility(View.VISIBLE);

            String locationDoseTwo = patientDetails.get(DBConstants.KEY.DOSE_TWO_GIVEN_LOCATION);
            if (StringUtils.isNotBlank(locationDoseTwo)) {

                locationTwoTextView.setVisibility(View.VISIBLE);
                locationTwoTextView.setText(String.format(context.getString(R.string.patient_location), StringUtils.capitalize(LocationHelper.getInstance().getOpenMrsLocationName(locationDoseTwo))));
            }
            renderUndoVaccinationButton(true, undoVaccineButton);
        } else {
            locationTwoTextView.setVisibility(View.GONE);
            doseTwoGivenTextView.setVisibility(View.GONE);
        }
    }


    private void renderUndoVaccinationButton(boolean activate, Button undoButton) {

        if (!isValidForUndo()) {
            undoButton.setVisibility(View.GONE);
            return;
        }

        if (activate) {
            undoButton.setVisibility(View.VISIBLE);
            undoButton.setOnClickListener(this);
        } else {
            undoButton.setVisibility(View.GONE);
        }
    }

    private boolean isValidForUndo() {

        List<Vaccine> vaccines = getVaccineRepository().findByEntityId(commonPersonObjectClient.entityId());
        boolean hpv1IsUnsynced = false;
        for (Vaccine vaccine : vaccines) {

            if ("hpv 2".equals(vaccine.getName()) && "Unsynced".equals(vaccine.getSyncStatus())) {
                return true;
            } else if ("hpv 2".equals(vaccine.getName()) && "Synced".equals(vaccine.getSyncStatus())) {
                return false;
            } else if ("hpv 1".equals(vaccine.getName()) && "Unsynced".equals(vaccine.getSyncStatus())) {
                hpv1IsUnsynced = true;
            }
        }

        return hpv1IsUnsynced || false;
    }

    private void renderFollowupButton(RenderPatientFollowupCardHelper helperContext, Button followUpView, boolean isDoseOneGiven, boolean isDoseTwoGiven, String nextVisitDate) {
        DoseStatus doseStatus = Utils.getCurrentDoseStatus(commonPersonObjectClient);
        if (isDoseTwoGiven) {
            followUpView.setVisibility(View.GONE);

        } else {

            followUpView.setVisibility(View.VISIBLE);
            followUpView.setOnClickListener(helperContext);
            followUpView.setText(String.format(context.getString(R.string.vaccine_dose_due_on_date), isDoseOneGiven ? Constants.HPV_DOSE.NUMBER_1 : Constants.HPV_DOSE.NUMBER_2, Utils.formatDate(nextVisitDate)));

            Constants.State doseState = Utils.getRegisterViewButtonStatus(doseStatus);
            followUpView.setBackground(Utils.getDoseButtonBackground(context, doseState));
            followUpView.setTextColor(Utils.getDoseButtonTextColor(context, doseState));

            if (doseState.equals(Constants.State.INACTIVE) || doseState.equals(Constants.State.FULLY_IMMUNIZED)) {
                followUpView.setOnClickListener(null);
                followUpView.setEnabled(false);
            }
        }
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.undo_vaccine_btn) {
            showUndoVaccinationDialog();
        } else {
            showVaccinationDialog();
        }
    }

    private void showVaccinationDialog() {
        new ShowVaccinationDialogTask().execute();
    }

    public void showUndoVaccinationDialog() {
        new ShowUndoVaccinationDialogTask().execute();
    }

    public void refreshVaccinesDueView(final String baseEntityId) {

        final Handler mHandler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {

                final Map<String, String> patientDetails = PatientRepository.getPatientVaccinationDetails(baseEntityId);
                
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        renderHPVVaccineDueCore(patientDetails, view, helperContext);
                    }
                });
            }
        }).start();


    }

    public void setVaccineRepository(VaccineRepository vaccineRepository) {
        this.vaccineRepository = vaccineRepository;
    }

    public VaccineRepository getVaccineRepository() {

         if (vaccineRepository != null) {
             return  vaccineRepository;
         }
         return HpvApplication.getInstance().vaccineRepository();
    }

    private void updateCommonPersonObjectClient(Map<String, String> patientDetails) {

        if (patientDetails.containsKey(DBConstants.KEY.DOSE_ONE_DATE)) {
            commonPersonObjectClient.getColumnmaps().put(DBConstants.KEY.DOSE_ONE_DATE, patientDetails.get(DBConstants.KEY.DOSE_ONE_DATE));
        } else {
            commonPersonObjectClient.getColumnmaps().put(DBConstants.KEY.DOSE_ONE_DATE, null);
        }

        if (patientDetails.containsKey(DBConstants.KEY.DATE_DOSE_ONE_GIVEN)) {
            commonPersonObjectClient.getColumnmaps().put(DBConstants.KEY.DATE_DOSE_ONE_GIVEN, patientDetails.get(DBConstants.KEY.DATE_DOSE_ONE_GIVEN));
        } else {
            commonPersonObjectClient.getColumnmaps().put(DBConstants.KEY.DATE_DOSE_ONE_GIVEN, null);
        }

        if (patientDetails.containsKey(DBConstants.KEY.DOSE_ONE_GIVEN_LOCATION)) {
            commonPersonObjectClient.getColumnmaps().put(DBConstants.KEY.DOSE_ONE_GIVEN_LOCATION, patientDetails.get(DBConstants.KEY.DOSE_ONE_GIVEN_LOCATION));
        } else {
            commonPersonObjectClient.getColumnmaps().put(DBConstants.KEY.DOSE_ONE_GIVEN_LOCATION, null);
        }

        if (patientDetails.containsKey(DBConstants.KEY.DOSE_TWO_DATE)) {
            commonPersonObjectClient.getColumnmaps().put(DBConstants.KEY.DOSE_TWO_DATE, patientDetails.get(DBConstants.KEY.DOSE_TWO_DATE));
        } else {
            commonPersonObjectClient.getColumnmaps().put(DBConstants.KEY.DOSE_TWO_DATE, null);
        }

        if (patientDetails.containsKey(DBConstants.KEY.DATE_DOSE_TWO_GIVEN)) {
            commonPersonObjectClient.getColumnmaps().put(DBConstants.KEY.DATE_DOSE_TWO_GIVEN, patientDetails.get(DBConstants.KEY.DATE_DOSE_TWO_GIVEN));
        } else {
            commonPersonObjectClient.getColumnmaps().put(DBConstants.KEY.DATE_DOSE_TWO_GIVEN, null);
        }

        if (patientDetails.containsKey(DBConstants.KEY.DOSE_TWO_GIVEN_LOCATION)) {
            commonPersonObjectClient.getColumnmaps().put(DBConstants.KEY.DOSE_TWO_GIVEN_LOCATION, patientDetails.get(DBConstants.KEY.DOSE_TWO_GIVEN_LOCATION));
        } else {
            commonPersonObjectClient.getColumnmaps().put(DBConstants.KEY.DOSE_TWO_GIVEN_LOCATION, null);
        }
    }

    private class ShowVaccinationDialogTask extends AsyncTask<Void, Void, Void> {


        private String dateDoseOneGiven;

        private ArrayList<VaccineWrapper> vaccineWrappers = new ArrayList<>();

        protected Void doInBackground(Void... urls) {

            JSONObject object = HpvApplication.getInstance().getEventClientRepository().getClientByBaseEntityId(commonPersonObjectClient.getCaseId());

            try {
                dateDoseOneGiven = object.has(DBConstants.KEY.DATE_DOSE_ONE_GIVEN) ? object.get(DBConstants.KEY.DATE_DOSE_ONE_GIVEN).toString() : commonPersonObjectClient.getColumnmaps().get(DBConstants.KEY.DATE_DOSE_ONE_GIVEN);

            } catch (Exception e) {
                dateDoseOneGiven = commonPersonObjectClient.getColumnmaps().get(DBConstants.KEY.DATE_DOSE_ONE_GIVEN);

                Log.e(TAG, e.getMessage());
            }

            VaccineWrapper vaccineWrapper = new VaccineWrapper();

            vaccineWrapper.setId(commonPersonObjectClient.entityId());
            vaccineWrapper.setGender(commonPersonObjectClient.getColumnmaps().get(DBConstants.KEY.GENDER));

            DoseStatus doseStatus = Utils.getCurrentDoseStatus(commonPersonObjectClient);
            DateTime vaccineDateTime = null;

            if (dateDoseOneGiven == null) {
                vaccineWrapper.setName(Constants.HPV_DOSE_NAME.HPV_1);
                vaccineWrapper.setDefaultName(Constants.HPV_DOSE_NAME.HPV_1);
                vaccineDateTime = (new DateTime(doseStatus.getDoseOneDate())).toDateTime();
                vaccineWrapper.setVaccineDate(vaccineDateTime);
            } else {
                vaccineWrapper.setName(Constants.HPV_DOSE_NAME.HPV_2);
                vaccineWrapper.setDefaultName(Constants.HPV_DOSE_NAME.HPV_2);
                vaccineDateTime = (new DateTime(doseStatus.getDoseTwoDate())).toDateTime();
                vaccineWrapper.setVaccineDate(vaccineDateTime);
            }

            boolean isToday = (vaccineWrapper.getVaccineDate().getMillis() - DateTime.now().withTimeAtStartOfDay().getMillis()) == 0;
            vaccineWrapper.setUpdatedVaccineDate(vaccineDateTime, isToday);

            Photo photo = ImageUtils.profilePhotoByClientID(commonPersonObjectClient.getCaseId());
            vaccineWrapper.setPhoto(photo);

            String firstName = getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
            String lastName = getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);
            String childName = getName(firstName, lastName);
            String patientNumber = getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.OPENSRP_ID, true);

            vaccineWrapper.setPatientName(childName.trim());
            vaccineWrapper.setPatientNumber(patientNumber);
            vaccineWrappers.add(vaccineWrapper);

            return null;
        }

        protected void onPostExecute(Void result) {
            vaccinationHelper.addVaccinationDialogFragment(vaccineWrappers, new VaccineGroup(context));
        }
    }

    private class ShowUndoVaccinationDialogTask extends AsyncTask<Void, Void, Void> {

        private VaccineWrapper vaccineWrapper = new VaccineWrapper();
        protected Void doInBackground(Void... urls) {

            final Map<String, String> patientDetails = PatientRepository.getPatientVaccinationDetails(commonPersonObjectClient.entityId());
            String dateDoseTwoGiven = getValue(patientDetails, DBConstants.KEY.DATE_DOSE_TWO_GIVEN, true);
            if (!StringUtils.isBlank(dateDoseTwoGiven)) {
                vaccineWrapper.setName("HPV 2");
                vaccineWrapper.setDefaultName("HPV 2");
            } else {
                vaccineWrapper.setName("HPV 1");
                vaccineWrapper.setDefaultName("HPV 1");
            }

            // get vaccines given (shouldn't be more than two)
            List<Vaccine> vaccineList = getVaccineRepository().findByEntityId(commonPersonObjectClient.entityId());

            if (("hpv 1".equalsIgnoreCase(vaccineList.get(0).getName()) && StringUtils.isBlank(dateDoseTwoGiven)) ||
                    ("hpv 2".equalsIgnoreCase(vaccineList.get(0).getName()))) {
                vaccineWrapper.setDbKey(vaccineList.get(0).getId());
            } else {
                vaccineWrapper.setDbKey(vaccineList.get(1).getId());
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            vaccinationHelper.addUndoVaccinationDialogFragment(vaccineWrapper);
        }
    }
}
