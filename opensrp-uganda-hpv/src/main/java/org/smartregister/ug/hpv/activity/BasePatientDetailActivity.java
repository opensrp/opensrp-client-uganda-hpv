package org.smartregister.ug.hpv.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;

import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.adapter.HPVRegisterActivityPagerAdapter;
import org.smartregister.ug.hpv.fragment.BasePatientDetailsFragment;
import org.smartregister.view.viewpager.OpenSRPViewPager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by ndegwamartin on 17/11/2017.
 */

public abstract class BasePatientDetailActivity extends BaseActivity {
    private static final String TAG = BasePatientDetailActivity.class.getCanonicalName();
    protected Map<String, String> patientDedtails;
    protected File currentfile;
    private static final int REQUEST_TAKE_PHOTO = 1;

    @Bind(R.id.view_pager)
    protected OpenSRPViewPager mPager;
    private Fragment mBaseFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        ButterKnife.bind(this);

        Fragment[] otherFragments = {};

        mBaseFragment = getDetailFragment();
        mBaseFragment.setArguments(this.getIntent().getExtras());

        // Instantiate a ViewPager and a PagerAdapter.
        HPVRegisterActivityPagerAdapter mPagerAdapter = new HPVRegisterActivityPagerAdapter(getSupportFragmentManager(), mBaseFragment, otherFragments);
        mPager.setOffscreenPageLimit(otherFragments.length);
        mPager.setAdapter(mPagerAdapter);

    }

    protected abstract Fragment getDetailFragment();

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

}
