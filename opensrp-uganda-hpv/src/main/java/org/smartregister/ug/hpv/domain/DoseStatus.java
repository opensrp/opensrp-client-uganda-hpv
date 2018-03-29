package org.smartregister.ug.hpv.domain;

/**
 * Created by ndegwamartin on 29/03/2018.
 */

public class DoseStatus {
    private String doseOneDate;
    private String doseTwoDate;
    private boolean doseageComplete;

    public boolean isDoseageComplete() {
        return doseageComplete;
    }

    public void setDoseageComplete(boolean doseageComplete) {
        this.doseageComplete = doseageComplete;
    }

    public String getDoseOneDate() {
        return doseOneDate;
    }

    public void setDoseOneDate(String doseOneDate) {
        this.doseOneDate = doseOneDate;
    }

    public String getDoseTwoDate() {
        return doseTwoDate;
    }

    public void setDoseTwoDate(String doseTwoDate) {
        this.doseTwoDate = doseTwoDate;
    }
}
