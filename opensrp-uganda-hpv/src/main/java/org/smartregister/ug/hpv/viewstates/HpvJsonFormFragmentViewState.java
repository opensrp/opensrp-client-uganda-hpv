package org.smartregister.ug.hpv.viewstates;

import android.os.Parcel;

import com.vijay.jsonwizard.viewstates.JsonFormFragmentViewState;

/**
 * Created by vijay on 5/14/15.
 */
public class HpvJsonFormFragmentViewState extends JsonFormFragmentViewState implements android.os.Parcelable {
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public HpvJsonFormFragmentViewState() {
    }

    private HpvJsonFormFragmentViewState(Parcel in) {
        super(in);
    }

    public static final Creator<HpvJsonFormFragmentViewState> CREATOR = new Creator<HpvJsonFormFragmentViewState>() {
        public HpvJsonFormFragmentViewState createFromParcel(
                Parcel source) {
            return new HpvJsonFormFragmentViewState(source);
        }

        public HpvJsonFormFragmentViewState[] newArray(
                int size) {
            return new HpvJsonFormFragmentViewState[size];
        }
    };
}
