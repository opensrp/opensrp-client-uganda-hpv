package org.smartregister.ug.hpv.activity;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.smartregister.ug.hpv.BaseUnitTest;

import static org.mockito.Mockito.times;

/**
 * Created by ndegwamartin on 18/05/2018.
 */

public class HpvJsonFormActivityTest extends BaseUnitTest {

    private HpvJsonFormActivity activity;

    @Mock
    private String stepName;
    @Mock
    private String key;
    @Mock
    private String value;
    @Mock
    private String openMrsEntityParent;
    @Mock
    private String openMrsEntity;
    @Mock
    private String openMrsEntityId;

    @Before
    public void setUp() {
        activity = Mockito.mock(HpvJsonFormActivity.class);
    }

    @After
    public void tearDown() {
        destroyController();
    }

    private void destroyController() {
        System.gc();
    }

    @Test
    public void assertActivityStartsUpCorrectly() throws Exception {
        Assert.assertNotNull(activity);
    }


    @Test
    public void assertCallingOnFinishInvokesSuper() throws Exception {

        HpvJsonFormActivity activity = Mockito.spy(HpvJsonFormActivity.class);
        Mockito.doNothing().when(activity).callSuperFinish();

        activity.onFormFinish();
        Mockito.verify(activity, times(1)).callSuperFinish();
    }


    @Test
    public void assertCallingWriteValueInvokesSuperWithCorrectParams() throws Exception {

        HpvJsonFormActivity activity = Mockito.spy(HpvJsonFormActivity.class);
        Mockito.doNothing().when(activity).callSuperWriteValue(stepName, key, value, openMrsEntityParent, openMrsEntity, openMrsEntityId);

        activity.writeValue(stepName, key, value, openMrsEntityParent, openMrsEntity, openMrsEntityId);
        Mockito.verify(activity, times(1)).callSuperWriteValue(stepName, key, value, openMrsEntityParent, openMrsEntity, openMrsEntityId);
    }

    @Test
    public void initializeFormFragmentCallsCorrectMethod() throws Exception {

        HpvJsonFormActivity activity = Mockito.spy(HpvJsonFormActivity.class);
        Mockito.doNothing().when(activity).initializeFormFragmentCore();

        activity.initializeFormFragment();
        Mockito.verify(activity, times(1)).initializeFormFragmentCore();
    }
}