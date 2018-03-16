package org.smartregister.ug.hpv.provider;

import android.content.Context;
import android.database.Cursor;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.configurableviews.helper.ConfigurableViewsHelper;
import org.smartregister.configurableviews.model.ViewConfiguration;
import org.smartregister.cursoradapter.SmartRegisterCLientsProviderForCursorAdapter;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.util.Constants;
import org.smartregister.ug.hpv.util.TbrSpannableStringBuilder;
import org.smartregister.util.DateUtil;
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

public class PatientRegisterProvider implements SmartRegisterCLientsProviderForCursorAdapter {
    private final LayoutInflater inflater;
    private Context context;
    private Set<org.smartregister.configurableviews.model.View> visibleColumns;
    private View.OnClickListener onClickListener;

    private static final String DETECTED = "detected";
    private static final String NOT_DETECTED = "not_detected";
    private static final String INDETERMINATE = "indeterminate";
    private static final String ERROR = "error";
    private static final String NO_RESULT = "no_result";
    private static final String POSITIVE = "positive";
    private static final String NEGATIVE = "negative";


    private ForegroundColorSpan redForegroundColorSpan;
    private ForegroundColorSpan blackForegroundColorSpan;
    private DetailsRepository detailsRepository;


    private static final String TAG = PatientRegisterProvider.class.getCanonicalName();


    public PatientRegisterProvider(Context context, Set visibleColumns, View.OnClickListener onClickListener, DetailsRepository detailsRepository) {

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.visibleColumns = visibleColumns;
        this.onClickListener = onClickListener;
        redForegroundColorSpan = new ForegroundColorSpan(
                context.getResources().getColor(android.R.color.holo_red_dark));
        blackForegroundColorSpan = new ForegroundColorSpan(
                context.getResources().getColor(android.R.color.black));
        this.detailsRepository = detailsRepository;
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, View convertView) {
        CommonPersonObjectClient pc = (CommonPersonObjectClient) client;
        if (visibleColumns.isEmpty()) {
            populatePatientColumn(pc, client, convertView);

            populatePatientColumn(pc, client, convertView);
            populatePatientColumn(pc, client, convertView);

            return;
        }
        for (org.smartregister.configurableviews.model.View columnView : visibleColumns) {
            switch (columnView.getIdentifier()) {
                case ID:
                    populatePatientColumn(pc, client, convertView);
                    break;
                case NAME:
                    populatePatientColumn(pc, client, convertView);
                    break;
                case DOSE:
                    populatePatientColumn(pc, client, convertView);
                    break;
                default:
            }
        }

        Map<String, Integer> mapping = new HashMap();
        mapping.put(ID, R.id.patient_column);
        mapping.put(DOSE, R.id.results_column);
        mapping.put(NAME, R.id.diagnose_column);
        ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper().processRegisterColumns(mapping, convertView, visibleColumns, R.id.register_columns);
    }

    private void populatePatientColumn(CommonPersonObjectClient pc, SmartRegisterClient client, View view) {

        String firstName = org.smartregister.util.Utils.getValue(pc.getColumnmaps(), Constants.KEY.FIRST_NAME, true);
        String lastName = org.smartregister.util.Utils.getValue(pc.getColumnmaps(), Constants.KEY.LAST_NAME, true);
        String patientName = getName(firstName, lastName);

        fillValue((TextView) view.findViewById(R.id.patient_name), patientName);

        fillValue((TextView) view.findViewById(R.id.participant_id), "#" + org.smartregister.util.Utils.getValue(pc.getColumnmaps(), Constants.KEY.PARTICIPANT_ID, false));

        String gender = org.smartregister.util.Utils.getValue(pc.getColumnmaps(), Constants.KEY.GENDER, true);

        fillValue((TextView) view.findViewById(R.id.gender), gender);

        String dobString = getDuration(org.smartregister.util.Utils.getValue(pc.getColumnmaps(), Constants.KEY.DOB, false));

        fillValue((TextView) view.findViewById(R.id.age), dobString.substring(0, dobString.indexOf("y")));

        View patient = view.findViewById(R.id.patient_column);
        attachOnclickListener(patient, client);
    }

    private boolean populateXpertResult(Map<String, String> testResults, TbrSpannableStringBuilder stringBuilder, boolean withOtherResults) {

        return false;
    }

    private void processXpertResult(String result, TbrSpannableStringBuilder stringBuilder) {
        if (result == null)
            return;
        switch (result) {
            case DETECTED:
                stringBuilder.append("+ve", redForegroundColorSpan);
                break;
            case NOT_DETECTED:
                stringBuilder.append("-ve", blackForegroundColorSpan);
                break;
            case INDETERMINATE:
                stringBuilder.append("?", blackForegroundColorSpan);
                break;
            case ERROR:
                stringBuilder.append("err", blackForegroundColorSpan);
                break;
            case NO_RESULT:
                stringBuilder.append("No result", blackForegroundColorSpan);
                break;
            default:
                break;
        }
    }

    private void populateResultsColumn(CommonPersonObjectClient pc, SmartRegisterClient client, View view) {
        View button = view.findViewById(R.id.result_lnk);
        TextView details = (TextView) view.findViewById(R.id.result_details);
        details.setText("");
        populateResultsColumn(pc, client, new TbrSpannableStringBuilder(), false, null, button, details);
    }

    private void populateResultsColumn(CommonPersonObjectClient pc, SmartRegisterClient client, TbrSpannableStringBuilder stringBuilder, boolean singleResult, Long baseline, View button, TextView details) {

    }

    private void populateSmearResult(TbrSpannableStringBuilder stringBuilder, String result, boolean hasXpert, boolean smearOnlyColumn) {
        if (result == null) return;
        else if (hasXpert && !smearOnlyColumn)
            stringBuilder.append(",\n");
        if (!smearOnlyColumn)
            stringBuilder.append("Smr ");
        switch (result) {
            case "one_plus":
                stringBuilder.append("1+", redForegroundColorSpan);
                break;
            case "two_plus":
                stringBuilder.append("2+", redForegroundColorSpan);
                break;
            case "three_plus":
                stringBuilder.append("3+", redForegroundColorSpan);
                break;
            case "scanty":
                stringBuilder.append(smearOnlyColumn ? "Scanty" : "Sty", redForegroundColorSpan);
                break;
            case "negative":
                stringBuilder.append(smearOnlyColumn ? "Negative" : "Neg", blackForegroundColorSpan);
                break;
            default:
        }
    }


    public String formatDate(String date) {
        return StringUtils.isNotEmpty(date) ? new DateTime(date).toString("dd/MM/yyyy") : date;
    }

    public String getDuration(String date) {
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
        view = inflater.inflate(R.layout.register_presumptive_list_row, null);
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
