package org.smartregister.ug.hpv;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.smartregister.ug.hpv.fragment.HomeRegisterFragment;
import org.smartregister.ug.hpv.mocks.CaretakerUtilsShadow;
import org.smartregister.ug.hpv.mocks.CommonRepositoryShadow;
import org.smartregister.ug.hpv.mocks.HomeRegisterActivityTestVersion;
import org.smartregister.ug.hpv.mocks.HpvApplicationTestVersion;
import org.smartregister.ug.hpv.util.Constants;

import java.util.List;

import static org.powermock.api.mockito.PowerMockito.spy;

@Config(shadows = {CaretakerUtilsShadow.class, CommonRepositoryShadow.class}, application = HpvApplicationTestVersion.class)
@Ignore
public class HomeRegisterActivityTestVersionUnitTest extends BaseUnitTest {
    private ActivityController<HomeRegisterActivityTestVersion> controller;
    private HomeRegisterActivityTestVersion activity;

    @Before
    public void setUp() {
        org.mockito.MockitoAnnotations.initMocks(this);
        Intent intent = new Intent(RuntimeEnvironment.application, HomeRegisterActivityTestVersion.class);
        controller = Robolectric.buildActivity(HomeRegisterActivityTestVersion.class, intent);
        activity = controller.get();
        controller.setup();
    }


    private void destroyController() {
        try {
            activity.finish();
            controller.pause().stop().destroy(); //destroy controller if we can

        } catch (Exception e) {
            Log.e(getClass().getCanonicalName(), e.getMessage());
        }
    }

    @After
    public void tearDown() {
        destroyController();
    }

    @Test
    public void getViewIdentifierReturnsCorrectValue() throws Exception {


        HomeRegisterActivityTestVersion spyActivity = spy(activity);
        junit.framework.Assert.assertNotNull(spyActivity);


        List viewIdentifiers = spyActivity.getViewIdentifiers();
        Assert.assertEquals(0, viewIdentifiers.size());
        Assert.assertEquals(Constants.VIEW_CONFIGS.HOME_REGISTER, viewIdentifiers.size());
    }


    @Test
    public void getRegisterFragmentReturnsCorrectFragment() throws Exception {

        HomeRegisterActivityTestVersion spyActivity = spy(activity);
        junit.framework.Assert.assertNotNull(spyActivity);

        Fragment fragment = spyActivity.getRegisterFragment();
        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof HomeRegisterFragment);
    }
}