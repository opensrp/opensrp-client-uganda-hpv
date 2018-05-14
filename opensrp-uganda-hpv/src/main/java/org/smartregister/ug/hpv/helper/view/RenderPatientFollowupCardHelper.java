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
import org.smartregister.ug.hpv.helper.VaccinationHelper;
import org.smartregister.ug.hpv.util.Constants;
import org.smartregister.ug.hpv.util.DBConstants;
import org.smartregister.ug.hpv.util.ImageUtils;
import org.smartregister.ug.hpv.util.Utils;

import java.util.ArrayList;
import java.util.Map;

import util.UgandaHpvConstants;

import static org.smartregister.util.Utils.getName;
import static org.smartregister.util.Utils.getValue;

/**
 * Created by ndegwamartin on 09/04/2018.
 */

public class RenderPatientFollowupCardHelper extends BaseRenderHelper implements View.OnClickListener {

    private static final String TAG = RenderPatientFollowupCardHelper.class.getCanonicalName();
    private VaccinationHelper vaccinationHelper;

    public RenderPatientFollowupCardHelper(Context context, CommonPersonObjectClient client) {
        super(context, client);
        vaccinationHelper = new VaccinationHelper((BasePatientDetailActivity) context, client);
    }

    @Override
    public void renderView(final View view) {
        final RenderPatientFollowupCardHelper helperContext = this;

        new Handler().post(new Runnable() {

            @Override
            public void run() {
                try {
                    Map<String, String> patientDetails = commonPersonObjectClient.getDetails();
                    Button followUpView = (Button) view.findViewById(R.id.follow_up_button);
                    followUpView.setAllCaps(false);
                    String dateDoseOneGiven = patientDetails.get(DBConstants.KEY.DATE_DOSE_ONE_GIVEN);
                    String nextVisitDate = StringUtils.isBlank(dateDoseOneGiven) ? patientDetails.get(DBConstants.KEY.DOSE_ONE_DATE) : patientDetails.get(DBConstants.KEY.DOSE_TWO_DATE);

                    DoseStatus doseStatus = Utils.getCurrentDoseStatus(commonPersonObjectClient);

                    if (followUpView != null) {
                        followUpView.setOnClickListener(helperContext);
                        followUpView.setText(String.format(context.getString(R.string.vaccine_dose_due_on_date), StringUtils.isBlank(dateDoseOneGiven) ? "1" : "2", Utils.formatDate(nextVisitDate)));

                        UgandaHpvConstants.State doseState = Utils.getRegisterViewButtonStatus(doseStatus);
                        followUpView.setBackground(Utils.getDoseButtonBackground(context, doseState));
                        followUpView.setTextColor(Utils.getDoseButtonTextColor(context, doseState));

                        if (doseState.equals(UgandaHpvConstants.State.INACTIVE) || doseState.equals(UgandaHpvConstants.State.FULLY_IMMUNIZED)) {
                            followUpView.setOnClickListener(null);
                            followUpView.setEnabled(false);
                        }
                    }

                    if (StringUtils.isNotBlank(dateDoseOneGiven)) {
                        TextView doseOneGivenTextView = (TextView) view.findViewById(R.id.dateDoseOneGivenTextView);
                        doseOneGivenTextView.setText(String.format(context.getString(R.string.dose_given_date), "1", Utils.formatDate(dateDoseOneGiven)));
                        doseOneGivenTextView.setVisibility(View.VISIBLE);
                    }

                    String school = patientDetails.get(DBConstants.KEY.SCHOOL_NAME);
                    if (StringUtils.isNotBlank(school)) {
                        TextView locationTextView = (TextView) view.findViewById(R.id.locationVaccineGivenTextView);
                        locationTextView.setVisibility(View.VISIBLE);
                        locationTextView.setText(String.format(context.getString(R.string.patient_location), StringUtils.capitalize(school)));
                    }
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }


//launch vaccination dialog
                boolean launchDialog = ((BasePatientDetailActivity) context).getIntent().getBooleanExtra(Constants.INTENT_KEY.LAUNCH_VACCINE_DIALOG, false);
                if (launchDialog) {
                    showVaccinationDialog(context, commonPersonObjectClient, vaccinationHelper);
                }
            }

        });
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
        vaccineWrapper.setGender(commonPersonObjectClient.getDetails().get("gender"));

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

        String firstName = getValue(commonPersonObjectClient.getColumnmaps(), "first_name", true);
        String lastName = getValue(commonPersonObjectClient.getColumnmaps(), "last_name", true);
        String childName = getName(firstName, lastName);

        vaccineWrapper.setPatientName(childName.trim());
        vaccineWrappers.add(vaccineWrapper);
        vaccinationHelper.addVaccinationDialogFragment(vaccineWrappers, new VaccineGroup(context));
    }

}
