package org.smartregister.ug.hpv.helper.view;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Photo;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.view.VaccineGroup;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.activity.BasePatientDetailActivity;
import org.smartregister.ug.hpv.domain.DoseStatus;
import org.smartregister.ug.hpv.helper.LocationHelper;
import org.smartregister.ug.hpv.helper.VaccinationHelper;
import org.smartregister.ug.hpv.repository.PatientRepository;
import org.smartregister.ug.hpv.util.Constants;
import org.smartregister.ug.hpv.util.DBConstants;
import org.smartregister.ug.hpv.util.ImageUtils;
import org.smartregister.ug.hpv.util.Utils;

import java.util.ArrayList;
import java.util.Map;

import static org.smartregister.util.Utils.getName;
import static org.smartregister.util.Utils.getValue;

/**
 * Created by ndegwamartin on 09/04/2018.
 */

public class RenderPatientFollowupCardHelper extends BaseRenderHelper implements View.OnClickListener {

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
                    Map<String, String> patientDetails = commonPersonObjectClient.getDetails();
                    renderHPVVaccineDueCore(patientDetails, view, helperContext);


                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }


//launch vaccination dialog

                DoseStatus doseStatus = Utils.getCurrentDoseStatus(commonPersonObjectClient);
                Constants.State doseState = Utils.getRegisterViewButtonStatus(doseStatus);
                boolean launchDialog = ((BasePatientDetailActivity) context).getIntent().getBooleanExtra(Constants.INTENT_KEY.LAUNCH_VACCINE_DIALOG, false);
                if (launchDialog && (!doseState.equals(Constants.State.INACTIVE) || StringUtils.isBlank(doseStatus.getDateDoseOneGiven()))) {
                    showVaccinationDialog(context, commonPersonObjectClient, vaccinationHelper);
                }
            }

        });
    }

    private void renderHPVVaccineDueCore(Map<String, String> patientDetails, View view, RenderPatientFollowupCardHelper helperContext) {
        Button followUpView = (Button) view.findViewById(R.id.follow_up_button);
        followUpView.setAllCaps(false);
        String dateDoseOneGiven = patientDetails.get(DBConstants.KEY.DATE_DOSE_ONE_GIVEN);
        String nextVisitDate = StringUtils.isBlank(dateDoseOneGiven) ? patientDetails.get(DBConstants.KEY.DOSE_ONE_DATE) : patientDetails.get(DBConstants.KEY.DOSE_TWO_DATE);

        updateCommonPersonObjectClient(patientDetails);

        String dateDoseTwoGiven = patientDetails.get(DBConstants.KEY.DATE_DOSE_TWO_GIVEN);

        renderFollowupButton(followUpView, StringUtils.isBlank(dateDoseOneGiven), StringUtils.isNotBlank(dateDoseTwoGiven), nextVisitDate);

        if (StringUtils.isNotBlank(dateDoseOneGiven)) {
            TextView doseOneGivenTextView = (TextView) view.findViewById(R.id.dateDoseOneGivenTextView);
            doseOneGivenTextView.setText(String.format(context.getString(R.string.dose_given_date), Constants.HPV_DOSE.NUMBER_1, Utils.formatDate(dateDoseOneGiven)));
            doseOneGivenTextView.setVisibility(View.VISIBLE);

            String locationDoseOne = patientDetails.get(DBConstants.KEY.DOSE_ONE_GIVEN_LOCATION);
            if (StringUtils.isNotBlank(locationDoseOne)) {
                TextView locationTextView = (TextView) view.findViewById(R.id.locationVaccineOneGivenTextView);
                locationTextView.setVisibility(View.VISIBLE);
                locationTextView.setText(String.format(context.getString(R.string.patient_location), StringUtils.capitalize(LocationHelper.getInstance().getOpenMrsLocationName(locationDoseOne))));
            }
        }
        if (StringUtils.isNotBlank(dateDoseTwoGiven)) {
            TextView doseTwoGivenTextView = (TextView) view.findViewById(R.id.dateDoseTwoGivenTextView);
            doseTwoGivenTextView.setText(String.format(context.getString(R.string.dose_given_date), Constants.HPV_DOSE.NUMBER_2, Utils.formatDate(dateDoseTwoGiven)));
            doseTwoGivenTextView.setVisibility(View.VISIBLE);

            String locationDoseTwo = patientDetails.get(DBConstants.KEY.DOSE_TWO_GIVEN_LOCATION);
            if (StringUtils.isNotBlank(locationDoseTwo)) {
                TextView locationTextView = (TextView) view.findViewById(R.id.locationVaccineTwoGivenTextView);
                locationTextView.setVisibility(View.VISIBLE);
                locationTextView.setText(String.format(context.getString(R.string.patient_location), StringUtils.capitalize(LocationHelper.getInstance().getOpenMrsLocationName(locationDoseTwo))));
            }
        }
    }

    private void renderFollowupButton(Button followUpView, boolean isDoseOneGiven, boolean isDoseTwoGiven, String nextVisitDate) {
        DoseStatus doseStatus = Utils.getCurrentDoseStatus(commonPersonObjectClient);
        if (isDoseTwoGiven) {
            followUpView.setVisibility(View.GONE);

        } else {

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
        showVaccinationDialog(context, commonPersonObjectClient, vaccinationHelper);
    }

    private void showVaccinationDialog(Context context, CommonPersonObjectClient commonPersonObjectClient, VaccinationHelper vaccinationHelper) {
        String dateDoseOneGiven = commonPersonObjectClient.getDetails().get(DBConstants.KEY.DATE_DOSE_ONE_GIVEN);

        ArrayList<VaccineWrapper> vaccineWrappers = new ArrayList<>();
        VaccineWrapper vaccineWrapper = new VaccineWrapper();

        vaccineWrapper.setId(commonPersonObjectClient.entityId());
        vaccineWrapper.setGender(commonPersonObjectClient.getDetails().get(DBConstants.KEY.GENDER));

        DoseStatus doseStatus = Utils.getCurrentDoseStatus(commonPersonObjectClient);
        DateTime vaccineDateTime = null;

        if (dateDoseOneGiven == null) {
            vaccineWrapper.setName("HPV 1");
            vaccineWrapper.setDefaultName("HPV 1");
            vaccineDateTime = (new DateTime(doseStatus.getDoseOneDate())).toDateTime();
            vaccineWrapper.setVaccineDate(vaccineDateTime);
        } else {
            vaccineWrapper.setName("HPV 2");
            vaccineWrapper.setDefaultName("HPV 2");
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

        vaccineWrapper.setPatientName(childName.trim());
        vaccineWrappers.add(vaccineWrapper);
        vaccinationHelper.addVaccinationDialogFragment(vaccineWrappers, new VaccineGroup(context));
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

    private void updateCommonPersonObjectClient(Map<String, String> patientDetails) {

        if (patientDetails.containsKey(DBConstants.KEY.DOSE_ONE_DATE)) {
            commonPersonObjectClient.getColumnmaps().put(DBConstants.KEY.DOSE_ONE_DATE, patientDetails.get(DBConstants.KEY.DOSE_ONE_DATE));
        }
        if (patientDetails.containsKey(DBConstants.KEY.DATE_DOSE_ONE_GIVEN)) {
            commonPersonObjectClient.getColumnmaps().put(DBConstants.KEY.DATE_DOSE_ONE_GIVEN, patientDetails.get(DBConstants.KEY.DATE_DOSE_ONE_GIVEN));
        }
        if (patientDetails.containsKey(DBConstants.KEY.DOSE_ONE_GIVEN_LOCATION)) {
            commonPersonObjectClient.getColumnmaps().put(DBConstants.KEY.DOSE_ONE_GIVEN_LOCATION, patientDetails.get(DBConstants.KEY.DOSE_ONE_GIVEN_LOCATION));
        }
        if (patientDetails.containsKey(DBConstants.KEY.DOSE_TWO_DATE)) {
            commonPersonObjectClient.getColumnmaps().put(DBConstants.KEY.DOSE_TWO_DATE, patientDetails.get(DBConstants.KEY.DOSE_TWO_DATE));
        }
        if (patientDetails.containsKey(DBConstants.KEY.DATE_DOSE_TWO_GIVEN)) {
            commonPersonObjectClient.getColumnmaps().put(DBConstants.KEY.DATE_DOSE_TWO_GIVEN, patientDetails.get(DBConstants.KEY.DATE_DOSE_TWO_GIVEN));
        }
        if (patientDetails.containsKey(DBConstants.KEY.DOSE_TWO_GIVEN_LOCATION)) {
            commonPersonObjectClient.getColumnmaps().put(DBConstants.KEY.DOSE_TWO_GIVEN_LOCATION, patientDetails.get(DBConstants.KEY.DOSE_TWO_GIVEN_LOCATION));
        }

    }
}
