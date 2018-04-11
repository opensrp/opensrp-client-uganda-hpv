package org.smartregister.ug.hpv.helper.view;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.util.DBConstants;
import org.smartregister.ug.hpv.util.Utils;

import java.util.Map;

/**
 * Created by ndegwamartin on 09/04/2018.
 */

public class RenderPatientFollowupCardHelper extends BaseRenderHelper {

    private static final String TAG = RenderPatientFollowupCardHelper.class.getCanonicalName();

    public RenderPatientFollowupCardHelper(Context context, CommonPersonObjectClient client) {
        super(context, client);
    }

    @Override
    public void renderView(final View view, final Map<String, String> extraDetails) {
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                try {

                    Map<String, String> patientDetails = commonPersonObjectClient.getDetails();
                    Button followUpView = (Button) view.findViewById(R.id.follow_up_button);
                    followUpView.setAllCaps(false);
                    String dateDoseOneGiven = patientDetails.get(DBConstants.KEY.DATE_DOSE_ONE_GIVEN);
                    String nextVisitDate = StringUtils.isBlank(dateDoseOneGiven) ? patientDetails.get(DBConstants.KEY.DOSE_ONE_DATE) : patientDetails.get(DBConstants.KEY.DOSE_TWO_DATE);

                    if (followUpView != null) {


                        followUpView.setText(context.getString(R.string.hpv_vaccine_due) + " - due " + Utils.formatDate(nextVisitDate));
                        DateTime treatmentStartDate = DateTime.parse(nextVisitDate);
                        int due = Days.daysBetween(new DateTime().withTimeAtStartOfDay(), treatmentStartDate.withTimeAtStartOfDay()).getDays();
                        if (due < 0) {
                            followUpView.setBackgroundResource(R.drawable.due_vaccine_red_bg);
                            followUpView.setTextColor(context.getResources().getColor(R.color.white));
                        } else if (due == 0) {
                            followUpView.setBackgroundResource(R.drawable.due_vaccine_blue_bg);
                            followUpView.setTextColor(context.getResources().getColor(R.color.white));
                        } else {
                            followUpView.setTextColor(context.getResources().getColor(R.color.dark_grey_text));
                            followUpView.setBackgroundResource(R.drawable.due_vaccine_grey_bg);
                        }


                    } else {
                        followUpView.setText(R.string.hpv_vaccine_due);
                        followUpView.setTextColor(context.getResources().getColor(R.color.dark_grey_text));
                        followUpView.setBackgroundResource(R.drawable.due_vaccine_grey_bg);
                    }

                    if (StringUtils.isNotBlank(dateDoseOneGiven)) {
                        TextView doseOneGivenTextView = (TextView) view.findViewById(R.id.dateDoseOneGivenTextView);
                        doseOneGivenTextView.setText("Dose 1 given " + Utils.formatDate(dateDoseOneGiven));
                    }

                    String school = patientDetails.get(DBConstants.KEY.SCHOOL_NAME);
                    if (StringUtils.isNotBlank(school)) {
                        TextView locationTextView = (TextView) view.findViewById(R.id.locationVaccineGivenTextView);
                        locationTextView.setText(StringUtils.capitalize(school));
                    }


                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }

        });
    }
}
