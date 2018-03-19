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

    public static final class KEY {
        public static final String FIRST_NAME = "first_name";
        public static final String LAST_NAME = "last_name";
        public static final String DOB = "dob";
        public static final String AGE = "age";
        public static final String PARTICIPANT_ID = "participant_id";
        public static final String PROGRAM_ID = "program_id";
        public static final String GENDER = "gender";
        public static final String BASE_ENTITY_ID = "base_entity_id";
        public static final String FIRST_ENCOUNTER = "first_encounter";
        public static final String LAST_INTERACTED_WITH = "last_interacted_with";
        public static final String CLIENT = "client";
        public static final String TREATMENT_INITIATION_DATE = "treatment_initiation_date";
        public static final String BASELINE = "baseline";
        public static final String NEXT_VISIT_DATE = "next_visit_date";
        public static final String TREATMENT_REGIMEN = "treatment_regimen";
        public static final String OTHER_REGIMEN = "regimen_oth";
        public static final String STEPNAME = "stepname";
        public static final String TREATMENT_MONTH = "month";
        public static final String SMR_NEXT_VISIT_DATE = "smear_due_date";
        public static final String DATE_REMOVED = "date_removed";
        public static final String ATTRIBUTE_DATEREMOVED = "dateRemoved";//different
        public static final String DEATHDATE = "deathdate";
        public static final String PARENT_ENTITY_ID = "parent_entity_id";
        public static final String RELATIONAL_ID = "relational_id";
        public static final String MOTHER = "_index";
        public static final String ENTITY_ID = "enitity_id";
        public static final String VALUE = "value";
        public static final String LOOK_UP = "look_up";
        public static final String NUMBER_PICKER = "number_picker";


    }   public static final class EventType {

        public static final String AEFI = "AEFI";
        public static final String BITRH_REGISTRATION = "Birth Registration";
        public static final String UPDATE_BITRH_REGISTRATION = "Update Birth Registration";
        public static final String NEW_WOMAN_REGISTRATION = "New Woman Registration";
        public static final String DEATH = "Death";
        public static final String OUT_OF_CATCHMENT_SERVICE = "Out of Catchment Service";
        public static final String VACCINATION = "Vaccination";
    }
}
