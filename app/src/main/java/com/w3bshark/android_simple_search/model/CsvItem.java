package com.w3bshark.android_simple_search.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CsvItem implements Parcelable {
    // Internal ID
    String id;
    // ID from CSV column "ID"
    String csvId;
    // Description from CSV column "Item Description"
    String description;

    public CsvItem() {}

    CsvItem(Parcel in) {
        this.id = in.readString();
        this.csvId = in.readString();
        this.description = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(csvId);
        dest.writeString(description);
    }

    public static final Parcelable.Creator<CsvItem> CREATOR
            = new Parcelable.Creator<CsvItem>() {
        public CsvItem createFromParcel(Parcel in) {
            return new CsvItem(in);
        }

        public CsvItem[] newArray(int size) {
            return new CsvItem[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCsvId() {
        return csvId;
    }

    public void setCsvId(String csvId) {
        this.csvId = csvId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
