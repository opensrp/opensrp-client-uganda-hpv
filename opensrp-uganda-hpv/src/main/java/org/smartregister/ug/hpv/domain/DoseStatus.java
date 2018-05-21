package org.smartregister.ug.hpv.domain;

/**
 * Created by ndegwamartin on 29/03/2018.
 */

public class DoseStatus {

    private String doseOneDate;
    private String doseTwoDate;
    private String dateDoseOneGiven;
    private String dateDoseTwoGiven;
    private boolean isDoseTwoDue = false;
    private String doseOneGivenLocation;

    private String doseTwoGivenLocation;

    public boolean isDoseTwoDue() {
        return isDoseTwoDue;
    }

    public void setDoseTwoDue(boolean doseTwoDue) {
        isDoseTwoDue = doseTwoDue;
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

    public String getDateDoseOneGiven() {
        return dateDoseOneGiven;
    }

    public void setDateDoseOneGiven(String dateDoseOneGiven) {
        this.dateDoseOneGiven = dateDoseOneGiven;
    }

    public String getDateDoseTwoGiven() {
        return dateDoseTwoGiven;
    }

    public void setDateDoseTwoGiven(String dateDoseTwoGiven) {
        this.dateDoseTwoGiven = dateDoseTwoGiven;
    }

    public String getDoseOneGivenLocation() {
        return doseOneGivenLocation;
    }

    public void setDoseOneGivenLocation(String doseOneGivenLocation) {
        this.doseOneGivenLocation = doseOneGivenLocation;
    }

    public String getDoseTwoGivenLocation() {
        return doseTwoGivenLocation;
    }

    public void setDoseTwoGivenLocation(String doseTwoGivenLocation) {
        this.doseTwoGivenLocation = doseTwoGivenLocation;
    }
}
