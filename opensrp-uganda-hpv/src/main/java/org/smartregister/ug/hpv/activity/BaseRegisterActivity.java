package org.smartregister.ug.hpv.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.domain.FetchStatus;
import org.smartregister.provider.SmartRegisterClientsProvider;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.application.HpvApplication;
import org.smartregister.ug.hpv.event.ShowProgressDialogEvent;
import org.smartregister.ug.hpv.event.SyncEvent;
import org.smartregister.ug.hpv.util.Utils;
import org.smartregister.view.activity.SecuredNativeSmartRegisterActivity;

import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;


/**
 * Created by ndegwamartin on 14/03/2018.
 */

public abstract class BaseRegisterActivity extends SecuredNativeSmartRegisterActivity {

    public static final String TAG = "BaseRegisterActivity";

    public static String TOOLBAR_TITLE = "org.smartregister.ug.hpv.activity.toolbarTitle";

    private ProgressDialog progressDialog;
    private final int MINIUM_LANG_COUNT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_register);

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
        //Refresh list

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


    @Override
    protected void onStop() {
        super.onStop();
        ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper().unregisterViewConfiguration(getViewIdentifiers());
    }

    public abstract List<String> getViewIdentifiers();


}
