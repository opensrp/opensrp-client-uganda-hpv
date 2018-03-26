package org.smartregister.ug.hpv.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.domain.FetchStatus;
import org.smartregister.provider.SmartRegisterClientsProvider;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.adapter.HPVRegisterActivityPagerAdapter;
import org.smartregister.ug.hpv.application.HpvApplication;
import org.smartregister.ug.hpv.barcode.Barcode;
import org.smartregister.ug.hpv.barcode.BarcodeIntentIntegrator;
import org.smartregister.ug.hpv.event.ShowProgressDialogEvent;
import org.smartregister.ug.hpv.event.SyncEvent;
import org.smartregister.ug.hpv.fragment.BaseRegisterFragment;
import org.smartregister.ug.hpv.fragment.HomeRegisterFragment;
import org.smartregister.ug.hpv.util.Constants;
import org.smartregister.ug.hpv.util.JsonFormUtils;
import org.smartregister.ug.hpv.util.Utils;
import org.smartregister.ug.hpv.view.LocationPickerView;
import org.smartregister.view.activity.SecuredNativeSmartRegisterActivity;
import org.smartregister.view.viewpager.OpenSRPViewPager;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Created by ndegwamartin on 14/03/2018.
 */

public abstract class BaseRegisterActivity extends SecuredNativeSmartRegisterActivity {

    public static final String TAG = BaseRegisterActivity.class.getCanonicalName();

    public static String TOOLBAR_TITLE = "org.smartregister.ug.hpv.activity.toolbarTitle";

    private ProgressDialog progressDialog;
    private final int MINIUM_LANG_COUNT = 2;
    private static final String CHILD = "child";


    @Bind(R.id.view_pager)
    protected OpenSRPViewPager mPager;
    private FragmentPagerAdapter mPagerAdapter;
    private static final int REQUEST_CODE_GET_JSON = 3432;
    private int currentPage;
    public static final int ADVANCED_SEARCH_POSITION = 1;
    public static final String EXTRA_CHILD_DETAILS = "child_details";
    private CommonPersonObjectClient childDetails;

    private String location_name = "";

