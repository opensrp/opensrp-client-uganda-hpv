package org.smartregister.ug.hpv.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.apache.commons.lang3.ArrayUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.configurableviews.helper.ConfigurableViewsHelper;
import org.smartregister.configurableviews.model.RegisterConfiguration;
import org.smartregister.configurableviews.model.ViewConfiguration;
import org.smartregister.cursoradapter.CursorCommonObjectFilterOption;
import org.smartregister.cursoradapter.CursorCommonObjectSort;
import org.smartregister.cursoradapter.CursorSortOption;
import org.smartregister.cursoradapter.SecuredNativeSmartRegisterCursorAdapterFragment;
import org.smartregister.cursoradapter.SmartRegisterPaginatedCursorAdapter;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.provider.SmartRegisterClientsProvider;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.activity.BaseRegisterActivity;
import org.smartregister.ug.hpv.provider.PatientRegisterProvider;
import org.smartregister.ug.hpv.servicemode.HpvServiceModeOption;
import org.smartregister.ug.hpv.util.Constants;
import org.smartregister.ug.hpv.util.DBConstants;
import org.smartregister.view.activity.SecuredNativeSmartRegisterActivity;
import org.smartregister.view.dialog.DialogOption;
import org.smartregister.view.dialog.FilterOption;
import org.smartregister.view.dialog.ServiceModeOption;
import org.smartregister.view.dialog.SortOption;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.smartregister.ug.hpv.activity.BaseRegisterActivity.TOOLBAR_TITLE;
import static org.smartregister.ug.hpv.util.Constants.VIEW_CONFIGS.COMMON_REGISTER_HEADER;

/**
 * Created by ndegwamartin on 14/03/2018.
 */

public abstract class BaseRegisterFragment extends SecuredNativeSmartRegisterCursorAdapterFragment {

    protected Set<org.smartregister.configurableviews.model.View> visibleColumns = new TreeSet<>();
    protected CommonPersonObjectClient patient;
    protected RegisterActionHandler registerActionHandler = new RegisterActionHandler();

    private String viewConfigurationIdentifier;

    @Override
    protected SecuredNativeSmartRegisterActivity.DefaultOptionsProvider getDefaultOptionsProvider() {
        return new SecuredNativeSmartRegisterActivity.DefaultOptionsProvider() {


            @Override
            public ServiceModeOption serviceMode() {
                return new HpvServiceModeOption(null, "Linda Clinic", new int[]{
                        R.string.patient_name, R.string.participant_id, R.string.app_name
                }, new int[]{5, 3, 2});
            }

            @Override
            public FilterOption villageFilter() {
                return new CursorCommonObjectFilterOption("no village filter", "");
            }

            @Override
            public SortOption sortOption() {
                return new CursorCommonObjectSort(getResources().getString(R.string.alphabetical_sort), "last_interacted_with desc");
            }

            @Override
            public String nameInShortFormForTitle() {
                return context().getStringResource(R.string.hpv);
            }
        };
    }

