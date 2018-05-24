package org.smartregister.ug.hpv.view;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.smartregister.ug.hpv.BaseUnitTest;

/**
 * Created by ndegwamartin on 24/05/2018.
 */

public class CopyToClipboardDialogTest extends BaseUnitTest {

    @Before
    public void setUp() {

    }

    @Test
    public void callingConstructorInstantiatesDialogCorrectly() {
        CopyToClipboardDialog copyToClipboardDialog = new CopyToClipboardDialog(RuntimeEnvironment.application);
        Assert.assertNotNull(copyToClipboardDialog);

        copyToClipboardDialog = new CopyToClipboardDialog(RuntimeEnvironment.application, 0);
        Assert.assertNotNull(copyToClipboardDialog);
    }
}