    private android.support.v4.app.Fragment mBaseFragment = null;
    ////////////////////////////////////////////////
    public DetailsRepository detailsRepository;
    private Map<String, String> details;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private File currentfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_register);
        ButterKnife.bind(this);

        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            Serializable serializable = extras.getSerializable(EXTRA_CHILD_DETAILS);
            if (serializable != null && serializable instanceof CommonPersonObjectClient) {
                childDetails = (CommonPersonObjectClient) serializable;
            }
        }


        location_name = extras.getString("location_name");
        detailsRepository = detailsRepository == null ? HpvApplication.getInstance().getContext().detailsRepository() : detailsRepository;
        if (childDetails != null) {
            details = detailsRepository.getAllDetailsForClient(childDetails.entityId());
            Utils.putAll(details, childDetails.getColumnmaps());
        }


        Fragment[] otherFragments = {};

        mBaseFragment = getRegisterFragment();
        mBaseFragment.setArguments(this.getIntent().getExtras());

        // Instantiate a ViewPager and a PagerAdapter.
        mPagerAdapter = new HPVRegisterActivityPagerAdapter(getSupportFragmentManager(), mBaseFragment, otherFragments);
        mPager.setOffscreenPageLimit(otherFragments.length);
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                currentPage = position;
            }
        });

    }

    protected abstract Fragment getRegisterFragment();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_register, menu);
        if (HpvApplication.getJsonSpecHelper().getAvailableLanguages().size() < MINIUM_LANG_COUNT) {
            invalidateOptionsMenu();
            MenuItem item = menu.findItem(R.id.action_language);
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_language) {
            this.showLanguageDialog();
            HpvApplication.getInstance().startPullUniqueIdsService();
            return true;
        } else if (id == R.id.action_logout) {
            logOutUser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void logOutUser() {
        Utils.showToast(this, "Logging out user");
    }

    public void showLanguageDialog() {


        final List<String> displayValues = ConfigurableViewsLibrary.getJsonSpecHelper().getAvailableLanguages();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, displayValues.toArray(new String[displayValues.size()])) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(ConfigurableViewsLibrary.getInstance().getContext().getColorResource(org.smartregister.ug.hpv.R.color.customAppThemeBlue));

                return view;
            }
        };
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.select_language));
        builder.setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedItem = displayValues.get(which);
                Map<String, String> langs = HpvApplication.getJsonSpecHelper().getAvailableLanguagesMap();
                Utils.saveLanguage(Utils.getKeyByValue(langs, selectedItem));
                // Utils.postEvent(new LanguageConfigurationEvent(false));
                Utils.showToast(getApplicationContext(), selectedItem + " selected");
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected DefaultOptionsProvider getDefaultOptionsProvider() {
        return null;
    }

    @Override
    protected NavBarOptionsProvider getNavBarOptionsProvider() {
        return null;
    }

    @Override
    protected SmartRegisterClientsProvider clientsProvider() {
        return null;
    }

    @Override
    protected void setupViews() {//Implement Abstract Method
    }

    @Override
    protected void onResumption() {
        ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper().registerViewConfigurations(getViewIdentifiers());
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onInitialization() {//Implement Abstract Method
    }

    @Override
    public void startRegistration() {//Implement Abstract Method
    }

    public void refreshList(final FetchStatus fetchStatus) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            BaseRegisterFragment registerFragment = (BaseRegisterFragment) findFragmentByPosition(0);
            if (registerFragment != null && fetchStatus.equals(FetchStatus.fetched)) {
                registerFragment.refreshListView();
            }
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    BaseRegisterFragment registerFragment = (BaseRegisterFragment) findFragmentByPosition(0);
                    if (registerFragment != null && fetchStatus.equals(FetchStatus.fetched)) {
                        registerFragment.refreshListView();
                    }
                }
            });
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showProgressDialog(ShowProgressDialogEvent showProgressDialogEvent) {
        if (showProgressDialogEvent != null)
            showProgressDialog();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshList(SyncEvent syncEvent) {
        if (syncEvent != null && syncEvent.getFetchStatus().equals(FetchStatus.fetched))
            refreshList(FetchStatus.fetched);
    }

    public void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getString(R.string.saving_dialog_title));
        progressDialog.setMessage(getString(R.string.please_wait_message));
        if (!isFinishing())
            progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }


    public android.support.v4.app.Fragment findFragmentByPosition(int position) {
        FragmentPagerAdapter fragmentPagerAdapter = mPagerAdapter;
        return getSupportFragmentManager().findFragmentByTag("android:switcher:" + mPager.getId() + ":" + fragmentPagerAdapter.getItemId(position));
    }


    @Override
    protected void onStop() {
        super.onStop();
        ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper().unregisterViewConfiguration(getViewIdentifiers());
    }

    public abstract List<String> getViewIdentifiers();


    public void startQrCodeScanner() {
        BarcodeIntentIntegrator integ = new BarcodeIntentIntegrator(this);
        integ.addExtra(Barcode.SCAN_MODE, Barcode.QR_MODE);
        integ.initiateScan();
    }

    @Override
    public void startFormActivity(String formName, String entityId, String metaData) {
        try {
            if (mBaseFragment instanceof HomeRegisterFragment) {
                LocationPickerView locationPickerView = ((HomeRegisterFragment) mBaseFragment).getLocationPickerView();
                String locationId = JsonFormUtils.getOpenMrsLocationId(context(), locationPickerView.getSelectedItem());
                JsonFormUtils.startForm(this, context(), REQUEST_CODE_GET_JSON, formName, entityId,
                        metaData, locationId);
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra("json");
                Log.d("JSONResult", jsonString);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);

                JSONObject form = new JSONObject(jsonString);
                if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.Remove)) {

                } else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Constants.EventType.REGISTRATION)) {
                    JsonFormUtils.editsave(this, HpvApplication.getInstance().getContext(), jsonString, allSharedPreferences.fetchRegisteredANM(), "photo", CHILD);
                }
                //childDataFragment.childDetails = childDetails;
                //childDataFragment.loadData();
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }

        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            String imageLocation = currentfile.getAbsolutePath();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);

            JsonFormUtils.saveImage(this, allSharedPreferences.fetchRegisteredANM(), childDetails.entityId(), imageLocation);
            //updateProfilePicture(gender);
        }
    }

}
