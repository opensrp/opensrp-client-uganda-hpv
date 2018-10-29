package org.smartregister.ug.hpv.util;

import android.content.ContentValues;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.application.HpvApplication;
import org.smartregister.ug.hpv.domain.DoseStatus;
import org.smartregister.ug.hpv.event.BaseEvent;
import org.smartregister.ug.hpv.repository.PatientRepository;
import org.smartregister.util.DateUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static org.smartregister.immunization.repository.VaccineRepository.ID_COLUMN;
import static org.smartregister.immunization.repository.VaccineRepository.VACCINE_TABLE_NAME;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static org.smartregister.util.Log.logError;

/**
 * Created by ndegwamartin on 14/03/2018.
 */

public class Utils {

    private static final String TAG = Utils.class.getCanonicalName();
    private static final SimpleDateFormat DB_DF = new SimpleDateFormat("yyyy-MM-dd");
    public static final int DOSE_EXPIRY_WINDOW_DAYS = 10;

    public static final ArrayList<String> ALLOWED_LEVELS;
    public static final String DEFAULT_LOCATION_LEVEL = "Health Facility";
    public static final String SCHOOL = "School";


    static {
        ALLOWED_LEVELS = new ArrayList<>();
        ALLOWED_LEVELS.add(DEFAULT_LOCATION_LEVEL);
        ALLOWED_LEVELS.add(SCHOOL);
    }


    public static void addVaccine(VaccineRepository vaccineRepository, Vaccine vaccine) {
        try {
            if (vaccineRepository == null || vaccine == null) {
                return;
            }
            vaccineRepository.add(vaccine);
        } catch (Exception e) {
            Log.e(Utils.class.getCanonicalName(), Log.getStackTraceString(e));
        }
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

    }

