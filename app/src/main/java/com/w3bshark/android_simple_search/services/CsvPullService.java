package com.w3bshark.android_simple_search.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.w3bshark.android_simple_search.activities.MainActivity;
import com.w3bshark.android_simple_search.data.CsvDbContract.CsvDbEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class CsvPullService extends IntentService {
    // Asset file name for CSV - In production, this should not be hard-coded
    public static final String FILENAME_CSV = "items.csv";
    public static final String FILENAME_CSV_COPY = "items_copy.csv";
    // CSV column location of "ID" starting from 0
    public static final int CSV_ITEM_ID_LOCATION = 0;
    public static final String CSV_ITEM_ID_NAME = "ID";
    // CSV column location of "Item Description" starting from 0
    public static final int CSV_ITEM_DESCR_LOCATION = 2;
    public static final String CSV_ITEM_DESCR_NAME = "Item Description";
    // Main IntentService action
    public static final String ACTION_CSVPULL = "com.w3bshark.android_simple_search.action.CSVPULL";
    public static final String ACTION_CSVPULL_RETURN = "com.w3bshark.android_simple_search.action.CSVPULLRETURN";

    public CsvPullService() {
        super("CSVPullService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CSVPULL.equals(action)) {
                parseAndStoreCSVData();
            } else  {
                // Something terrible went wrong
                // TODO: Handle wrong action case here
            }
        }
    }

    /**
     * Handle pulling data from CSV file and store it in our database table
     * using a provided background thread.
     */
    private void parseAndStoreCSVData() {
        InputStream is = null;
        String line;
        Vector<ContentValues> csvItemValuesVector = new Vector<>();

        try {
            is = getAssets().open(FILENAME_CSV);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            // Remove first line from being added to the list
            // We're assuming here that the first row in our CSV will be a header row
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] rows = line.split(",");
                if (rows[CSV_ITEM_DESCR_LOCATION] != null) {
                    ContentValues csvItemValues = new ContentValues();
                    csvItemValues.put(CsvDbEntry.COLUMN_CSVID, rows[CSV_ITEM_ID_LOCATION]);
                    csvItemValues.put(CsvDbEntry.COLUMN_DESCRIPTION, rows[CSV_ITEM_DESCR_LOCATION]);
                    csvItemValuesVector.add(csvItemValues);
                }
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            try {
                if (is != null) {
                    is.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Now store the items from the CSV in our DB table
        ContentValues[] cvArray = new ContentValues[csvItemValuesVector.size()];
        csvItemValuesVector.toArray(cvArray);
        int inserted = getApplicationContext().getContentResolver().bulkInsert(CsvDbEntry.buildAllCsvItemsUri(), cvArray);
        if (inserted > 0) {
            // Edit the preferences so that we know not to re-parse the CSV again.
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor preferenceEditor = settings.edit();
            preferenceEditor.putBoolean(MainActivity.CSV_PARSED, true);
            preferenceEditor.apply();
        }

        // Broadcast back that the service has completed it's work
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_CSVPULL_RETURN);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }
}
