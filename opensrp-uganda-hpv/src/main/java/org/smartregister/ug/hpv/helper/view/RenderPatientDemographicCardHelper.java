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
import org.opensrp.api.constants.Gender;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Photo;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.activity.BasePatientDetailActivity;
import org.smartregister.ug.hpv.util.DBConstants;
import org.smartregister.ug.hpv.util.ImageUtils;
import org.smartregister.ug.hpv.util.Utils;
import org.smartregister.util.OpenSRPImageLoader;
import org.smartregister.view.activity.DrishtiApplication;

import java.util.Map;


/**
 * Created by ndegwamartin on 09/04/2018.
 */

public class RenderPatientDemographicCardHelper extends BaseRenderHelper implements View.OnClickListener {
    private static final String TAG = RenderPatientDemographicCardHelper.class.getCanonicalName();
    private ImageView profileImageView;


    public RenderPatientDemographicCardHelper(Context context, CommonPersonObjectClient client) {
        super(context, client);

    }

    @Override
    public void renderView(final View view) {
        final RenderPatientDemographicCardHelper cardHelperContext = this;
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                try {
                    Map<String, String> patientDetails = commonPersonObjectClient.getDetails();
                    TextView opensrpIdTextView = (TextView) view.findViewById(R.id.openSrpIdTextView);
                    opensrpIdTextView.setText(patientDetails.get(DBConstants.KEY.OPENSRP_ID) + ", Class: " + patientDetails.get(DBConstants.KEY.CLASS));

                    TextView clientAgeTextView = (TextView) view.findViewById(R.id.clientAgeTextView);
                    String dobString = patientDetails.get(DBConstants.KEY.DOB);
                    String formattedAge = Utils.getFormattedAgeString(dobString);
                    clientAgeTextView.setText("Age " + formattedAge);

                    TextView clientNameTextView = (TextView) view.findViewById(R.id.clientNameTextView);
                    String fullName = patientDetails.get(DBConstants.KEY.FIRST_NAME) + " " + patientDetails.get(DBConstants.KEY.LAST_NAME);
                    clientNameTextView.setText(WordUtils.capitalizeFully(fullName));

                    profileImageView = (ImageView) view.findViewById(R.id.patientImageView);
                    refreshProfileImage(profileImageView);

                    profileImageView.setOnClickListener(cardHelperContext);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }

        });
    }

    private void refreshProfileImage(ImageView profileImageView) {

        Photo photo = ImageUtils.profilePhotoByClientID(commonPersonObjectClient.entityId());

        if (StringUtils.isNotBlank(photo.getFilePath())) {
            try {
                Bitmap myBitmap = BitmapFactory.decodeFile(photo.getFilePath());
                profileImageView.setImageBitmap(myBitmap);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());

                int backgroundResource = R.drawable.ic_african_girl;
                profileImageView.setBackground(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? context.getDrawable(backgroundResource) : ContextCompat.getDrawable(context, backgroundResource));

            }
        } else {
            int backgroundResource = photo.getResourceId();
            profileImageView.setBackground(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? context.getDrawable(backgroundResource) : ContextCompat.getDrawable(context, backgroundResource));


        }
        // if (org.smartregister.util.Utils.getValue(commonPersonObjectClient.getColumnmaps(), "has_profile_image", false).equals("true")) {
        profileImageView.setTag(org.smartregister.R.id.entity_id, commonPersonObjectClient.entityId());
        DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(commonPersonObjectClient.entityId(), OpenSRPImageLoader.getStaticImageListener(profileImageView, 0, 0));
        // }
    }

    @Override
    public void onClick(View view) {
        ((BasePatientDetailActivity) this.context).dispatchTakePictureIntent();
    }

    private boolean isDataOk() {
        return commonPersonObjectClient != null && commonPersonObjectClient.getDetails() != null;
    }

    public void updateProfilePicture(Gender gender) {
        if (isDataOk() && commonPersonObjectClient.entityId() != null) { //image already in local storage most likey ):
            //set profile image by passing the client id.If the image doesn't exist in the image repository then download and save locally
            profileImageView.setTag(org.smartregister.R.id.entity_id, commonPersonObjectClient.entityId());
            DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(commonPersonObjectClient.entityId(), OpenSRPImageLoader.getStaticImageListener(profileImageView, ImageUtils.getProfileImageResourceIDentifier(), ImageUtils.getProfileImageResourceIDentifier()));
        }

        refreshProfileImage(profileImageView);
    }
}
