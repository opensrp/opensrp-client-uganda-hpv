package org.smartregister.ug.hpv.provider;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.joda.time.DateTime;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.configurableviews.helper.ConfigurableViewsHelper;
import org.smartregister.configurableviews.model.ViewConfiguration;
import org.smartregister.cursoradapter.SmartRegisterCLientsProviderForCursorAdapter;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.domain.DoseStatus;
import org.smartregister.ug.hpv.util.DBConstants;
import org.smartregister.ug.hpv.util.Utils;
import org.smartregister.view.contract.SmartRegisterClient;
import org.smartregister.view.contract.SmartRegisterClients;
import org.smartregister.view.dialog.FilterOption;
import org.smartregister.view.dialog.ServiceModeOption;
import org.smartregister.view.dialog.SortOption;
import org.smartregister.view.viewholder.OnClickFormLauncher;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.smartregister.ug.hpv.util.Constants.REGISTER_COLUMNS.DOSE;
import static org.smartregister.ug.hpv.util.Constants.REGISTER_COLUMNS.ID;
import static org.smartregister.ug.hpv.util.Constants.REGISTER_COLUMNS.NAME;
import static org.smartregister.ug.hpv.util.Constants.VIEW_CONFIGS.COMMON_REGISTER_ROW;
import static org.smartregister.util.Utils.getName;

/**
 * Created by ndegwamartin on 14/03/2018.
 */

public class HomeRegisterProvider implements SmartRegisterCLientsProviderForCursorAdapter {
    private final LayoutInflater inflater;
    private Set<org.smartregister.configurableviews.model.View> visibleColumns;
    private View.OnClickListener onClickListener;
    private Context context;
    private static final int DOSE_EXPIRY_WINDOW_DAYS = 10;
    private static final int DOSE_TWO_WINDOW_MONTHS = 6;


    public HomeRegisterProvider(Context context, Set visibleColumns, View.OnClickListener onClickListener) {

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.visibleColumns = visibleColumns;
        this.onClickListener = onClickListener;
        this.context = context;
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, View convertView) {
        CommonPersonObjectClient pc = (CommonPersonObjectClient) client;
        if (visibleColumns.isEmpty()) {
            populatePatientColumn(pc, client, convertView);
            populateIdentifierColumn(pc, convertView);
            populateDoseColumn(pc, convertView);

            return;
        }
        for (org.smartregister.configurableviews.model.View columnView : visibleColumns) {
            switch (columnView.getIdentifier()) {
                case ID:
                    populatePatientColumn(pc, client, convertView);
                    break;
                case NAME:
                    populateIdentifierColumn(pc, convertView);
                    break;
                case DOSE:
                    populateDoseColumn(pc, convertView);
                    break;
                default:
            }
        }

        Map<String, Integer> mapping = new HashMap();
        mapping.put(ID, R.id.patient_column);
        mapping.put(DOSE, R.id.identifier_column);
        mapping.put(NAME, R.id.dose_column);
        ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper().processRegisterColumns(mapping, convertView, visibleColumns, R.id.register_columns);
    }

    private void populatePatientColumn(CommonPersonObjectClient pc, SmartRegisterClient client, View view) {

        String firstName = org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
        String lastName = org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);
        String patientName = getName(firstName, lastName);

