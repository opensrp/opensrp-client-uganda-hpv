package org.smartregister.ug.hpv.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.listener.VaccinationActionListener;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.view.VaccineGroup;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.adapter.HPVRegisterActivityPagerAdapter;
import org.smartregister.ug.hpv.application.HpvApplication;
import org.smartregister.ug.hpv.event.VaccineUpdatedEvent;
import org.smartregister.ug.hpv.fragment.BasePatientDetailsFragment;
import org.smartregister.ug.hpv.fragment.PatientDetailsFragment;
import org.smartregister.ug.hpv.helper.view.RenderContactCardHelper;
import org.smartregister.ug.hpv.util.Constants;
import org.smartregister.ug.hpv.view.LocationPickerView;
import org.smartregister.util.PermissionUtils;
import org.smartregister.view.viewpager.OpenSRPViewPager;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

import static org.smartregister.ug.hpv.util.Utils.addVaccine;
import static org.smartregister.ug.hpv.util.Utils.postStickyEvent;
import static org.smartregister.ug.hpv.util.Utils.updateEcPatient;
import static org.smartregister.ug.hpv.util.Utils.updateVaccineTable;
import static org.smartregister.util.Utils.startAsyncTask;

/**
 * Created by ndegwamartin on 17/11/2017.
 */

public abstract class BasePatientDetailActivity extends BaseActivity implements VaccinationActionListener {
    private static final String TAG = BasePatientDetailActivity.class.getCanonicalName();
    protected File currentfile;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private ArrayList<VaccineGroup> vaccineGroups;

    @Bind(R.id.view_pager)
    protected OpenSRPViewPager mPager;

    private CommonPersonObjectClient commonPersonObjectClient;
    private Fragment mBaseFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        ButterKnife.bind(this);

        commonPersonObjectClient = (CommonPersonObjectClient) getIntent().getSerializableExtra(Constants.INTENT_KEY.CLIENT_OBJECT);

        vaccineGroups = new ArrayList<>();

        Fragment[] otherFragments = {};

        mBaseFragment = getDetailFragment();
        mBaseFragment.setArguments(this.getIntent().getExtras());

