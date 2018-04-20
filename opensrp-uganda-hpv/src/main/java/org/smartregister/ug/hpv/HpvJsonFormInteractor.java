package org.smartregister.ug.hpv;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;

import org.smartregister.ug.hpv.widgets.HpvDatePickerFactory;
import org.smartregister.ug.hpv.widgets.HpvEditTextFactory;


/**
 * Created by ndegwamartin on 19/03/2018.
 */
public class HpvJsonFormInteractor extends JsonFormInteractor {

    private static final JsonFormInteractor INSTANCE = new HpvJsonFormInteractor();

    private HpvJsonFormInteractor() {
        super();
    }

    @Override
    protected void registerWidgets() {
        super.registerWidgets();
        map.put(JsonFormConstants.EDIT_TEXT, new HpvEditTextFactory());
        map.put(JsonFormConstants.DATE_PICKER, new HpvDatePickerFactory());
//        map.put(JsonFormConstants.LABEL, new PathCalculateLabelFactory());
    }

    public static JsonFormInteractor getInstance() {
        return INSTANCE;
    }
}
