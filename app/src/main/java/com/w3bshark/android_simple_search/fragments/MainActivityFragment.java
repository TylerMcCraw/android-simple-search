package com.w3bshark.android_simple_search.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.w3bshark.android_simple_search.R;
import com.w3bshark.android_simple_search.Util;
import com.w3bshark.android_simple_search.activities.MainActivity;
import com.w3bshark.android_simple_search.data.CsvDbContract.CsvDbEntry;
import com.w3bshark.android_simple_search.data.CsvDbDataLoader;
import com.w3bshark.android_simple_search.model.CsvItem;
import com.w3bshark.android_simple_search.services.CsvPullService;
import com.w3bshark.android_simple_search.widgets.MainRecyclerAdapter;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<CsvItem>> {

    private RecyclerView mRecyclerView;
    private MainRecyclerAdapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private View mCoordinatorLayoutView;
    // Layout that allows users to swipe down the screen to refresh data anytime
    private SwipeRefreshLayout mSwipeRefreshLayout;
    // CSV item parcelable key for saving instance state
    private static final String SAVED_ITEMS = "SAVED_ITEMS";
    private ArrayList<CsvItem> csvItems;
    private ArrayList<CsvItem> unfilteredCsvItems;
    private FrameLayout mProgressOverlay;

    private static final String[] CSVDATA_COLUMNS = {
            CsvDbEntry.TABLE_NAME + "." + CsvDbEntry._ID,
            CsvDbEntry.COLUMN_CSVID,
            CsvDbEntry.COLUMN_DESCRIPTION,
    };
    private static final int CSVDBDATA_LOADER = 0;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        //Set up the xml layout
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.main_fragment_recyclerview);
        mCoordinatorLayoutView = rootView.findViewById(R.id.main_fragment_coordinatorlayout);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this.getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        // Set up initial adapter (until we retrieve our data) so there is no skipping the layout
        mRecyclerView.setAdapter(new MainRecyclerAdapter(getActivity(), new ArrayList<CsvItem>()));

        // Attempt to restore loaded csv data from savedInstanceState
        if (savedInstanceState != null) {
            csvItems = savedInstanceState.getParcelableArrayList(SAVED_ITEMS);
            if (mRecyclerAdapter == null) {
                initializeAdapter();
            } else {
                mRecyclerAdapter.notifyDataSetChanged();
            }
        }
        // If we couldn't retrieve csv data from a saved instance state
        if (csvItems == null || csvItems.size() == 0) {
            csvItems = new ArrayList<>();
        }

        getLoaderManager().initLoader(CSVDBDATA_LOADER, null, this);

        // Handle user pull-down to refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.main_fragment_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRecyclerView.refreshDrawableState();
                MainActivity mainAct = (MainActivity) getActivity();
                mainAct.mServiceIntent = new Intent(mainAct, CsvPullService.class);
                mainAct.mServiceIntent.setAction(CsvPullService.ACTION_CSVPULL);
                mainAct.startService(mainAct.mServiceIntent);
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.smashing_pink);

        // Register a Broadcast Receiver so that we know when to restart the loader and refresh data displayed
        // The filter's action is com.w3bshark.android_simple_search.action.CSVPULLRETURN
        IntentFilter mStatusIntentFilter = new IntentFilter(CsvPullService.ACTION_CSVPULL_RETURN);
        // Instantiates a new ServiceStateReceiver
        BroadcastReceiver mServiceStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(CsvPullService.ACTION_CSVPULL_RETURN)) {
                    restartLoader();
                    // Stop the service, since we no longer need it
                    Intent stopIntent = new Intent(getActivity(), CsvPullService.class);
                    getActivity().stopService(stopIntent);
                    toggleProgressBar(false);
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        };
        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                mServiceStateReceiver,
                mStatusIntentFilter);

        return rootView;
    }

    private void initializeAdapter(){
        mRecyclerAdapter = new MainRecyclerAdapter(getActivity(), csvItems);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (csvItems != null) {
            savedInstanceState.putParcelableArrayList(SAVED_ITEMS, csvItems);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public Loader<ArrayList<CsvItem>> onCreateLoader(int i, Bundle bundle) {
        Uri csvItemsUri = CsvDbEntry.buildAllCsvItemsUri();
        return new CsvDbDataLoader(getActivity(), csvItemsUri);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<CsvItem>> loader, ArrayList<CsvItem> data) {
        if (csvItems == null) {
            csvItems = new ArrayList<>();
        }
        unfilteredCsvItems = data;
        csvItems.clear();
        String temp = data.size() + getString(R.string.items_count);
        CsvItem item = new CsvItem();
        item.setId("00");
        item.setDescription(temp);
        csvItems.add(item);
        csvItems.addAll(data);
        if (mRecyclerAdapter == null) {
            initializeAdapter();
        } else {
            mRecyclerAdapter.notifyDataSetChanged();
        }
        toggleProgressBar(false);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<CsvItem>> loader) {
        if (csvItems != null) {
            this.csvItems.clear();
        }
        if (mRecyclerAdapter != null) {
            mRecyclerAdapter.notifyDataSetChanged();
        }
        toggleProgressBar(false);
    }

    public void filterCSVItems(String query) {
        mSwipeRefreshLayout.setRefreshing(false);
        toggleProgressBar(true);
        if (query == null || query.isEmpty()) {
            csvItems.clear();
            String temp = unfilteredCsvItems.size() + getString(R.string.items_count);
            CsvItem item = new CsvItem();
            item.setId("00");
            item.setDescription(temp);
            csvItems.add(item);
            csvItems.addAll(unfilteredCsvItems);
            restartLoader();
            toggleProgressBar(false);
        } else {
            final ArrayList<CsvItem> filteredItemsList = filter(unfilteredCsvItems, query);
            mRecyclerAdapter.animateTo(filteredItemsList);
            mRecyclerView.scrollToPosition(0);
            csvItems.clear();
            String temp = filteredItemsList.size() + getString(R.string.items_count);
            CsvItem item = new CsvItem();
            item.setId("00");
            item.setDescription(temp);
            csvItems.add(item);
            csvItems.addAll(filteredItemsList);
            toggleProgressBar(false);
        }
    }

    private ArrayList<CsvItem> filter(ArrayList<CsvItem> items, String query) {
        query = query.toLowerCase();

        final ArrayList<CsvItem> filteredModelList = new ArrayList<>();
        for (CsvItem item : items) {
            final String text = item.getDescription().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(item);
            }
        }
        return filteredModelList;
    }

    private void restartLoader() {
        if (getLoaderManager() != null) {
            getLoaderManager().restartLoader(CSVDBDATA_LOADER, null, this);
        }
    }

    private void toggleProgressBar(Boolean toggle) {
        if (mProgressOverlay == null) {
            mProgressOverlay = (FrameLayout) mCoordinatorLayoutView.findViewById(R.id.progress_overlay);
        }
        if (toggle) {
            Util.animateView(mProgressOverlay, View.VISIBLE, 0.4f, 3);
        } else {
            Util.animateView(mProgressOverlay, View.INVISIBLE, 0.4f, 3);
        }
    }
}