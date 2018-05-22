package org.smartregister.ug.hpv.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.listener.VaccinationActionListener;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.view.VaccineGroup;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.adapter.HPVRegisterActivityPagerAdapter;
import org.smartregister.ug.hpv.application.HpvApplication;
import org.smartregister.ug.hpv.event.VaccineGivenEvent;
import org.smartregister.ug.hpv.fragment.BasePatientDetailsFragment;
import org.smartregister.ug.hpv.fragment.PatientDetailsFragment;
import org.smartregister.ug.hpv.helper.LocationHelper;
import org.smartregister.ug.hpv.util.Constants;
import org.smartregister.ug.hpv.view.LocationPickerView;
import org.smartregister.util.Utils;
import org.smartregister.view.viewpager.OpenSRPViewPager;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static org.smartregister.ug.hpv.util.Utils.updateEcPatient;
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

    public void dispatchTakePictureIntent() {
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
                currentfile = photoFile;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
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
        startAsyncTask(new UndoVaccineTask(tag, v), null);
    }

    private void saveVaccine(ArrayList<VaccineWrapper> tags, final View view) {
        if (tags.isEmpty()) {
            return;
        }

        VaccineRepository vaccineRepository = HpvApplication.getInstance().vaccineRepository();

        VaccineWrapper[] arrayTags = tags.toArray(new VaccineWrapper[tags.size()]);
        SaveVaccinesTask backgroundTask = new SaveVaccinesTask();
        backgroundTask.setVaccineRepository(vaccineRepository);
        backgroundTask.setView(view);
        startAsyncTask(backgroundTask, arrayTags);

    }


    private void saveVaccine(VaccineRepository vaccineRepository, VaccineWrapper tag) {
        if (tag.getUpdatedVaccineDate() == null) {
            return;
        }

        Vaccine vaccine = new Vaccine();
        if (tag.getDbKey() != null) {
            vaccine = vaccineRepository.find(tag.getDbKey());
        }
        vaccine.setBaseEntityId(commonPersonObjectClient.entityId());
        vaccine.setName(tag.getName());
        vaccine.setDate(tag.getUpdatedVaccineDate().toDate());
        vaccine.setAnmId(getOpenSRPContext().allSharedPreferences().fetchRegisteredANM());

        LocationPickerView locationPickerView = ((PatientDetailsFragment) mBaseFragment).getLocationPickerView();
        vaccine.setLocationId(LocationHelper.getInstance().getOpenMrsLocationId(locationPickerView.getSelectedItem()));

        AllSharedPreferences sharedPreferences = getOpenSRPContext().allSharedPreferences();
        vaccine.setTeam(sharedPreferences.fetchDefaultTeam(sharedPreferences.fetchRegisteredANM()));
        vaccine.setTeamId(sharedPreferences.fetchDefaultTeamId(sharedPreferences.fetchRegisteredANM()));

        String lastChar = vaccine.getName().substring(vaccine.getName().length() - 1);
        if (StringUtils.isNumeric(lastChar)) {
            vaccine.setCalculation(Integer.valueOf(lastChar));
        } else {
            vaccine.setCalculation(-1);
        }
        org.smartregister.ug.hpv.util.Utils.addVaccine(vaccineRepository, vaccine);
        tag.setDbKey(vaccine.getId());

        updateEcPatient(vaccine);

        org.smartregister.ug.hpv.util.Utils.postStickyEvent(new VaccineGivenEvent());
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


    private class UndoVaccineTask extends AsyncTask<Void, Void, Void> {
        private final VaccineWrapper tag;
        private final View view;
        private final VaccineRepository vaccineRepository;
        private List<Vaccine> vaccineList;
        private List<String> affectedVaccines;

        public UndoVaccineTask(VaccineWrapper tag, View view) {
            this.tag = tag;
            this.view = view;
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

                String dobString = Utils.getValue(commonPersonObjectClient.getColumnmaps(), Constants.DOB, false);
                DateTime dateTime = org.smartregister.ug.hpv.util.Utils.dobStringToDateTime(dobString);
                if (dateTime != null) {
                    affectedVaccines = VaccineSchedule.updateOfflineAlerts(commonPersonObjectClient.entityId(), dateTime, Constants.KEY.CHILD);
                }
                vaccineList = vaccineRepository.findByEntityId(commonPersonObjectClient.entityId());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            hideProgressDialog();
            super.onPostExecute(params);

            // Refresh the vaccine group with the updated vaccine
            tag.setUpdatedVaccineDate(null, false);
            tag.setDbKey(null);

            View view = getLastOpenedView();

            ArrayList<VaccineWrapper> wrappers = new ArrayList<>();
            wrappers.add(tag);
            updateVaccineGroupViews(view, wrappers, vaccineList, true);
        }
    }

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
}
