package org.smartregister.ug.hpv.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.google.common.collect.ImmutableMap;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.ug.hpv.application.HpvApplication;
import org.smartregister.ug.hpv.util.Constants;
import org.smartregister.ug.hpv.util.DBConstants;
import org.smartregister.ug.hpv.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

/**
 * Created by ndegwamartin on 25/04/2018.
 */

public class PatientRepository {

    private static final String TAG = PatientRepository.class.getCanonicalName();

    public static Map<String, String> getPatientContacts(String baseEntityId) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = HpvApplication.getInstance().getRepository().getReadableDatabase();

            String query = "SELECT " + DBConstants.KEY.CARETAKER_NAME + "," + DBConstants.KEY.CARETAKER_PHONE + "," + DBConstants.KEY.VHT_NAME + "," + DBConstants.KEY.VHT_PHONE + " FROM " + DBConstants.PATIENT_TABLE_NAME + " WHERE " + DBConstants.KEY.BASE_ENTITY_ID + " = ?";
            cursor = db.rawQuery(query, new String[]{baseEntityId});
            if (cursor != null && cursor.moveToFirst()) {

                if (cursor.getString(cursor.getColumnIndex(DBConstants.KEY.VHT_PHONE)) != null) {
                    return ImmutableMap.of(DBConstants.KEY.CARETAKER_NAME, cursor.getString(cursor.getColumnIndex(DBConstants.KEY.CARETAKER_NAME)), DBConstants.KEY.CARETAKER_PHONE, cursor.getString(cursor.getColumnIndex(DBConstants.KEY.CARETAKER_PHONE)), DBConstants.KEY.VHT_NAME, cursor.getString(cursor.getColumnIndex(DBConstants.KEY.VHT_NAME)), DBConstants.KEY.VHT_PHONE, cursor.getString(cursor.getColumnIndex(DBConstants.KEY.VHT_PHONE)));
                } else {
                    return ImmutableMap.of(DBConstants.KEY.CARETAKER_NAME, cursor.getString(cursor.getColumnIndex(DBConstants.KEY.CARETAKER_NAME)), DBConstants.KEY.CARETAKER_PHONE, cursor.getString(cursor.getColumnIndex(DBConstants.KEY.CARETAKER_PHONE)));

                }

            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public static void updateDoseDates(Vaccine vaccine) {

        try {

            String doseNumber = "one";

            if (vaccine.getName().equals(Constants.HPV_DOSE_NAME.HPV_2) || "hpv_2".equals(vaccine.getName())) {
                doseNumber = "two";
            }

            String date = Utils.convertDateFormat(vaccine.getDate(), new SimpleDateFormat("yyyy-MM-dd"));

            SQLiteDatabase db = HpvApplication.getInstance().getRepository().getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("date_dose_" + doseNumber + "_given", date);
            if ("one".equalsIgnoreCase(doseNumber)) {
                values.put("dose_two_date", Utils.calculateVaccineDueDate(date));
            }
            values.put("dose_" + doseNumber + "_given_location", vaccine.getLocationId());
            values.put(DBConstants.KEY.LAST_INTERACTED_WITH, Calendar.getInstance().getTimeInMillis());
            db.update(DBConstants.PATIENT_TABLE_NAME, values, DBConstants.KEY.BASE_ENTITY_ID + " = ?", new String[]{vaccine.getBaseEntityId()});

        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }


    public static Map<String, String> getPatientVaccinationDetails(String baseEntityId) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = HpvApplication.getInstance().getRepository().getReadableDatabase();

            String query = "SELECT " + DBConstants.KEY.DATE_DOSE_ONE_GIVEN + "," + DBConstants.KEY.DATE_DOSE_TWO_GIVEN + "," + DBConstants.KEY.DOSE_ONE_DATE + "," + DBConstants.KEY.DOSE_TWO_DATE + "," + DBConstants.KEY.DOSE_ONE_GIVEN_LOCATION + "," + DBConstants.KEY.DOSE_TWO_GIVEN_LOCATION + " FROM " + DBConstants.PATIENT_TABLE_NAME + " WHERE " + DBConstants.KEY.BASE_ENTITY_ID + " = ?";
            cursor = db.rawQuery(query, new String[]{baseEntityId});
            if (cursor != null && cursor.moveToFirst()) {

                if (cursor.getString(cursor.getColumnIndex(DBConstants.KEY.DATE_DOSE_TWO_GIVEN)) != null) {
                    return ImmutableMap.of(DBConstants.KEY.DOSE_TWO_DATE, cursor.getString(cursor.getColumnIndex(DBConstants.KEY.DOSE_TWO_DATE)), DBConstants.KEY.DATE_DOSE_ONE_GIVEN, cursor.getString(cursor.getColumnIndex(DBConstants.KEY.DATE_DOSE_ONE_GIVEN)), DBConstants.KEY.DOSE_ONE_GIVEN_LOCATION, cursor.getString(cursor.getColumnIndex(DBConstants.KEY.DOSE_ONE_GIVEN_LOCATION)), DBConstants.KEY.DATE_DOSE_TWO_GIVEN, cursor.getString(cursor.getColumnIndex(DBConstants.KEY.DATE_DOSE_TWO_GIVEN)), DBConstants.KEY.DOSE_TWO_GIVEN_LOCATION, cursor.getString(cursor.getColumnIndex(DBConstants.KEY.DOSE_TWO_GIVEN_LOCATION)));
                } else {
                    boolean isDoseOneGiven = cursor.getString(cursor.getColumnIndex(DBConstants.KEY.DATE_DOSE_ONE_GIVEN)) != null;
                    return ImmutableMap.of(isDoseOneGiven ? DBConstants.KEY.DOSE_TWO_DATE : DBConstants.KEY.DOSE_ONE_DATE, cursor.getString(cursor.getColumnIndex(isDoseOneGiven ? DBConstants.KEY.DOSE_TWO_DATE : DBConstants.KEY.DOSE_ONE_DATE)), DBConstants.KEY.DATE_DOSE_ONE_GIVEN, cursor.getString(cursor.getColumnIndex(DBConstants.KEY.DATE_DOSE_ONE_GIVEN)), DBConstants.KEY.DOSE_ONE_GIVEN_LOCATION, cursor.getString(cursor.getColumnIndex(DBConstants.KEY.DOSE_ONE_GIVEN_LOCATION)));

                }

            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
}
