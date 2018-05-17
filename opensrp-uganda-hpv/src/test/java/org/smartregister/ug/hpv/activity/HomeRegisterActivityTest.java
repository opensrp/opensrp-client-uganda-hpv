package org.smartregister.ug.hpv.activity;

import android.support.v4.app.Fragment;
import android.view.Menu;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.smartregister.ug.hpv.BaseUnitTest;
import org.smartregister.ug.hpv.fragment.HomeRegisterFragment;

/**
 * Created by ndegwamartin on 17/05/2018.
 */

public class HomeRegisterActivityTest extends BaseUnitTest {
    private HomeRegisterActivity activity = new HomeRegisterActivity();
    private ActivityController<HomeRegisterActivity> controller;

    @Mock
    Menu menu;

    @Before
    public void setUp() {
        controller = Robolectric.buildActivity(HomeRegisterActivity.class).create().start();
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
        Fragment fragment = activity.getRegisterFragment();
        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof HomeRegisterFragment);
    }

}