        // Instantiate a ViewPager and a PagerAdapter.
        HPVRegisterActivityPagerAdapter mPagerAdapter = new HPVRegisterActivityPagerAdapter(getSupportFragmentManager(), mBaseFragment, otherFragments);
        mPager.setOffscreenPageLimit(otherFragments.length);
        mPager.setAdapter(mPagerAdapter);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Serializable serializable = savedInstanceState.getSerializable(Constants.INTENT_KEY.CLIENT_OBJECT);
        if (serializable != null && serializable instanceof CommonPersonObjectClient) {
            commonPersonObjectClient = (CommonPersonObjectClient) serializable;
        }
    }

    protected abstract Fragment getDetailFragment();

    @Override
    protected void onResume() {
        super.onResume();
        if (vaccineGroups != null) {
            // TODO: might need to add some logic here (similar to Zeir)
            vaccineGroups = null;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    @Override
    public void onBackPressed() {

        super.onBackPressed(); // allow back key only if we are
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d(TAG, "Permission callback called-------");

        if (grantResults.length == 0) {
            return;
        }

        switch (requestCode) {
            case PermissionUtils.CAMERA_PERMISSION_REQUEST_CODE:
                if (PermissionUtils.verifyPermissionGranted(permissions, grantResults, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    dispatchTakePictureIntent();
                }
                break;
            case PermissionUtils.PHONE_STATE_PERMISSION_REQUEST_CODE:
                if (PermissionUtils.verifyPermissionGranted(permissions, grantResults, Manifest.permission.READ_PHONE_STATE)) {
                    RenderContactCardHelper.launchPhoneDialer(this, RenderContactCardHelper.phoneNumber);
                }
                break;
            default:
                break;

        }
    }


    public void dispatchTakePictureIntent() {
        if (PermissionUtils.isPermissionGranted(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionUtils.CAMERA_PERMISSION_REQUEST_CODE)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.e(TAG, Log.getStackTraceString(ex));
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {

                    //We need this for backward compatibility
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                        StrictMode.setVmPolicy(builder.build());
                    }

                    currentfile = photoFile;
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        storageDir.mkdirs();
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
    }


    @Override
    public void startFormActivity(String formName, String entityId, String metaData) {
        try {
            ((BasePatientDetailsFragment) mBaseFragment).startFormActivity(formName, entityId, metaData);

        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

    }


    @Override
    public void onVaccinateToday(ArrayList<VaccineWrapper> tags, View v) {
        if (tags != null && !tags.isEmpty()) {
            View view = getLastOpenedView();
            saveVaccine(tags, view);
        }
    }

    @Override
    public void onVaccinateEarlier(ArrayList<VaccineWrapper> tags, View v) {
        if (tags != null && !tags.isEmpty()) {
            View view = getLastOpenedView();
            saveVaccine(tags, view);
        }
    }

    @Override
    public void onUndoVaccination(VaccineWrapper tag, View v) {
        startAsyncTask(new UndoVaccineTask(tag), null);
    }

    private void saveVaccine(ArrayList<VaccineWrapper> vaccineWrappers, final View view) {


        if (vaccineWrappers.isEmpty()) {
            return;
        }

        VaccineRepository vaccineRepository = HpvApplication.getInstance().vaccineRepository();

        VaccineWrapper[] arrayTags = vaccineWrappers.toArray(new VaccineWrapper[vaccineWrappers.size()]);
        SaveVaccinesTask backgroundTask = new SaveVaccinesTask();
        backgroundTask.setVaccineRepository(vaccineRepository);
        backgroundTask.setView(view);
        startAsyncTask(backgroundTask, arrayTags);
    }


    private void saveVaccine(VaccineRepository vaccineRepository, VaccineWrapper vaccineWrapper) {
        if (vaccineWrapper.getUpdatedVaccineDate() == null) {
            return;
        }

        Vaccine vaccine = new Vaccine();
        if (vaccineWrapper.getDbKey() != null) {
            vaccine = vaccineRepository.find(vaccineWrapper.getDbKey());
        }
        vaccine.setBaseEntityId(commonPersonObjectClient.entityId());
        vaccine.setName(vaccineWrapper.getName());
        vaccine.setDate(vaccineWrapper.getUpdatedVaccineDate().toDate());
        vaccine.setAnmId(getOpenSRPContext().allSharedPreferences().fetchRegisteredANM());

        LocationPickerView locationPickerView = ((PatientDetailsFragment) mBaseFragment).getLocationPickerView();

        LocationHelper.getInstance().setParentAndChildLocationIds(locationPickerView.getSelectedItem());
        vaccine.setLocationId(LocationHelper.getInstance().getParentLocationId());
        vaccine.setChildLocationId(LocationHelper.getInstance().getChildLocationId());

        AllSharedPreferences sharedPreferences = getOpenSRPContext().allSharedPreferences();
        vaccine.setTeam(sharedPreferences.fetchDefaultTeam(sharedPreferences.fetchRegisteredANM()));
        vaccine.setTeamId(sharedPreferences.fetchDefaultTeamId(sharedPreferences.fetchRegisteredANM()));

        String lastChar = vaccine.getName().substring(vaccine.getName().length() - 1);
        if (StringUtils.isNumeric(lastChar)) {
            vaccine.setCalculation(Integer.valueOf(lastChar));
        } else {
            vaccine.setCalculation(-1);
        }

        addVaccine(vaccineRepository, vaccine);

        String CHILD_LOCATION_ID = "child_location_id";

        vaccineWrapper.setDbKey(vaccine.getId());

        // update childLocationId
        SQLiteDatabase db = vaccineRepository.getWritableDatabase();
        Map<String, String> contentValues = new HashMap<>();
        contentValues.put(CHILD_LOCATION_ID, vaccine.getChildLocationId());
        updateVaccineTable(db, vaccine, contentValues);

        // update patient record
        String baseEntityId = vaccine.getBaseEntityId();
        String vaccineName = vaccine.getName();
        Date vaccineDate = vaccine.getDate();
        String locationId = vaccine.getChildLocationId();

        updateEcPatient(baseEntityId, vaccineName, vaccineDate, locationId);
    }

    private void updateVaccineGroupViews(View view, final ArrayList<VaccineWrapper> wrappers, List<Vaccine> vaccineList) {
        updateVaccineGroupViews(view, wrappers, vaccineList, false);
    }

    private void updateVaccineGroupViews(View view, final ArrayList<VaccineWrapper> wrappers, final List<Vaccine> vaccineList, final boolean undo) {
        if (view == null || !(view instanceof VaccineGroup)) {
            return;
        }
        final VaccineGroup vaccineGroup = (VaccineGroup) view;
        vaccineGroup.setModalOpen(false);

        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (undo) {
                vaccineGroup.setVaccineList(vaccineList);
                vaccineGroup.updateWrapperStatus(wrappers, Constants.KEY.CHILD);
            }
            vaccineGroup.updateViews(wrappers);

        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (undo) {
                        vaccineGroup.setVaccineList(vaccineList);
                        vaccineGroup.updateWrapperStatus(wrappers, Constants.KEY.CHILD);
                    }
                    vaccineGroup.updateViews(wrappers);
                }
            });
        }
    }

    private VaccineGroup getLastOpenedView() {
        if (vaccineGroups == null) {
            return null;
        }

        for (VaccineGroup vaccineGroup : vaccineGroups) {
            if (vaccineGroup.isModalOpen()) {
                return vaccineGroup;
            }
        }
        return null;
    }


    //////////////////////////////// AsyncTasks ////////////////////////////////////

    private class SaveVaccinesTask extends AsyncTask<VaccineWrapper, Void, Pair<ArrayList<VaccineWrapper>, List<Vaccine>>> {

        private View view;
        private VaccineRepository vaccineRepository;
        private List<String> affectedVaccines;
        private List<Vaccine> vaccineList;

        public void setView(View view) {
            this.view = view;
        }

        public void setVaccineRepository(VaccineRepository vaccineRepository) {
            this.vaccineRepository = vaccineRepository;
            affectedVaccines = new ArrayList<>();
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        @Override
        protected void onPostExecute(Pair<ArrayList<VaccineWrapper>, List<Vaccine>> pair) {
            hideProgressDialog();
            updateVaccineGroupViews(view, pair.first, pair.second);
            postStickyEvent(new VaccineUpdatedEvent());
        }

        @Override
        protected Pair<ArrayList<VaccineWrapper>, List<Vaccine>> doInBackground(VaccineWrapper... vaccineWrappers) {

            ArrayList<VaccineWrapper> list = new ArrayList<>();
            if (vaccineRepository != null) {
                for (VaccineWrapper tag : vaccineWrappers) {
                    saveVaccine(vaccineRepository, tag);
                    list.add(tag);
                }
            }

            Pair<ArrayList<VaccineWrapper>, List<Vaccine>> pair = new Pair<>(list, vaccineList);
            vaccineList = vaccineRepository.findByEntityId(commonPersonObjectClient.entityId());

            return pair;
        }
    }

    private class UndoVaccineTask extends AsyncTask<Void, Void, Void> {

        private final VaccineWrapper tag;
        private final VaccineRepository vaccineRepository;

        public UndoVaccineTask(VaccineWrapper tag) {
            this.tag = tag;
            vaccineRepository = HpvApplication.getInstance().vaccineRepository();
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog(getString(R.string.updating_dialog_title), null);
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (tag != null && tag.getDbKey() != null) {
                Long dbKey = tag.getDbKey();
                vaccineRepository.deleteVaccine(dbKey);
            }

            String vaccineName = tag.getName();
            updateEcPatient(commonPersonObjectClient.entityId(), vaccineName, null, null);

            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            hideProgressDialog();
            super.onPostExecute(params);

            // Refresh the vaccine group with the updated vaccine
            tag.setUpdatedVaccineDate(null, false);
            tag.setDbKey(null);

            ArrayList<VaccineWrapper> wrappers = new ArrayList<>();
            wrappers.add(tag);

            postStickyEvent(new VaccineUpdatedEvent());
        }
    }
}
