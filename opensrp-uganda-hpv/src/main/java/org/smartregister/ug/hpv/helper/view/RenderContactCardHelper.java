package org.smartregister.ug.hpv.helper.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.commons.lang3.text.WordUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.activity.BasePatientDetailActivity;
import org.smartregister.ug.hpv.repository.PatientRepository;
import org.smartregister.ug.hpv.util.Constants;
import org.smartregister.ug.hpv.util.DBConstants;
import org.smartregister.ug.hpv.util.JsonFormUtils;
import org.smartregister.ug.hpv.util.Utils;
import org.smartregister.ug.hpv.view.CopyToClipboardDialog;
import org.smartregister.util.PermissionUtils;

import java.util.Map;


/**
 * Created by ndegwamartin on 09/04/2018.
 */

public class RenderContactCardHelper extends BaseRenderHelper implements View.OnClickListener {

    private TextView caretakerNameTextView;
    private TextView caretakerContactTextView;
    private TextView vhtNameTextView;
    private TextView vhtContactTextView;
    private final String TAG = RenderContactCardHelper.class.getCanonicalName();
    private RelativeLayout vhtContactWrapperView;
    private View vhtTitleTextView;
    public static String phoneNumber;

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
                caretakerNameTextView = (TextView) view.findViewById(R.id.caretakerNameTextView);
                String fullName = patientDetails.get(DBConstants.KEY.CARETAKER_NAME);
                caretakerNameTextView.setText(WordUtils.capitalizeFully(fullName));

                caretakerContactTextView = (TextView) view.findViewById(R.id.caretakerContactTextView);
                String caretakerContact = patientDetails.get(DBConstants.KEY.CARETAKER_PHONE);
                caretakerContactTextView.setTag(R.id.CONTACT, caretakerContact);
                caretakerContactTextView.setText(Utils.getFormattedPhoneNumber(caretakerContact));
                caretakerContactTextView.setOnClickListener(helperContext);

                vhtNameTextView = (TextView) view.findViewById(R.id.vhtNameTextView);
                String vhtName = patientDetails.get(DBConstants.KEY.VHT_NAME);
                vhtNameTextView.setText(WordUtils.capitalizeFully(vhtName));

                vhtContactTextView = (TextView) view.findViewById(R.id.vhtContactTextView);
                String vhtContact = patientDetails.get(DBConstants.KEY.VHT_PHONE);


                vhtContactWrapperView = (RelativeLayout) view.findViewById(R.id.vhtContactWrapperView);
                vhtTitleTextView = view.findViewById(R.id.vhtTitleTextView);

                if (vhtContact != null) {
                    vhtContactTextView.setText(Utils.getFormattedPhoneNumber(vhtContact));

                    RelativeLayout vhtContactNumberView = (RelativeLayout) view.findViewById(R.id.vhtContactNumberView);
                    vhtContactNumberView.setTag(R.id.CONTACT, vhtContact);
                    vhtContactNumberView.setOnClickListener(helperContext);
                } else {

                    vhtContactWrapperView.setVisibility(View.GONE);
                    vhtTitleTextView.setVisibility(View.GONE);
                }


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
            phoneNumber = view.getTag(R.id.CONTACT).toString();
            launchPhoneDialer(context, phoneNumber);
        } else if (view.getTag() != null && view.getTag().equals(Constants.ADD_CONTACT)) {

            String formMetadata = JsonFormUtils.getAutoPopulatedJsonEditFormString(context, commonPersonObjectClient);
            ((BasePatientDetailActivity) context).startFormActivity(Constants.JSON_FORM.PATIENT_REGISTRATION, view.getTag(R.id.CLIENT_ID
            ).toString(), formMetadata);

        }

    }

    public static void launchPhoneDialer(Context context, String phoneNumber) {
        if (PermissionUtils.isPermissionGranted((BasePatientDetailActivity) context, Manifest.permission.READ_PHONE_STATE, PermissionUtils.PHONE_STATE_PERMISSION_REQUEST_CODE)) {
            try {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null));
                context.startActivity(intent);
            } catch (Exception e) {

                CopyToClipboardDialog copyToClipboardDialog = new CopyToClipboardDialog(context, R.style.copy_clipboard_dialog);
                copyToClipboardDialog.setContent(phoneNumber);
                copyToClipboardDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                copyToClipboardDialog.show();

            }
        }
    }

    public void refreshContacts(final String baseEntityId) {

        final Handler mHandler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {


                final Map<String, String> contactDetails = PatientRepository.getPatientContacts(baseEntityId);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        caretakerNameTextView.setText(WordUtils.capitalizeFully(contactDetails.get(DBConstants.KEY.CARETAKER_NAME)));
                        caretakerContactTextView.setText(Utils.getFormattedPhoneNumber(contactDetails.get(DBConstants.KEY.CARETAKER_PHONE)));
                        caretakerContactTextView.setTag(R.id.CONTACT, contactDetails.get(DBConstants.KEY.CARETAKER_PHONE));
                        //update common object to enable updated edit contacts content
                        commonPersonObjectClient.getColumnmaps().put(DBConstants.KEY.CARETAKER_NAME, contactDetails.get(DBConstants.KEY.CARETAKER_NAME));
                        commonPersonObjectClient.getColumnmaps().put(DBConstants.KEY.CARETAKER_PHONE, contactDetails.get(DBConstants.KEY.CARETAKER_PHONE));

                        if (contactDetails.containsKey(DBConstants.KEY.VHT_PHONE) && contactDetails.get(DBConstants.KEY.VHT_PHONE) != null) {

                            vhtNameTextView.setText(WordUtils.capitalizeFully(contactDetails.get(DBConstants.KEY.VHT_NAME)));
                            vhtContactTextView.setText(Utils.getFormattedPhoneNumber(contactDetails.get(DBConstants.KEY.VHT_PHONE)));
                            vhtContactTextView.setTag(R.id.CONTACT, contactDetails.get(DBConstants.KEY.VHT_PHONE));
                            commonPersonObjectClient.getColumnmaps().put(DBConstants.KEY.VHT_NAME, contactDetails.get(DBConstants.KEY.VHT_NAME));
                            commonPersonObjectClient.getColumnmaps().put(DBConstants.KEY.VHT_PHONE, contactDetails.get(DBConstants.KEY.VHT_PHONE));
                            vhtContactWrapperView.setVisibility(View.VISIBLE);
                            vhtTitleTextView.setVisibility(View.VISIBLE);

                        }

                    }
                });
            }
        }).start();


    }
}
