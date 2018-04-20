package org.smartregister.ug.hpv.helper.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.commons.lang3.text.WordUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.activity.BasePatientDetailActivity;
import org.smartregister.ug.hpv.util.Constants;
import org.smartregister.ug.hpv.util.DBConstants;
import org.smartregister.ug.hpv.util.JsonFormUtils;
import org.smartregister.ug.hpv.util.Utils;

import java.util.Map;


/**
 * Created by ndegwamartin on 09/04/2018.
 */

public class RenderContactCardHelper extends BaseRenderHelper implements View.OnClickListener {

    public RenderContactCardHelper(Context context, CommonPersonObjectClient client) {
        super(context, client);
    }

    @Override
    public void renderView(final View view) {
        final RenderContactCardHelper helperContext = this;
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                Map<String, String> patientDetails = commonPersonObjectClient.getDetails();
                TextView caretakerNameTextView = (TextView) view.findViewById(R.id.caretakerNameTextView);
                String fullName = patientDetails.get(DBConstants.KEY.CARETAKER_NAME);
                caretakerNameTextView.setText(WordUtils.capitalizeFully(fullName));

                TextView caretakerContactTextView = (TextView) view.findViewById(R.id.caretakerContactTextView);
                String caretakerContact = patientDetails.get(DBConstants.KEY.CARETAKER_PHONE);
                caretakerContactTextView.setTag(R.id.CONTACT, caretakerContact);
                caretakerContactTextView.setText(Utils.getFormattedPhoneNumber(caretakerContact));
                caretakerContactTextView.setOnClickListener(helperContext);

                TextView vhtNameTextView = (TextView) view.findViewById(R.id.vhtNameTextView);
                String vhtName = patientDetails.get(DBConstants.KEY.VHT_NAME);
                vhtNameTextView.setText(WordUtils.capitalizeFully(vhtName));

                TextView vhtContactTextView = (TextView) view.findViewById(R.id.vhtContactTextView);
                String vhtContact = patientDetails.get(DBConstants.KEY.VHT_PHONE);
                vhtContactTextView.setText(Utils.getFormattedPhoneNumber(vhtContact));

                RelativeLayout vhtContactNumberView = (RelativeLayout) view.findViewById(R.id.vhtContactNumberView);
                vhtContactNumberView.setTag(R.id.CONTACT, caretakerContact);
                vhtContactNumberView.setOnClickListener(helperContext);


                TextView addContactView = (TextView) view.findViewById(R.id.add_contact);
                addContactView.setTag(R.id.CLIENT_ID, patientDetails.get(Constants.KEY._ID));
                addContactView.setTag(Constants.ADD_CONTACT);
                addContactView.setOnClickListener(helperContext);
            }

        });

    }

    @Override
    public void onClick(View view) {
        if (view.getTag(R.id.CONTACT) != null) {
            launchPhoneDialer(view.getTag(R.id.CONTACT).toString());
        } else if (view.getTag() != null && view.getTag().equals(Constants.ADD_CONTACT)) {

            String formMetadata = JsonFormUtils.getAutoPopulatedJsonEditFormString(context, commonPersonObjectClient);
            ((BasePatientDetailActivity) context).startFormActivity(Constants.JSON_FORM.PATIENT_REGISTRATION, view.getTag(R.id.CLIENT_ID
            ).toString(), formMetadata);


        }

    }

    private void launchPhoneDialer(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null));
        context.startActivity(intent);
    }
}
