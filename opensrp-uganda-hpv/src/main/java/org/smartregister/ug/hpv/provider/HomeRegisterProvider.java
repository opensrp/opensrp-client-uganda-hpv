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

import org.apache.commons.lang3.text.WordUtils;
import org.joda.time.DateTime;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.configurableviews.helper.ConfigurableViewsHelper;
import org.smartregister.configurableviews.model.ViewConfiguration;
import org.smartregister.cursoradapter.SmartRegisterCLientsProviderForCursorAdapter;
import org.smartregister.repository.DetailsRepository;
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
    private static final int DOSE_ONE_WINDOW_DAYS = 10;


    private static final String TAG = HomeRegisterProvider.class.getCanonicalName();


    public HomeRegisterProvider(Context context, Set visibleColumns, View.OnClickListener onClickListener, DetailsRepository detailsRepository) {

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
            populateDoseColumn(pc, client, convertView);

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
                    populateDoseColumn(pc, client, convertView);
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
        attachOnclickListener(patient, client);
    }


    private void populateIdentifierColumn(CommonPersonObjectClient pc, View view) {

        fillValue((TextView) view.findViewById(R.id.opensrp_id), org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.OPENSRP_ID, false));
    }


    private void populateDoseColumn(CommonPersonObjectClient pc, SmartRegisterClient client, View view) {

        Button patient = (Button) view.findViewById(R.id.dose_button);

        DoseStatus doseStatus = getCurrentDoseStatus(pc);

        doseStatus.setDoseOneDate("02-03-2018");
        doseStatus.setDoseTwoDate("04-04-2018");
        doseStatus.setDoseageComplete(false);


        patient.setText(getDoseButtonText(doseStatus));
        patient.setBackground(getDoseButtonBackground(doseStatus));
        patient.setTextColor(doseStatus.isDoseageComplete() ? context.getResources().getColor(R.color.client_list_grey) : patient.getCurrentTextColor());
        attachOnclickListener(patient, client);
    }

    private DoseStatus getCurrentDoseStatus(CommonPersonObjectClient pc) {

        DoseStatus doseStatus = new DoseStatus();

        doseStatus.setDoseOneDate(org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOSE_ONE_DATE, false) != null ? Utils.formatDate(org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOSE_ONE_DATE, false)) : null);

        doseStatus.setDoseTwoDate(org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOSE_TWO_DATE, false) != null ? Utils.formatDate(org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOSE_TWO_DATE, false)) : null);

        doseStatus.setDoseageComplete(org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.IS_DOSE_TWO_GIVEN, false) != null ? Boolean.valueOf(Utils.formatDate(org.smartregister.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.IS_DOSE_TWO_GIVEN, false))) : false);

        return doseStatus;
    }

    private String getDoseButtonText(DoseStatus doseStatus) {

        String doseText;

        if (doseStatus.isDoseageComplete()) {
            doseText = context.getResources().getString(R.string.complete);
        } else {

            if (doseStatus.getDoseTwoDate() != null && !doseStatus.getDoseTwoDate().isEmpty()) {
                doseText = "D2 Due \n" + doseStatus.getDoseTwoDate() + " \n D1: " + doseStatus.getDoseOneDate();

            } else {
                doseText = "D1 Due \n" + doseStatus.getDoseOneDate();

            }
        }

        return doseText;
    }

    private Drawable getDoseButtonBackground(DoseStatus doseStatus) {

        int backgroundResource;

        if (doseStatus.isDoseageComplete()) {
            backgroundResource = R.drawable.due_vaccine_na_bg;
        } else {

            if (doseStatus.getDoseTwoDate() != null) {

                backgroundResource = isDoseExpired(doseStatus.getDoseTwoDate()) ? R.drawable.due_vaccine_blue_bg : R.drawable.due_vaccine_red_bg;

            } else {
                backgroundResource = isDoseExpired(doseStatus.getDoseOneDate()) ? R.drawable.due_vaccine_blue_bg : R.drawable.due_vaccine_red_bg;

            }
        }

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? context.getDrawable(backgroundResource) : ContextCompat.getDrawable(context, backgroundResource);
    }

    private boolean isDoseExpired(String date) {

        DateTime doseDate = new DateTime(org.smartregister.util.Utils.toDate(date, true));
        DateTime expiryDate = doseDate.plusDays(DOSE_ONE_WINDOW_DAYS - 1);
        return expiryDate.isAfterNow();
    }

    private void adjustLayoutParams(View view, TextView details) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(params);

        params = details.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        details.setLayoutParams(params);
    }

    private void attachOnclickListener(View view, SmartRegisterClient client) {
        view.setOnClickListener(onClickListener);
        view.setTag(client);
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
