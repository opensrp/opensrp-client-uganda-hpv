package org.smartregister.ug.hpv.util;

import org.smartregister.ug.hpv.BuildConfig;

/**
 * Created by ndegwamartin on 14/03/2018.
 */

public class Constants {
    public static final int OPENMRS_UNIQUE_ID_INITIAL_BATCH_SIZE = BuildConfig.OPENMRS_UNIQUE_ID_INITIAL_BATCH_SIZE;
    public static final int OPENMRS_UNIQUE_ID_BATCH_SIZE = BuildConfig.OPENMRS_UNIQUE_ID_BATCH_SIZE;
    public static final int OPENMRS_UNIQUE_ID_SOURCE = BuildConfig.OPENMRS_UNIQUE_ID_SOURCE;

    public static final long MAX_SERVER_TIME_DIFFERENCE = BuildConfig.MAX_SERVER_TIME_DIFFERENCE;
    public static final boolean TIME_CHECK = BuildConfig.TIME_CHECK;
    public static final String LAST_SYNC_TIMESTAMP = "LAST_SYNC_TIMESTAMP";
    public static final String LAST_CHECK_TIMESTAMP = "LAST_SYNC_CHECK_TIMESTAMP";
    public static final String LAST_VIEWS_SYNC_TIMESTAMP = "LAST_VIEWS_SYNC_TIMESTAMP";

    public static final String PATIENT_TABLE_NAME = "ec_patient";
    public static final String CONTACT_TABLE_NAME = "ec_contact";
    public static final String CURRENT_LOCATION_ID = "CURRENT_LOCATION_ID";

    public static final class REGISTER_COLUMNS {
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String DOSE = "dose";

    }

    public static final class VIEW_CONFIGS {
        public static final String HOME_REGISTER_HEADER = "home_register_header";
        public static final String HOME_REGISTER = "home_register";
        public static final String COMMON_REGISTER_HEADER = "common_register_header";
        public static final String COMMON_REGISTER_ROW = "common_register_row";


    }

    public static final class EventType {

        public static final String REGISTRATION = "Registration";
        public static final String UPDATE_REGISTRATION = "Update Registration";
        public static final String REMOVE = "Remove";
    }

    public static final class KEY {
        public static final String _ID = "_id";
        public static final String KEY = "key";
        public static final String VALUE = "value";
        public static final String NAME = "name";
        public static final String LEVEL = "level";
        public static final String NODE = "node";
        public static final String NODES = "nodes";
        public static final String CHILDREN = "children";
        public static final String LOCATIONID = "locationId";
        public static final String TREE = "tree";
        public static final String DEFAULT = "default";
        public static final String TAGS = "tags";
    }

    public static class INTENT_KEY {
        public static final String FULL_NAME = "full_name";
        public static final String IS_REMOTE_LOGIN = "is_remote_login";
        public static final String REGISTER_TITLE = "register_title";
        public static final String PATIENT_DETAIL_MAP = "patient_detail_map";
        public static final String CLIENT_OBJECT = "client_object";
        public static final String LAST_SYNC_TIME_STRING = "last_manual_sync_time_string";
        public static final String OPENSRP_ID = "opensrp_id";
    }

    public static class CONFIGURATION {
        public static final String LOGIN = "login";
        public static final String HOME = "home";
        public static final String MAIN = "main";
        public static final String LANG = "lang";
        public static final String PATIENT_DETAILS = "patient_details";

        public static class COMPONENTS {
            public static final String PATIENT_DETAILS_DEMOGRAPHICS = "component_patient_details_demographics";
            public static final String PATIENT_DETAILS_CONTACT_SCREENING = "component_patient_details_contact_screening";
            public static final String PATIENT_DETAILS_FOLLOWUP = "component_patient_details_followup";
        }
    }

    public static class JSON_FORM {
        public static final String PATIENT_REGISTRATION = "patient_registration";
        public static final String PATIENT_REMOVAL = "patient_removal";
    }
}
