package org.smartregister.ug.hpv.helper.view;

import android.content.Context;
import android.view.View;

import org.smartregister.commonregistry.CommonPersonObjectClient;

/**
 * Created by ndegwamartin on 09/04/2018.
 */

public abstract class BaseRenderHelper {
    protected Context context;
    protected CommonPersonObjectClient commonPersonObjectClient;

    protected BaseRenderHelper(Context context, CommonPersonObjectClient commonPersonObjectClient) {
        this.context = context;
        this.commonPersonObjectClient = commonPersonObjectClient;
    }

    public abstract void renderView(View containerView);
}
