package com.w3bshark.android_simple_search.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.w3bshark.android_simple_search.R;
import com.w3bshark.android_simple_search.fragments.MainActivityFragment;
import com.w3bshark.android_simple_search.services.CsvPullService;

public class MainActivity extends AppCompatActivity {

    // Key for storing global check if csv has been parsed and stored in DB
    public static final String CSV_PARSED = "CSV_PARSED";
    public Intent mServiceIntent;

    private SearchView.OnQueryTextListener mSearchQueryListener = new SearchView.OnQueryTextListener() {
        /**
         * Handle submission of queries
         * Overridden from SearchView.OnQueryTextListener
         * @param query text of query entered in search view
         * @return boolean to handle whether or not the query can be submitted
         */
        @Override
        public boolean onQueryTextSubmit(String query) {
            if (query != null) {
                MainActivityFragment fragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.main_container);
                fragment.filterCSVItems(query);
                return true;
            } else {
                return false;
            }
        }

        /**
         * Handle query text change in search view
         * Overridden from SearchView.OnQueryTextListener
         * @param newText text of query entered in search view
         * @return boolean to handle whether or not the query can be changed
         */
        @Override
        public boolean onQueryTextChange(String newText) {
            if (newText != null) {
                MainActivityFragment fragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.main_container);
                fragment.filterCSVItems(newText);
                return true;
            } else {
                MainActivityFragment fragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.main_container);
                // Passing null here will just reset the view with all data
                fragment.filterCSVItems(null);
                return false;
            }
        }
    };
    private MenuItemCompat.OnActionExpandListener mActionExpandListener = new MenuItemCompat.OnActionExpandListener() {
        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
            MainActivityFragment fragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.main_container);
            // Passing null here will just reset the view with all data
            fragment.filterCSVItems(null);
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // Check if we're loading the CSV for the first time, so that we can start a service
        // to copy the CSV data into our DB
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean defaultValue = false;
        Boolean csvParsed = settings.getBoolean(CSV_PARSED, defaultValue);
        if (!csvParsed) {
            Context mContext = getApplicationContext();
            mServiceIntent = new Intent(this, CsvPullService.class);
            mServiceIntent.setAction(CsvPullService.ACTION_CSVPULL);
            this.startService(mServiceIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        MenuItemCompat.setOnActionExpandListener(menuItem, mActionExpandListener);
        // Associate searchable configuration with the SearchView
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(mSearchQueryListener);
//        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
//            @Override
//            public boolean onClose() {
//                MainActivityFragment fragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.main_container);
//                // Passing null here will just reset the view with all data
//                fragment.filterCSVItems(null);
//                return false;
//            }
//        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle new intents
     * For the purposes of this application, this is used for handling the search action intent,
     *   specifically when the user presses the search icon button after entering a query
     * @param intent the intent received that must be handled
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            MainActivityFragment fragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.main_container);
            fragment.filterCSVItems(query);
        }
    }
}
