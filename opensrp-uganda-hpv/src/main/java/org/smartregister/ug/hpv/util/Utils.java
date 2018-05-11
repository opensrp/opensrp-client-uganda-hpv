package org.smartregister.ug.hpv.util;

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
import android.widget.Toast;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import util.UgandaHpvConstants;

/**
 * Created by ndegwamartin on 14/03/2018.
 */

public class Utils {

    private static final String TAG = Utils.class.getCanonicalName();
    private static final SimpleDateFormat DB_DF = new SimpleDateFormat("yyyy-MM-dd");
    private static BaseEvent myEvent;
    public static final int DOSE_EXPIRY_WINDOW_DAYS = 10;
    public static final int DOSE_TWO_WINDOW_MONTHS = 6;

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

    public static void updateEcPatient(String baseEntityId, String vaccineName, Date date) {
        Log.d(TAG, "Starting processEC_Patient table");

        String doseNumber = "one";
        if (vaccineName.equals("hpv_2")) {
            doseNumber = "two";
        }

        String dateString = org.smartregister.ug.hpv.util.Utils.convertDateFormat(date, new SimpleDateFormat("dd/MM/yy"));
        PatientRepository.updateDoseDates(baseEntityId, dateString, doseNumber);

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

    public static Drawable getDoseButtonBackground(@Nullable Context context, UgandaHpvConstants.State state) {

        int backgroundResource;

        if (state.equals(UgandaHpvConstants.State.INACTIVE) || state.equals(UgandaHpvConstants.State.FULLY_IMMUNIZED)) {
            backgroundResource = R.drawable.due_vaccine_grey_bg;
        } else if (state.equals(UgandaHpvConstants.State.DUE)) {
            backgroundResource = R.drawable.due_vaccine_blue_bg;
        } else if (state.equals(UgandaHpvConstants.State.OVERDUE)) {
            backgroundResource = R.drawable.due_vaccine_red_bg;
        } else {
            backgroundResource = R.color.transparent;
        }

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? context.getDrawable(backgroundResource) : ContextCompat.getDrawable(context, backgroundResource);
    }

    public static int getDoseButtonTextColor(@NonNull Context context, UgandaHpvConstants.State doseState) {
        if (doseState.equals(UgandaHpvConstants.State.FULLY_IMMUNIZED) || doseState.equals(UgandaHpvConstants.State.INACTIVE)) {
            return context.getResources().getColor(R.color.lighter_grey_text);
        }

        return context.getResources().getColor(R.color.white);
    }

    public static UgandaHpvConstants.State getRegisterViewButtonStatus(DoseStatus doseStatus) {
        UgandaHpvConstants.State doseOneStatus = getDoseOneStatus(doseStatus);
        if (doseOneStatus.equals(UgandaHpvConstants.State.FULLY_IMMUNIZED)) {
            return getDoseTwoStatus(doseStatus);
        }

        return doseOneStatus;
    }

    public static UgandaHpvConstants.State getDoseOneStatus(DoseStatus doseStatus) {
        if (StringUtils.isNotBlank(doseStatus.getDateDoseOneGiven())) {
            return UgandaHpvConstants.State.FULLY_IMMUNIZED;
        }

        if (isDoseDue(doseStatus.getDoseOneDate())) {
            return UgandaHpvConstants.State.DUE;
        }

        if (isDoseOverdue(doseStatus.getDoseOneDate())) {
            return UgandaHpvConstants.State.OVERDUE;
        }

        // Probably inactive
        return UgandaHpvConstants.State.INACTIVE;
    }

    public static UgandaHpvConstants.State getDoseTwoStatus(DoseStatus doseStatus) {
        if (StringUtils.isNotBlank(doseStatus.getDateDoseTwoGiven())) {
            return UgandaHpvConstants.State.FULLY_IMMUNIZED;
        }

        if (isDoseDue(doseStatus.getDoseTwoDate())) {
            return UgandaHpvConstants.State.DUE;
        }

        if (isDoseOverdue(doseStatus.getDoseTwoDate())) {
            return UgandaHpvConstants.State.OVERDUE;
        }

        // Probably inactive
        return UgandaHpvConstants.State.INACTIVE;
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

}
