package org.smartregister.ug.hpv.mocks;

import android.database.Cursor;

import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.smartregister.commonregistry.CommonRepository;

/**
 * Created by ndegwamartin on 05/04/2018.
 */

@Implements(CommonRepository.class)
public class CommonRepositoryShadow extends Shadow {

    public Cursor rawCustomQueryForAdapter(String query) {
        return null;
    }
}
