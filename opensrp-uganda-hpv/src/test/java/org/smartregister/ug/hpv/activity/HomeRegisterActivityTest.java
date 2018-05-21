package org.smartregister.ug.hpv.activity;

import android.support.v4.app.Fragment;
import android.view.MenuItem;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.smartregister.ug.hpv.BaseUnitTest;
import org.smartregister.ug.hpv.fragment.HomeRegisterFragment;
import org.smartregister.ug.hpv.util.Constants;

import java.util.List;

import static org.mockito.Mockito.times;

/**
 * Created by ndegwamartin on 17/05/2018.
 */

public class HomeRegisterActivityTest extends BaseUnitTest {
    private HomeRegisterActivity activity;
    private ActivityController<HomeRegisterActivity> controller;

    @Mock
    private MenuItem menuItem;

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
        HomeRegisterActivity activity = Mockito.spy(HomeRegisterActivity.class);
        Assert.assertNotNull(activity);

        Mockito.doReturn(true).when(activity).superOnOptionsItemsSelected(menuItem);

        activity.onOptionsItemSelected(menuItem);
        Mockito.verify(activity, times(1)).superOnOptionsItemsSelected(menuItem);

        Mockito.doReturn(false).when(activity).superOnOptionsItemsSelected(menuItem);

        boolean result = activity.onOptionsItemSelected(menuItem);
        Assert.assertFalse(result);
    }

    @Test
    public void getDetailsFragmentCreatesAndReturnsCorrectFragment() throws Exception {
        Fragment fragment = activity.getRegisterFragment();
        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof HomeRegisterFragment);
    }

    @Test
    public void getViewIdentifierReturnsTheCorrectItemsList() {
        List<String> identifiers = activity.getViewIdentifiers();
        Assert.assertEquals(identifiers.size(), 1);
        Assert.assertEquals(identifiers.get(0), Constants.VIEW_CONFIGS.HOME_REGISTER);
    }

}
