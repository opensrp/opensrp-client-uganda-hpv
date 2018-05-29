package org.smartregister.ug.hpv;

import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.smartregister.ug.hpv.application.TestHpvApplication;

/**
 * Created by ndegwamartin on 12/03/2018.
 */


@RunWith(RobolectricTestRunner.class)
@Config(application = TestHpvApplication.class, constants = BuildConfig.class, sdk = 21)
public abstract class BaseUnitTest {

}
