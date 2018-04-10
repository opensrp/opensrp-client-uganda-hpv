package org.smartregister.ug.hpv.fragment;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.configurableviews.helper.ConfigurableViewsHelper;
import org.smartregister.configurableviews.model.ViewConfiguration;
import org.smartregister.domain.FetchStatus;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.event.EnketoFormSaveCompleteEvent;
import org.smartregister.ug.hpv.event.SyncEvent;
import org.smartregister.ug.hpv.helper.view.RenderContactCardHelper;
import org.smartregister.ug.hpv.helper.view.RenderPatientDemographicCardHelper;
import org.smartregister.ug.hpv.helper.view.RenderPatientFollowupCardHelper;
import org.smartregister.ug.hpv.util.Constants;
import org.smartregister.ug.hpv.util.DBConstants;
import org.smartregister.ug.hpv.util.JsonFormUtils;
import org.smartregister.ug.hpv.util.Utils;
import org.smartregister.ug.hpv.view.LocationPickerView;
import org.smartregister.view.fragment.SecuredFragment;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.smartregister.ug.hpv.activity.BaseRegisterActivity.TOOLBAR_TITLE;


/**
 * Created by ndegwamartin on 06/12/2017.
 */

public abstract class BasePatientDetailsFragment extends SecuredFragment implements View.OnClickListener {

    protected Map<String, String> patientDetails;
    protected CommonPersonObjectClient commonPersonObjectClient;
    protected Map<String, String> languageTranslations;
    private static String TAG = BasePatientDetailsFragment.class.getCanonicalName();
    private LocationPickerView facilitySelection;
    private static final int REQUEST_CODE_GET_JSON = 3432;

    protected abstract void setPatientDetails(Map<String, String> patientDetails);


    protected abstract void setClient(CommonPersonObjectClient commonPersonObjectClient);


    protected abstract String getViewConfigurationIdentifier();

    protected void renderDemographicsView(View view, Map<String, String> patientDetails) {
        CommonPersonObjectClient client = (CommonPersonObjectClient) view.getTag();
        RenderPatientDemographicCardHelper renderPatientDemographicCardHelper = new RenderPatientDemographicCardHelper(getActivity(), client);
        renderPatientDemographicCardHelper.renderView(view, patientDetails);

    }

    protected void renderFollowUpView(View view, Map<String, String> patientDetails) {
        CommonPersonObjectClient client = (CommonPersonObjectClient) view.getTag();

        RenderPatientFollowupCardHelper renderPatientFollowupCardHelper = new RenderPatientFollowupCardHelper(getActivity(), client);
        renderPatientFollowupCardHelper.renderView(view, patientDetails);

    }


    protected void renderContactView(View view, Map<String, String> patientDetails) {
        CommonPersonObjectClient client = (CommonPersonObjectClient) view.getTag();
        RenderContactCardHelper renderContactHelper = new RenderContactCardHelper(getActivity(), client);
        renderContactHelper.renderView(view, patientDetails);
    }

