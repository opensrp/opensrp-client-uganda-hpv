package org.smartregister.ug.hpv.helper.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Photo;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.util.DBConstants;
import org.smartregister.ug.hpv.util.ImageUtils;
import org.smartregister.ug.hpv.util.Utils;

import java.util.Map;


/**
 * Created by ndegwamartin on 09/04/2018.
 */

public class RenderPatientDemographicCardHelper extends BaseRenderHelper {
    private static final String TAG = RenderPatientDemographicCardHelper.class.getCanonicalName();

    public RenderPatientDemographicCardHelper(Context context, CommonPersonObjectClient client) {
        super(context, client);
    }

    @Override
    public void renderView(final View view, final Map<String, String> extraData) {
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                try {
                    Map<String, String> patientDetails = commonPersonObjectClient.getDetails();
                    TextView opensrpIdTextView = (TextView) view.findViewById(R.id.openSrpIdTextView);
                    opensrpIdTextView.setText(patientDetails.get(DBConstants.KEY.OPENSRP_ID));

                    TextView clientAgeTextView = (TextView) view.findViewById(R.id.clientAgeTextView);
                    String dobString = patientDetails.get(DBConstants.KEY.DOB);
                    String formattedAge = Utils.getFormattedAgeString(dobString);
                    clientAgeTextView.setText("Age " + formattedAge);

                    TextView clientNameTextView = (TextView) view.findViewById(R.id.clientNameTextView);
                    String fullName = patientDetails.get(DBConstants.KEY.FIRST_NAME) + " " + patientDetails.get(DBConstants.KEY.LAST_NAME);
                    clientNameTextView.setText(WordUtils.capitalizeFully(fullName));

                    ImageView imageView = (ImageView) view.findViewById(R.id.patientImageView);

                    Photo photo = ImageUtils.profilePhotoByClient(commonPersonObjectClient);

                    if (StringUtils.isNotBlank(photo.getFilePath())) {
                        try {
                            Bitmap myBitmap = BitmapFactory.decodeFile(photo.getFilePath());
                            imageView.setImageBitmap(myBitmap);
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());

                            int backgroundResource = R.drawable.ic_african_girl;
                            imageView.setBackground(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? context.getDrawable(backgroundResource) : ContextCompat.getDrawable(context, backgroundResource));

                        }
                    } else {
                        int backgroundResource = photo.getResourceId();
                        imageView.setBackground(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? context.getDrawable(backgroundResource) : ContextCompat.getDrawable(context, backgroundResource));
                    }

                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Utils.showToast(context, "Take new picture...");

                            // dispatchTakePictureIntent();
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }

        });
    }
}