    @Override
    protected SecuredNativeSmartRegisterActivity.NavBarOptionsProvider getNavBarOptionsProvider() {
        return new SecuredNativeSmartRegisterActivity.NavBarOptionsProvider() {

            @Override
            public DialogOption[] filterOptions() {
                return new DialogOption[]{};
            }

            @Override
            public DialogOption[] serviceModeOptions() {
                return new DialogOption[]{
                };
            }

            @Override
            public DialogOption[] sortingOptions() {
                return new DialogOption[]{
                        new CursorCommonObjectSort(getResources().getString(R.string.alphabetical_sort), Constants.KEY.FIRST_NAME),
                        new CursorCommonObjectSort(getResources().getString(R.string.participant_id), Constants.KEY.PARTICIPANT_ID)
                };
            }

            @Override
            public String searchHint() {
                return context().getStringResource(R.string.str_search_hint);
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register_activity, container, false);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.register_toolbar);
        AppCompatActivity activity = ((AppCompatActivity) getActivity());
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle(activity.getIntent().getStringExtra(TOOLBAR_TITLE));
        viewConfigurationIdentifier = ((BaseRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        setupViews(view);
        return view;
    }

    protected void processViewConfigurations() {
        ViewConfiguration viewConfiguration = ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper().getViewConfiguration(getViewConfigurationIdentifier());
        if (viewConfiguration == null)
            return;
        RegisterConfiguration config = (RegisterConfiguration) viewConfiguration.getMetadata();
        if (config.getSearchBarText() != null && getView() != null)
            ((EditText) getView().findViewById(R.id.edt_search)).setHint(config.getSearchBarText());
        visibleColumns = ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper().getRegisterActiveColumns(getViewConfigurationIdentifier());

    }

    protected void updateSearchView() {
        if (getSearchView() != null) {
            getSearchView().removeTextChangedListener(textWatcher);
            getSearchView().addTextChangedListener(textWatcher);
        }
    }

    protected void filter(String filterString, String joinTableString, String mainConditionString) {
        filters = filterString;
        joinTable = joinTableString;
        mainCondition = mainConditionString;
        getSearchCancelView().setVisibility(isEmpty(filterString) ? INVISIBLE : VISIBLE);
        CountExecute();
        filterandSortExecute();
    }

    @Override
    public void setupViews(View view) {
        super.setupViews(view);
        clientsView.setVisibility(VISIBLE);
        clientsProgressView.setVisibility(INVISIBLE);
        view.findViewById(R.id.sorted_by_bar).setVisibility(GONE);
        processViewConfigurations();
        initializeQueries();
        updateSearchView();
        populateClientListHeaderView(view);
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        getDefaultOptionsProvider();
        if (isPausedOrRefreshList()) {
            initializeQueries();
        }
        updateSearchView();
        processViewConfigurations();
    }

    protected void initializeQueries() {

        String tableName = DBConstants.PATIENT_TABLE_NAME;

        PatientRegisterProvider hhscp = new PatientRegisterProvider(getActivity(), visibleColumns, registerActionHandler, ConfigurableViewsLibrary.getInstance().getContext().detailsRepository());
        clientAdapter = new SmartRegisterPaginatedCursorAdapter(getActivity(), null, hhscp, context().commonrepository(tableName));
        clientsView.setAdapter(clientAdapter);

        setTablename(tableName);
        SmartRegisterQueryBuilder countQueryBuilder = new SmartRegisterQueryBuilder();
        countQueryBuilder.SelectInitiateMainTableCounts(tableName);
        mainCondition = getMainCondition();
        countSelect = countQueryBuilder.mainCondition(mainCondition);
        super.CountExecute();

        SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
        String[] columns = new String[]{
                tableName + ".relationalid",
                tableName + "." + Constants.KEY.LAST_INTERACTED_WITH,
                tableName + "." + Constants.KEY.FIRST_ENCOUNTER,
                tableName + "." + Constants.KEY.BASE_ENTITY_ID,
                tableName + "." + Constants.KEY.FIRST_NAME,
                tableName + "." + Constants.KEY.LAST_NAME,
                tableName + "." + Constants.KEY.PARTICIPANT_ID,
                tableName + "." + Constants.KEY.PROGRAM_ID,
                tableName + "." + Constants.KEY.GENDER,
                tableName + "." + Constants.KEY.DOB};
        String[] allColumns = ArrayUtils.addAll(columns, getAdditionalColumns(tableName));
        queryBUilder.SelectInitiateMainTable(tableName, allColumns);
        mainSelect = queryBUilder.mainCondition(mainCondition);
        Sortqueries = ((CursorSortOption) getDefaultOptionsProvider().sortOption()).sort();

        currentlimit = 20;
        currentoffset = 0;

        super.filterandSortInInitializeQueries();

        refresh();

    }

    protected abstract void populateClientListHeaderView(View view);

    protected void populateClientListHeaderView(View view, View headerLayout, String viewConfigurationIdentifier) {
        LinearLayout clientsHeaderLayout = (LinearLayout) view.findViewById(org.smartregister.R.id.clients_header_layout);
        clientsHeaderLayout.setVisibility(GONE);

        ConfigurableViewsHelper helper = ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper();
        if (helper.isJsonViewsEnabled()) {
            ViewConfiguration viewConfiguration = helper.getViewConfiguration(viewConfigurationIdentifier);
            ViewConfiguration commonConfiguration = helper.getViewConfiguration(COMMON_REGISTER_HEADER);
            if (viewConfiguration != null)
                headerLayout = helper.inflateDynamicView(viewConfiguration, commonConfiguration, headerLayout, R.id.register_headers, true);
        }
        if (!visibleColumns.isEmpty()) {
            Map<String, Integer> mapping = new HashMap();
            mapping.put(org.smartregister.ug.hpv.util.Constants.REGISTER_COLUMNS.NAME, R.id.patient_header);
            mapping.put(org.smartregister.ug.hpv.util.Constants.REGISTER_COLUMNS.ID, R.id.results_header);
            mapping.put(org.smartregister.ug.hpv.util.Constants.REGISTER_COLUMNS.DOSE, R.id.diagnose_header);
            helper.processRegisterColumns(mapping, headerLayout, visibleColumns, R.id.register_headers);
        }

        clientsView.addHeaderView(headerLayout);
        clientsView.setEmptyView(getActivity().findViewById(R.id.empty_view));

    }

    protected final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(final CharSequence cs, int start, int before, int count) {
            filter(cs.toString(), "", getMainCondition());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    @Override
    protected SmartRegisterClientsProvider clientsProvider() {
        return null;
    }

    @Override
    protected void onInitialization() {//Implement Abstract Method
    }

    @Override
    protected void startRegistration() {//Implement Abstract Method
    }

    @Override
    protected void onCreation() {//Implement Abstract Method
    }

    protected abstract String getMainCondition();


    protected abstract String[] getAdditionalColumns(String tableName);

    protected String getViewConfigurationIdentifier() {
        return viewConfigurationIdentifier;
    }

    private void goToPatientDetailActivity(String viewConfigurationIdentifier) {
    }

    class RegisterActionHandler implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            BaseRegisterActivity registerActivity = (BaseRegisterActivity) getActivity();
            if (view.getTag() != null && view.getTag() instanceof CommonPersonObjectClient) {
                patient = (CommonPersonObjectClient) view.getTag();
            }
        }
    }

}