    protected void processLanguageTokens(Map<String, String> viewLabelsMap, View parentView) {
        try {
            //Process token translations
            if (!viewLabelsMap.isEmpty()) {
                for (Map.Entry<String, String> entry : viewLabelsMap.entrySet()) {
                    String uniqueIdentifier = entry.getKey();
                    View view = parentView.findViewById(Utils.getLayoutIdentifierResourceId(getActivity(), uniqueIdentifier));
                    if (view instanceof TextView) {
                        TextView textView = (TextView) view;
                        if (textView != null) {
                            String translated = getTranslatedToken(entry.getKey(), textView.getText().toString());
                            textView.setText(translated);

                        }
                    } else {
                        Log.w(TAG, " IDentifier for Language Token '" + uniqueIdentifier + "' clashes with a non TextView");
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private String getTranslatedToken(String token, String defaultReturn) {
        if (languageTranslations != null && !languageTranslations.isEmpty() && languageTranslations.containsKey(token)) {
            return languageTranslations.get(token);
        } else return defaultReturn;
    }

    @Override
    protected void onCreation() {
        //Overrides
    }

    @Override
    protected void onResumption() {
        //Overrides
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshView(EnketoFormSaveCompleteEvent enketoFormSaveCompleteEvent) {
        //Refres on form save

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshView(SyncEvent syncEvent) {
        if (syncEvent != null && syncEvent.getFetchStatus().equals(FetchStatus.fetched)) {
            processViewConfigurations(getView());
        }
    }

    private void initializeRegister(Intent intent, String registerTitle) {
        intent.putExtra(TOOLBAR_TITLE, registerTitle);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        try {

            startFormActivity(Constants.JSON_FORM.PATIENT_REMOVAL, patientDetails.get(DBConstants.KEY.BASE_ENTITY_ID), null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }

    @Override
    public void startFormActivity(String formName, String entityId, String metaData) {
        try {
            String locationId = JsonFormUtils.getOpenMrsLocationId(context(), facilitySelection.getSelectedItem());
            JsonFormUtils.startForm(getActivity(), context(), REQUEST_CODE_GET_JSON, formName, entityId,
                    metaData, locationId);

        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

    }

    protected void setupViews(View rootView) {

        //Load Language Token Map
        ViewConfiguration config = ConfigurableViewsLibrary.getJsonSpecHelper().getLanguage(Utils.getLanguage());
        languageTranslations = config == null ? null : config.getLabels();


        facilitySelection = (LocationPickerView) rootView.findViewById(R.id.facility_selection);
        facilitySelection.init(context());

        setUpButtons(rootView);
    }

    private void setUpButtons(View rootView) {

        if (patientDetails != null) {

            Button removePatientButton = (Button) rootView.findViewById(R.id.remove_patient);
            if (removePatientButton != null) {
                removePatientButton.setTag(R.id.CLIENT_ID, patientDetails.get(Constants.KEY._ID));
                removePatientButton.setOnClickListener(this);
            }

            Button followUpButton = (Button) rootView.findViewById(R.id.follow_up_button);
            if (followUpButton != null) {
                followUpButton.setTag(R.id.CLIENT_ID, patientDetails.get(Constants.KEY._ID));
                followUpButton.setOnClickListener(this);
            }

            TextView addContactView = (TextView) rootView.findViewById(R.id.add_contact);
            if (addContactView != null) {
                addContactView.setTag(R.id.CLIENT_ID, patientDetails.get(Constants.KEY._ID));
                addContactView.setOnClickListener(this);
            }
        }
    }

    protected int getCardViewIdentifierByConfiguration(String viewConfigurationIdentifier) {

        int res = 0;
        switch (viewConfigurationIdentifier) {
            case Constants.CONFIGURATION.COMPONENTS.PATIENT_DETAILS_DEMOGRAPHICS:
                res = R.id.clientDetailsCardView;
                break;
            case Constants.CONFIGURATION.COMPONENTS.PATIENT_DETAILS_CONTACT_SCREENING:
                res = R.id.clientContactScreeningCardView;
                break;
            case Constants.CONFIGURATION.COMPONENTS.PATIENT_DETAILS_FOLLOWUP:
                res = R.id.clientFollowupCardView;
                break;

            default:
                break;
        }
        return res;
    }

    protected void processViewConfigurations(View rootView) {
        try {
            String jsonString = ConfigurableViewsLibrary.getInstance().getConfigurableViewsRepository().getConfigurableViewJson(getViewConfigurationIdentifier());
            if (jsonString == null) {
                renderDefaultLayout(rootView);

            } else {
                ViewConfiguration detailsView = ConfigurableViewsLibrary.getJsonSpecHelper().getConfigurableView(jsonString);
                List<org.smartregister.configurableviews.model.View> views = detailsView.getViews();
                if (!views.isEmpty() && false) {//To Do remove
                    Collections.sort(views, new Comparator<org.smartregister.configurableviews.model.View>() {
                        @Override
                        public int compare(org.smartregister.configurableviews.model.View registerA, org.smartregister.configurableviews.model.View registerB) {
                            return registerA.getResidence().getPosition() - registerB.getResidence().getPosition();
                        }
                    });

                    LinearLayout viewParent = (LinearLayout) rootView.findViewById(getContainerViewId());
                    for (org.smartregister.configurableviews.model.View componentView : views) {

                        try {
                            if (componentView.getResidence().getParent() == null) {
                                componentView.getResidence().setParent(detailsView.getIdentifier());
                            }

                            String jsonComponentString = ConfigurableViewsLibrary.getInstance().getConfigurableViewsRepository().getConfigurableViewJson(componentView.getIdentifier());
                            ViewConfiguration componentViewConfiguration = ConfigurableViewsLibrary.getJsonSpecHelper().getConfigurableView(jsonComponentString);
                            if (componentViewConfiguration != null) {

                                ConfigurableViewsHelper configurableViewsHelper = ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper();

                                View fallbackView = viewParent.findViewById(getCardViewIdentifierByConfiguration(componentViewConfiguration.getIdentifier()));
                                if (fallbackView != null) {
                                    viewParent.removeView(fallbackView);
                                }

                                View json2View = ConfigurableViewsLibrary.getJsonSpecHelper().isEnableJsonViews() ? configurableViewsHelper.inflateDynamicView(componentViewConfiguration, viewParent, fallbackView, componentView.isVisible()) : fallbackView;
                                if (componentView.isVisible()) {
                                    json2View.setTag(R.id.VIEW_CONFIGURATION_ID, getViewConfigurationIdentifier());

                                    if (!ConfigurableViewsLibrary.getJsonSpecHelper().isEnableJsonViews()) {
                                        viewParent.addView(json2View);
                                    }
                                    json2View.setTag(commonPersonObjectClient);
                                    renderViewConfigurationCore(componentViewConfiguration, json2View, patientDetails);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                } else {
                    renderDefaultLayout(rootView);
                }

                if (detailsView != null) {
                    processLanguageTokens(detailsView.getLabels(), rootView);
                }
            }
        } catch (Exception e) {

            Log.e(TAG, e.getMessage());
        }

    }

    private void renderViewConfigurationCore(ViewConfiguration componentViewConfiguration, View json2View, Map<String, String> patientDetails) {
        if (componentViewConfiguration.getIdentifier().equals(Constants.CONFIGURATION.COMPONENTS.PATIENT_DETAILS_DEMOGRAPHICS)) {

            renderDemographicsView(json2View, patientDetails);

        } else if (componentViewConfiguration.getIdentifier().equals(Constants.CONFIGURATION.COMPONENTS.PATIENT_DETAILS_FOLLOWUP)) {

            renderFollowUpView(json2View, patientDetails);
            Button followUpButton = (Button) json2View.findViewById(R.id.follow_up_button);
            followUpButton.setTag(R.id.CLIENT_ID, patientDetails.get(Constants.KEY._ID));
        } else if (componentViewConfiguration.getIdentifier().equals(Constants.CONFIGURATION.COMPONENTS.PATIENT_DETAILS_CONTACT_SCREENING)) {
            renderContactView(json2View, patientDetails);

            TextView addContactView = (TextView) json2View.findViewById(R.id.add_contact);
            addContactView.setTag(R.id.CLIENT_ID, patientDetails.get(Constants.KEY._ID));
            addContactView.setOnClickListener(this);

        }
    }

    protected void renderDefaultLayout(View rootView) {

        rootView.setTag(commonPersonObjectClient);
        renderDemographicsView(rootView, patientDetails);
        renderFollowUpView(rootView, patientDetails);
        renderContactView(rootView, patientDetails);

    }

    private int getContainerViewId() {
        return R.id.content_patient_detail_container;

    }

}
