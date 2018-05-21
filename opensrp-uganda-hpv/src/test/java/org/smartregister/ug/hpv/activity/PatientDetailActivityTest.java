package org.smartregister.ug.hpv.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.Menu;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.ug.hpv.BaseUnitTest;
import org.smartregister.ug.hpv.fragment.PatientDetailsFragment;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

/**
 * Created by ndegwamartin on 17/05/2018.
 */

public class PatientDetailActivityTest extends BaseUnitTest {
    private PatientDetailActivity activity;
    private ActivityController<PatientDetailActivity> controller;

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_CODE_GET_JSON = 3432;
    private static final int NON_EXISTENT_CODE = 10101;

    @Mock
    private Menu menu;

    @Mock
    private Intent intent;

    @Before
    public void setUp() {
        controller = Robolectric.buildActivity(PatientDetailActivity.class).create().start();
        activity = controller.get();
    }

    @After
    public void tearDown() {
        destroyController();
    }

    private void destroyController() {
        try {
            activity.finish();
            controller.pause().stop().destroy(); //destroy controller if we can

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.gc();
    }

    @Test
    public void assertActivityStartsUpCorrectly() throws Exception {
        Assert.assertNotNull(activity);
        Assert.assertTrue(activity.onCreateOptionsMenu(menu));
    }

    @Test
    public void getDetailsFragmentCreatesAndReturnsCorrectFragment() throws Exception {
        Fragment fragment = activity.getDetailFragment();
        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof PatientDetailsFragment);
    }

    @Test
    public void onActivityResultsInvokesCorrectMethodOnFormSave() throws Exception {

        PatientDetailActivity activity_ = Mockito.spy(activity);
        Assert.assertNotNull(activity_);

        Mockito.doNothing().when(activity_).processFormDetailsSave(Mockito.eq(intent), Mockito.any(AllSharedPreferences.class));

        activity_.onActivityResult(REQUEST_CODE_GET_JSON, Activity.RESULT_OK, intent);

        Mockito.verify(activity_, times(1)).processFormDetailsSave(Mockito.eq(intent), Mockito.any(AllSharedPreferences.class));

        activity_ = Mockito.spy(activity);
        Assert.assertNotNull(activity_);
        //Use take photo request code
        activity_ = Mockito.spy(activity);
        Assert.assertNotNull(activity_);

        activity_.onActivityResult(REQUEST_TAKE_PHOTO, Activity.RESULT_OK, intent);

        Mockito.verify(activity_, never()).processFormDetailsSave(Mockito.eq(intent), Mockito.any(AllSharedPreferences.class));

        //Use non existent request code
        activity_ = Mockito.spy(activity);
        Assert.assertNotNull(activity_);
        activity_.onActivityResult(NON_EXISTENT_CODE, Activity.RESULT_OK, intent);

        Mockito.verify(activity_, never()).processFormDetailsSave(Mockito.eq(intent), Mockito.any(AllSharedPreferences.class));


    }

    @Test
    public void onActivityResultsInvokesCorrectMethodOnTakePhoto() throws Exception {

        PatientDetailActivity activity_ = Mockito.spy(activity);
        Assert.assertNotNull(activity_);

        Mockito.doNothing().when(activity_).processFormDetailsSave(Mockito.eq(intent), Mockito.any(AllSharedPreferences.class));

        activity_.onActivityResult(REQUEST_TAKE_PHOTO, Activity.RESULT_OK, intent);

        Mockito.verify(activity_, times(1)).processPhotoUpload(Mockito.any(AllSharedPreferences.class));

        //Use take photo request code

        activity_ = Mockito.spy(activity);
        Assert.assertNotNull(activity_);
        activity_.onActivityResult(REQUEST_CODE_GET_JSON, Activity.RESULT_OK, intent);

        Mockito.verify(activity_, never()).processPhotoUpload(Mockito.any(AllSharedPreferences.class));


        //Use non existent request code
        activity_ = Mockito.spy(activity);
        Assert.assertNotNull(activity_);
        activity_.onActivityResult(NON_EXISTENT_CODE, Activity.RESULT_OK, intent);

        Mockito.verify(activity_, never()).processPhotoUpload(Mockito.any(AllSharedPreferences.class));


    }

}