        fillValue((TextView) view.findViewById(R.id.patient_name), WordUtils.capitalize(patientName));
        fillValue((TextView) view.findViewById(R.id.caretaker_name), WordUtils.capitalize(org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.CARETAKER_NAME, false)));

        String dobString = Utils.getDuration(org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOB, false));
        fillValue((TextView) view.findViewById(R.id.age), dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : dobString);

        View patient = view.findViewById(R.id.patient_column);
        attachPatientOnclickListener(patient, client);
    }


    private void populateIdentifierColumn(CommonPersonObjectClient pc, View view) {

        fillValue((TextView) view.findViewById(R.id.opensrp_id), org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.OPENSRP_ID, false));
    }


    private void populateDoseColumn(CommonPersonObjectClient pc, View view) {

        Button patient = (Button) view.findViewById(R.id.dose_button);

        DoseStatus doseStatus = getCurrentDoseStatus(pc);

        patient.setText(getDoseButtonText(doseStatus));
        patient.setBackground(getDoseButtonBackground(doseStatus));
        patient.setTextColor((StringUtils.isNotBlank(doseStatus.getDateDoseTwoGiven()) || doseStatus.isDoseTwoDue()) ? context.getResources().getColor(R.color.lighter_grey_text) : patient.getCurrentTextColor());
        attachDosageOnclickListener(patient, doseStatus);
    }

    private DoseStatus getCurrentDoseStatus(CommonPersonObjectClient pc) {

        DoseStatus doseStatus = new DoseStatus();

        doseStatus.setDoseOneDate(org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOSE_ONE_DATE, false));

        doseStatus.setDoseTwoDate(org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOSE_TWO_DATE, false));

        doseStatus.setDateDoseOneGiven(org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DATE_DOSE_ONE_GIVEN, false));

        doseStatus.setDateDoseOneGiven(org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DATE_DOSE_TWO_GIVEN, false));

        doseStatus.setDoseTwoDue(StringUtils.isBlank(doseStatus.getDateDoseOneGiven()) && isDoseTwoDue(doseStatus.getDoseTwoDate()));

        return doseStatus;
    }

    private boolean isDoseTwoDue(String date) {
        if (StringUtils.isNotBlank(date)) {

            DateTime doseDate = new DateTime(org.smartregister.util.Utils.toDate(date, true));
            DateTime dueDate = doseDate.plusMonths(DOSE_TWO_WINDOW_MONTHS);
            return dueDate.isBeforeNow();
        } else {
            return false;
        }
    }

    private String getDoseButtonText(DoseStatus doseStatus) {

        String doseText;

        if (StringUtils.isNotBlank(doseStatus.getDateDoseTwoGiven())) {
            doseText = context.getResources().getString(R.string.complete);
        } else {

            if (StringUtils.isNotBlank(doseStatus.getDoseTwoDate())) {
                doseText = "D2 Due \n" + Utils.formatDate(doseStatus.getDoseTwoDate()) + " \n D1: " + Utils.formatDate(doseStatus.getDoseOneDate());

            } else {
                doseText = "D1 Due \n" + Utils.formatDate(doseStatus.getDoseOneDate());

            }
        }

        return doseText;
    }

    private Drawable getDoseButtonBackground(DoseStatus doseStatus) {

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

    private void adjustLayoutParams(View view, TextView details) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(params);

        params = details.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        details.setLayoutParams(params);
    }

    private void attachPatientOnclickListener(View view, SmartRegisterClient client) {
        view.setOnClickListener(onClickListener);
        view.setTag(client);
    }

    private void attachDosageOnclickListener(View view, DoseStatus doseStatus) {
        view.setOnClickListener(onClickListener);
        view.setTag(doseStatus);
    }

    @Override
    public SmartRegisterClients updateClients(FilterOption villageFilter, ServiceModeOption serviceModeOption, FilterOption searchFilter, SortOption sortOption) {
        return null;
    }

    @Override
    public void onServiceModeSelected(ServiceModeOption serviceModeOption) {//Implement Abstract Method
    }

    @Override
    public OnClickFormLauncher newFormLauncher(String formName, String entityId, String metaData) {
        return null;
    }

    @Override
    public LayoutInflater inflater() {
        return inflater;
    }

    @Override
    public View inflatelayoutForCursorAdapter() {
        String viewIdentifier;
        String HOME_REGISTER_ROW = "home_register_row";
        View view;
        viewIdentifier = HOME_REGISTER_ROW;
        view = inflater.inflate(R.layout.register_home_list_row, null);
        ConfigurableViewsHelper helper = ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper();
        if (helper.isJsonViewsEnabled()) {

            ViewConfiguration viewConfiguration = helper.getViewConfiguration(viewIdentifier);
            ViewConfiguration commonConfiguration = helper.getViewConfiguration(COMMON_REGISTER_ROW);

            if (viewConfiguration != null) {
                return helper.inflateDynamicView(viewConfiguration, commonConfiguration, view, R.id.register_columns, false);
            }
        }
        return view;
    }

    public static void fillValue(TextView v, String value) {
        if (v != null)
            v.setText(value);

    }

}
