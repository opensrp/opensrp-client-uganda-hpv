package org.smartregister.ug.hpv.service;

import org.smartregister.immunization.service.intent.VaccineIntentService;

/**
 * Created by vkaruri on 27/04/2018.
 */

public class HpvVaccineIntentService extends VaccineIntentService {

    @Override
    protected String getEventType() {
        return "HPV Vaccination";
    }
}