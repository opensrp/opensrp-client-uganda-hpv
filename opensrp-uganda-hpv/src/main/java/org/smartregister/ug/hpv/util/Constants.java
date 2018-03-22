package org.smartregister.ug.hpv.util;

import org.smartregister.ug.hpv.BuildConfig;

/**
 * Created by ndegwamartin on 14/03/2018.
 */

public class Constants {

    public static final long MAX_SERVER_TIME_DIFFERENCE = BuildConfig.MAX_SERVER_TIME_DIFFERENCE;
    public static final boolean TIME_CHECK = BuildConfig.TIME_CHECK;
    public static final String LAST_SYNC_TIMESTAMP = "LAST_SYNC_TIMESTAMP";
    public static final String LAST_CHECK_TIMESTAMP = "LAST_SYNC_CHECK_TIMESTAMP";
    public static final String LAST_VIEWS_SYNC_TIMESTAMP = "LAST_VIEWS_SYNC_TIMESTAMP";

    public static final String PATIENT_TABLE_NAME = "ec_patient";
    public static final String CONTACT_TABLE_NAME = "ec_contact";

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

        public static final String AEFI = "AEFI";
        public static final String BITRH_REGISTRATION = "Birth Registration";
        public static final String UPDATE_BITRH_REGISTRATION = "Update Birth Registration";
        public static final String NEW_WOMAN_REGISTRATION = "New Woman Registration";
        public static final String DEATH = "Death";
        public static final String OUT_OF_CATCHMENT_SERVICE = "Out of Catchment Service";
        public static final String VACCINATION = "Vaccination";
    }
}
