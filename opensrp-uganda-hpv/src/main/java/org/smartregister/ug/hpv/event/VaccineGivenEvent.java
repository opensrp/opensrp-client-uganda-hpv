package org.smartregister.ug.hpv.event;

/**
 * Created by ndegwamartin on 15/05/2018.
 */

public class VaccineGivenEvent extends BaseEvent {
    private String vaccine;

    public String getVaccine() {
        return vaccine;
    }

    public void setVaccine(String vaccine) {
        this.vaccine = vaccine;
    }
}
