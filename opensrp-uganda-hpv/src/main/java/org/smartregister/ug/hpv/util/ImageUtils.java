package org.smartregister.ug.hpv.util;

import org.apache.commons.lang3.StringUtils;
import org.opensrp.api.constants.Gender;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Photo;
import org.smartregister.domain.ProfileImage;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.application.HpvApplication;

import static org.smartregister.util.Utils.getValue;

/**
 * Created by keyman on 22/02/2017.
 */
public class ImageUtils {

    public static int profileImageResourceByGender(String gender) {
        return R.drawable.ic_african_girl;
    }

    public static int profileImageResourceByGender(Gender gender) {
        return R.drawable.ic_african_girl;
    }

    public static Photo profilePhotoByClient(CommonPersonObjectClient client) {
        Photo photo = new Photo();
        ProfileImage profileImage = HpvApplication.getInstance().getContext().imageRepository().findByEntityId(client.entityId());
        if (profileImage != null) {
            photo.setFilePath(profileImage.getFilepath());
        } else {
            String gender = getValue(client, "gender", true);
            photo.setResourceId(profileImageResourceByGender(gender));
        }
        return photo;
    }

}
