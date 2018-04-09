package org.smartregister.ug.hpv.helper.view;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import org.apache.commons.lang3.text.WordUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.util.DBConstants;

import java.util.Map;


/**
 * Created by ndegwamartin on 09/04/2018.
 */

public class RenderContactCardHelper extends BaseRenderHelper {

    private static String TAG = RenderContactCardHelper.class.getCanonicalName();

    public RenderContactCardHelper(Context context, CommonPersonObjectClient client) {
        super(context, client);
    }

    @Override
    public void renderView(final View view, final Map<String, String> metadata) {
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                Map<String, String> patientDetails = commonPersonObjectClient.getDetails();
                TextView caretakerNameTextView = (TextView) view.findViewById(R.id.caretakerNameTextView);
                String fullName = patientDetails.get(DBConstants.KEY.CARETAKER_NAME);
                caretakerNameTextView.setText(WordUtils.capitalizeFully(fullName));

                TextView caretakerContactTextView = (TextView) view.findViewById(R.id.caretakerContactTextView);
                String caretakerContact = patientDetails.get(DBConstants.KEY.CARETAKER_PHONE);
                caretakerContactTextView.setText(caretakerContact);

                TextView vhtNameTextView = (TextView) view.findViewById(R.id.vhtNameTextView);
                String vhtName = patientDetails.get(DBConstants.KEY.VHT_NAME);
                vhtNameTextView.setText(WordUtils.capitalizeFully(vhtName));

                TextView vhtContactTextView = (TextView) view.findViewById(R.id.vhtContactTextView);
                String vhtContact = patientDetails.get(DBConstants.KEY.VHT_PHONE);
                vhtContactTextView.setText(vhtContact);
            }

        });

    }

}
