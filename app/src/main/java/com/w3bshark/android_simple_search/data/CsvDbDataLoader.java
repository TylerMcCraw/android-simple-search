package com.w3bshark.android_simple_search.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import com.w3bshark.android_simple_search.data.CsvDbContract.CsvDbEntry;
import com.w3bshark.android_simple_search.model.CsvItem;

import java.util.ArrayList;
import java.util.List;

public class CsvDbDataLoader extends AsyncTaskLoader<ArrayList<CsvItem>> {

    private Context mContext;
    private ArrayList<CsvItem> mCsvItems;
    private Uri mCsvDbUri;

    public static final String[] CSVDB_COLUMNS = {
            CsvDbEntry.TABLE_NAME + "." + CsvDbEntry._ID,
            CsvDbEntry.COLUMN_CSVID,
            CsvDbEntry.COLUMN_DESCRIPTION
    };

    public static final int COL_ID = 0;
    public static final int COL_CSVID = 1;
    public static final int COL_DESCRIPTION = 2;

    public CsvDbDataLoader(Context context, Uri csvDbUri) {
        super(context);
        mContext = context;
        mCsvDbUri = csvDbUri;
    }

    @Override
    public ArrayList<CsvItem> loadInBackground() {
        if (mCsvItems == null) {
            mCsvItems = new ArrayList<>();
        }
        ArrayList<CsvItem> entries = new ArrayList<>(mCsvItems.size());
        String sortOrder = CsvDbEntry._ID + " ASC";

        Cursor cursor = getContext().getContentResolver().query(
                CsvDbEntry.buildAllCsvItemsUri(), CSVDB_COLUMNS, null, null, sortOrder);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                CsvItem csvItem = new CsvItem();
                csvItem.setId(cursor.getString(COL_ID));
                csvItem.setCsvId(cursor.getString(COL_CSVID));
                csvItem.setDescription(cursor.getString(COL_DESCRIPTION));
                mCsvItems.add(csvItem);
                entries.add(csvItem);
            }
            while (cursor.moveToNext());

            //TODO: Handle large amounts of data being loaded.
            // We should probably lazy-load this in the future, if possible
        }

        if (cursor != null) {
            cursor.close();
        }

        return entries;
    }

    @Override
    public void deliverResult(ArrayList<CsvItem> csvItems) {
        if (isReset()) {
            if (csvItems != null) {
                releaseResources(csvItems);
                return;
            }
        }

        ArrayList<CsvItem> oldCsvItems = mCsvItems;
        mCsvItems = csvItems;

        if (isStarted()) {
            super.deliverResult(csvItems);
        }

        if (oldCsvItems != null && oldCsvItems != csvItems) {
            releaseResources(oldCsvItems);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mCsvItems != null) {
            deliverResult(mCsvItems);
        }

        if (takeContentChanged()) {
            forceLoad();
        } else if (mCsvItems == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();

        if (mCsvItems != null) {
            releaseResources(mCsvItems);
            mCsvItems = null;
        }
    }

    @Override
    public void onCanceled(ArrayList<CsvItem> csvItems) {
        super.onCanceled(csvItems);
        releaseResources(csvItems);
    }

    @Override
    public void forceLoad() {
        super.forceLoad();
    }

    private void releaseResources(List<CsvItem> csvItems) {
    }
}