    public static void showShortToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

    }

    public static void saveLanguage(String language) {
        AllSharedPreferences allSharedPreferences = new AllSharedPreferences(PreferenceManager.getDefaultSharedPreferences(HpvApplication.getInstance().getApplicationContext()));
        allSharedPreferences.saveLanguagePreference(language);
        setLocale(new Locale(language));


    }


    public static String getLanguage() {
        AllSharedPreferences allSharedPreferences = new AllSharedPreferences(PreferenceManager.getDefaultSharedPreferences(HpvApplication.getInstance().getApplicationContext()));
        return allSharedPreferences.fetchLanguagePreference();
    }

    public static void setLocale(Locale locale) {
        Resources resources = HpvApplication.getInstance().getApplicationContext().getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale);
            HpvApplication.getInstance().getApplicationContext().createConfigurationContext(configuration);
        } else {
            configuration.locale = locale;
            resources.updateConfiguration(configuration, displayMetrics);
        }
    }

    public static void postEvent(BaseEvent event) {
        EventBus.getDefault().post(event);
    }

    public static void postStickyEvent(BaseEvent event) {//Each Sticky event must be manually cleaned by calling Utils.removeStickyEvent after handling
        EventBus.getDefault().postSticky(event);
    }

    public static void removeStickyEvent(BaseEvent event) {
        EventBus.getDefault().removeStickyEvent(event);

    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static int getTokenStringResourceId(Context context, String token) {
        return context.getResources().getIdentifier(token, "string", context.getPackageName());
    }

    public static int getLayoutIdentifierResourceId(Context context, String token) {
        return context.getResources().getIdentifier(token, "id", context.getPackageName());
    }

    public static String readPrefString(Context context, final String key, String defaultValue) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(key, defaultValue);
    }

    public static void writePrefString(Context context, final String key, final String value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static Date dobStringToDate(String dobString) {
        DateTime dateTime = dobStringToDateTime(dobString);
        if (dateTime != null) {
            return dateTime.toDate();
        }
        return null;
    }

    public static DateTime dobStringToDateTime(String dobString) {
        try {
            if (StringUtils.isBlank(dobString)) {
                return null;
            }
            return new DateTime(dobString);

        } catch (Exception e) {
            return null;
        }
    }

    public static String formatDate(String date) {
        return StringUtils.isNotEmpty(date) ? new DateTime(date).toString("dd/MM/yy") : date;
    }

    public static String getDuration(String date) {
        DateTime duration;
        if (StringUtils.isNotBlank(date)) {
            try {
                duration = new DateTime(date);
                return DateUtil.getDuration(duration);
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
        }
        return "";
    }


    public static String convertDateFormat(Date date, SimpleDateFormat formatter) {

        return formatter.format(date);
    }

    public static String getTodaysDate() {
        return convertDateFormat(Calendar.getInstance().getTime(), DB_DF);
    }

    public static int convertDpToPx(Context context, int dp) {
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return Math.round(px);
    }

    public static void putAll(Map<String, String> map, Map<String, String> extend) {
        Collection<String> values = extend.values();
        while (true) {
            if (!(values.remove(null))) break;
        }
        map.putAll(extend);
    }

    public static String getFormattedAgeString(String dobString) {
        String formattedAge = "";
        if (!TextUtils.isEmpty(dobString)) {
            DateTime dateTime = new DateTime(dobString);
            Date dob = dateTime.toDate();
            long timeDiff = Calendar.getInstance().getTimeInMillis() - dob.getTime();

            if (timeDiff >= 0) {
                formattedAge = DateUtil.getDuration(timeDiff);
            }
        }
        return formattedAge.contains("y") ? formattedAge.substring(0, formattedAge.indexOf('y')) : formattedAge;
    }

    public static String getFormattedPhoneNumber(String phoneNumber_) {
        if (phoneNumber_ != null) {
            String phoneNumber = phoneNumber_.startsWith("0") ? phoneNumber_.substring(1) : phoneNumber_;
            String[] tokens = Iterables.toArray(Splitter.fixedLength(3).split(phoneNumber), String.class);
            return "256-" + StringUtils.join(tokens, "-");
        } else {
            return "";
        }

    }

    public static void updateEcPatient(String baseEntityId, String vaccineName, @Nullable Date date, @Nullable  String locationId) {
        Log.d(TAG, "Starting processEC_Patient table");

        String doseNumber = "one";
        if ("HPV 2".equals(vaccineName)) {
            doseNumber = "two";
        }

        String dateString = null;
        if (date != null) {
            dateString = org.smartregister.ug.hpv.util.Utils.convertDateFormat(date, new SimpleDateFormat("yyyy-MM-dd"));
        }

        PatientRepository.updateDoseDates(baseEntityId, dateString, doseNumber, locationId);

        Log.d(TAG, "Finish processEC_Patient table");
    }


    public static boolean isEmptyMap(Map map) {
        return map == null || map.isEmpty();
    }

    public static boolean isEmptyCollection(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static DoseStatus getCurrentDoseStatus(CommonPersonObjectClient pc) {

        DoseStatus doseStatus = new DoseStatus();

        doseStatus.setDoseOneDate(org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOSE_ONE_DATE, false));

        doseStatus.setDoseTwoDate(org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOSE_TWO_DATE, false));

        doseStatus.setDateDoseOneGiven(org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DATE_DOSE_ONE_GIVEN, false));

        doseStatus.setDateDoseTwoGiven(org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DATE_DOSE_TWO_GIVEN, false));

        doseStatus.setDoseTwoDue(StringUtils.isBlank(doseStatus.getDateDoseOneGiven()) && isDoseTwoDue(doseStatus.getDoseTwoDate()));

        doseStatus.setDoseOneGivenLocation(org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOSE_ONE_GIVEN_LOCATION, false));

        doseStatus.setDoseTwoGivenLocation(org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOSE_TWO_GIVEN_LOCATION, false));

        return doseStatus;
    }

    public static boolean isDoseTwoDue(String date) {
        if (StringUtils.isNotBlank(date)) {
            DateTime doseDate = new DateTime(org.smartregister.util.Utils.toDate(date, true));

            // One extra day to move the time to this day's end
            DateTime doseDateEnd = doseDate.plusDays(DOSE_EXPIRY_WINDOW_DAYS + 1);
            return ((doseDateEnd.isAfterNow() || doseDateEnd.isEqualNow()) && (doseDate.isBeforeNow() || doseDate.isEqualNow()));
        }

        return false;
    }

    public static String calculateVaccineDueDate(String date) {

        DateTime dateTime = new DateTime(org.smartregister.util.Utils.toDate(date, true));
        DateTime dueDate = dateTime.plusMonths(6);

        return convertDateFormat(dueDate.toDate(), DB_DF);
    }

    /**
     * Is the dose due based on the dose date & the current time
     *
     * @param doseDateString
     * @return {@code true} if the dose is due i.e. else {@code false}
     */
    public static boolean isDoseDue(String doseDateString) {
        if (StringUtils.isNotBlank(doseDateString)) {
            DateTime doseDate = new DateTime(org.smartregister.util.Utils.toDate(doseDateString, true));

            // One extra day to move the time to this day's end
            DateTime doseDateEnd = doseDate.plusDays(DOSE_EXPIRY_WINDOW_DAYS + 1);
            return ((doseDateEnd.isAfterNow() || doseDateEnd.isEqualNow()) && (doseDate.isBeforeNow() || doseDate.isEqualNow()));
        }

        return false;
    }

    public static Drawable getDoseButtonBackground(@Nullable Context context, Constants.State state) {

        int backgroundResource;

        if (state.equals(Constants.State.INACTIVE) || state.equals(Constants.State.FULLY_IMMUNIZED)) {
            backgroundResource = R.drawable.due_vaccine_grey_bg;
        } else if (state.equals(Constants.State.DUE)) {
            backgroundResource = R.drawable.due_vaccine_blue_bg;
        } else if (state.equals(Constants.State.OVERDUE)) {
            backgroundResource = R.drawable.due_vaccine_red_bg;
        } else {
            backgroundResource = R.color.transparent;
        }

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? context.getDrawable(backgroundResource) : ContextCompat.getDrawable(context, backgroundResource);
    }

    public static int getDoseButtonTextColor(@NonNull Context context, Constants.State doseState) {
        if (doseState.equals(Constants.State.FULLY_IMMUNIZED) || doseState.equals(Constants.State.INACTIVE)) {
            return context.getResources().getColor(R.color.lighter_grey_text);
        }

        return context.getResources().getColor(R.color.white);
    }

    public static Constants.State getRegisterViewButtonStatus(DoseStatus doseStatus) {
        Constants.State doseOneStatus = getDoseOneStatus(doseStatus);
        if (doseOneStatus.equals(Constants.State.FULLY_IMMUNIZED)) {
            return getDoseTwoStatus(doseStatus);
        }

        return doseOneStatus;
    }

    public static Constants.State getDoseOneStatus(DoseStatus doseStatus) {
        if (StringUtils.isNotBlank(doseStatus.getDateDoseOneGiven())) {
            return Constants.State.FULLY_IMMUNIZED;
        }

        if (isDoseDue(doseStatus.getDoseOneDate())) {
            return Constants.State.DUE;
        }

        if (isDoseOverdue(doseStatus.getDoseOneDate())) {
            return Constants.State.OVERDUE;
        }

        // Probably inactive
        return Constants.State.INACTIVE;
    }

    public static Constants.State getDoseTwoStatus(DoseStatus doseStatus) {
        if (StringUtils.isNotBlank(doseStatus.getDateDoseTwoGiven())) {
            return Constants.State.FULLY_IMMUNIZED;
        }

        if (isDoseDue(doseStatus.getDoseTwoDate())) {
            return Constants.State.DUE;
        }

        if (isDoseOverdue(doseStatus.getDoseTwoDate())) {
            return Constants.State.OVERDUE;
        }

        // Probably inactive
        return Constants.State.INACTIVE;
    }

    public static boolean isDoseOverdue(DoseStatus doseStatus) {
        Boolean isDoseTwo = StringUtils.isNotBlank(doseStatus.getDoseTwoDate());
        DateTime doseDate = new DateTime(org.smartregister.util.Utils.toDate(isDoseTwo ? doseStatus.getDoseTwoDate() : doseStatus.getDoseOneDate(), true));
        DateTime expiryDate = doseDate.plusDays(DOSE_EXPIRY_WINDOW_DAYS);

        return expiryDate.isBeforeNow();
    }

    public static boolean isDoseOverdue(String doseDateString) {
        DateTime doseDate = new DateTime(org.smartregister.util.Utils.toDate(doseDateString, true));
        DateTime expiryDate = doseDate.plusDays(DOSE_EXPIRY_WINDOW_DAYS + 1);

        return expiryDate.isBeforeNow();
    }

    private Drawable getDoseButtonBackground(@NonNull Context context, DoseStatus doseStatus) {

        int backgroundResource;

        if (StringUtils.isNotBlank(doseStatus.getDateDoseTwoGiven()) || doseStatus.isDoseTwoDue()) {
            backgroundResource = R.color.transparent;
        } else if (doseStatus.isDoseTwoDue()) {
            backgroundResource = R.drawable.due_vaccine_grey_bg_no_radius;
        } else {

            backgroundResource = isDoseExpired(doseStatus) ? R.drawable.due_vaccine_red_bg_no_radius : R.drawable.due_vaccine_blue_bg_no_radius;

        }

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? context.getDrawable(backgroundResource) : ContextCompat.getDrawable(context, backgroundResource);
    }

    private boolean isDoseExpired(DoseStatus doseStatus) {
        Boolean isDoseTwo = StringUtils.isNotBlank(doseStatus.getDoseTwoDate());
        DateTime doseDate = new DateTime(org.smartregister.util.Utils.toDate(isDoseTwo ? doseStatus.getDoseTwoDate() : doseStatus.getDoseOneDate(), true));
        DateTime expiryDate = doseDate.plusDays(DOSE_EXPIRY_WINDOW_DAYS);

        return expiryDate.isBeforeNow();
    }

    public static void updateVaccineTable(SQLiteDatabase database, Vaccine vaccine, Map<String, String> contentVals) {

        if (vaccine == null || vaccine.getId() == null) {
            return;
        }

        ContentValues contentValues = new ContentValues();
        for (Map.Entry<String, String> entry : contentVals.entrySet()) {
            contentValues.put(entry.getKey(), entry.getValue());
        }

        try {
            String idSelection = ID_COLUMN + " = ?";
            database.update(VACCINE_TABLE_NAME, contentValues, idSelection, new String[]{vaccine.getId().toString()});
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public static void hideKeyboard(Activity activityContext) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activityContext.getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activityContext.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            logError("Error encountered while hiding keyboard " + e);
        }
    }
}
