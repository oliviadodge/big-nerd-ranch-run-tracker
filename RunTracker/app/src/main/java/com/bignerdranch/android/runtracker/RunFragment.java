package com.bignerdranch.android.runtracker;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by oliviadodge on 4/18/2015.
 */
public class RunFragment extends Fragment {
    private static final String TAG = "RunFragment";
    private static final String ARG_RUN_ID = "RUN_ID";
    private static final int LOAD_RUN = 0;
    private static final int LOAD_LOCATION = 1;

    private Button mStartButton, mStopButton, mMapButton;
    private TextView mStartedTextView, mLatitudeTextView,
    mLongitudeTextView, mAltitudeTextView, mDurationTextView;

    private RunManager mRunManager;

    public static RunFragment newInstance(long runId) {
        Bundle args = new Bundle();
        args.putLong(ARG_RUN_ID, runId);
        RunFragment rf = new RunFragment();
        rf.setArguments(args);
        return rf;
    }

    private BroadcastReceiver mLocationReceiver = new LocationReceiver(){
        @Override
        protected void onLocationReceived(Context context, Location location) {
            if (!mRunManager.isTrackingRun(mRun))
                return;
            mLastLocation = location;
            if (isVisible()) {
                updateUI();
            }
        }

        @Override
        protected void onProviderEnabledChanged(boolean enabled) {
            int toastText = enabled ? R.string.gps_enabled : R.string.gps_disabled;
            Toast.makeText(getActivity(), toastText, Toast.LENGTH_SHORT).show();
        }
    };

    private Run mRun;
    private Location mLastLocation;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mRunManager = RunManager.get(getActivity());
        //check for a Run id as an argument, and find the run
        Bundle args = getArguments();
        if (args != null) {
            long runId = args.getLong(ARG_RUN_ID, -1);
            Log.i(TAG, "OnCreate(Bundle savedInstanceState) called: Run ID is " + runId);
            if (runId != -1) {
                android.support.v4.app.LoaderManager lm = getLoaderManager();
                lm.initLoader(LOAD_RUN, args, new RunLoaderCallbacks());
                lm.initLoader(LOAD_LOCATION, args, new LocationLoaderCallbacks());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_run, container, false);

        mStartedTextView = (TextView)view.findViewById(R.id.run_startedTextView);
        mLatitudeTextView = (TextView)view.findViewById(R.id.run_latitudeTextView);
        mLongitudeTextView = (TextView)view.findViewById(R.id.run_longitudeTextView);
        mAltitudeTextView = (TextView)view.findViewById(R.id.run_altitudeTextView);
        mDurationTextView = (TextView)view.findViewById(R.id.run_durationTextView);

        mStartButton = (Button)view.findViewById(R.id.run_startButton);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRun == null) {
                    mRun = mRunManager.startNewRun();
                } else {
                    mRunManager.startTrackingRun(mRun);
                }
                updateUI();
            }
        });
        mStopButton = (Button)view.findViewById(R.id.run_stopButton);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRunManager.stopRun();
                updateUI();
            }
        });
        mMapButton = (Button)view.findViewById(R.id.run_mapButton);
        mMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), RunMapActivity.class);
                i.putExtra(RunMapActivity.EXTRA_RUN_ID, mRun.getId());
                startActivity(i);
            }
        });

        updateUI();

        return view;
    }

    public void updateUI(){
        boolean started = mRunManager.isTrackingRun();
        boolean trackingThisRun = mRunManager.isTrackingRun(mRun);

        if (mRun != null) {
            Log.i(TAG, mRun.toString());
            mStartedTextView.setText(mRun.getStartDate().toString());
        } else {
            Log.i(TAG, "updateUI() called and mRun is null");
        }

        int durationSeconds = 0;
        if (mRun != null && mLastLocation != null) {
            durationSeconds = mRun.getDurationSeconds(mLastLocation.getTime());
            mDurationTextView.setText(durationSeconds);
            mLatitudeTextView.setText(Double.toString(mLastLocation.getLatitude()));
            mLongitudeTextView.setText(Double.toString(mLastLocation.getLongitude()));
            mAltitudeTextView.setText(Double.toString(mLastLocation.getAltitude()));
            mMapButton.setEnabled(true);
        } else {
            mMapButton.setEnabled(false);
        }

        mStartButton.setEnabled(!started);
        mStopButton.setEnabled(started && trackingThisRun);
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().registerReceiver(mLocationReceiver,
                new IntentFilter((RunManager.ACTION_LOCATION)));
    }

    @Override
    public void onStop() {
        getActivity().unregisterReceiver(mLocationReceiver);
        super.onStop();
    }

    private class RunLoaderCallbacks implements android.support.v4.app.LoaderManager.LoaderCallbacks<Run> {
        private static final String TAG = "RunLoaderCallbacks";

        @Override
        public android.support.v4.content.Loader<Run> onCreateLoader(int i, Bundle bundle) {
            Log.i(TAG, "onCreateLoader called and run id = " + bundle.getLong(ARG_RUN_ID));
            return new RunLoader(getActivity(), bundle.getLong(ARG_RUN_ID));
        }

        @Override
        public void onLoadFinished(android.support.v4.content.Loader<Run> runLoader, Run run) {
            Log.i(TAG, "onLoadFinished called and run = " + run);
            mRun = run;
            updateUI();
        }

        @Override
        public void onLoaderReset(android.support.v4.content.Loader<Run> runLoader) {
            //do nothing
        }
    }

    private class LocationLoaderCallbacks implements android.support.v4.app.LoaderManager.LoaderCallbacks<Location> {

        @Override
        public android.support.v4.content.Loader<Location> onCreateLoader(int i, Bundle bundle) {
            return new LastLocationLoader(getActivity(), bundle.getLong(ARG_RUN_ID));
        }

        @Override
        public void onLoadFinished(android.support.v4.content.Loader<Location> locationLoader, Location location) {
            mLastLocation = location;
            updateUI();
        }

        @Override
        public void onLoaderReset(android.support.v4.content.Loader<Location> locationLoader) {
            //do nothing
        }
    }
}
