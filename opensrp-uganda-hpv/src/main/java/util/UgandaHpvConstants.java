package util;

import org.smartregister.AllConstants;
import org.smartregister.ug.hpv.BuildConfig;

/**
 * Created by vkaruri on 19/03/2018.
 */

public class UgandaHpvConstants extends AllConstants {
    public static final boolean TIME_CHECK = BuildConfig.TIME_CHECK;
    public static final String IS_REMOTE_LOGIN = "is_remote_login";
    public static final long MAX_SERVER_TIME_DIFFERENCE = BuildConfig.MAX_SERVER_TIME_DIFFERENCE;
    public static final int OPENMRS_UNIQUE_ID_INITIAL_BATCH_SIZE = BuildConfig.OPENMRS_UNIQUE_ID_INITIAL_BATCH_SIZE;
    public static final int OPENMRS_UNIQUE_ID_BATCH_SIZE = BuildConfig.OPENMRS_UNIQUE_ID_BATCH_SIZE;
    public static final int OPENMRS_UNIQUE_ID_SOURCE = BuildConfig.OPENMRS_UNIQUE_ID_SOURCE;
    public static final String ENGLISH_LOCALE = "en";
    public static final String URDU_LOCALE = "ur";
    public static final String ENGLISH_LANGUAGE = "English";
    public static final String URDU_LANGUAGE = "Urdu";
    public static final String VIEW_CONFIGURATION_PREFIX = "ViewConfiguration_";
    public static final String DOB = "dob";

    public static class CONFIGURATION {
        public static final String LOGIN = "login";
    }

    public static final class KEY {
        public static final String CHILD = "child";
    }


    public enum State {
        DUE,
        OVERDUE,
        EXPIRED,
        INACTIVE,
        FULLY_IMMUNIZED
    }
}
