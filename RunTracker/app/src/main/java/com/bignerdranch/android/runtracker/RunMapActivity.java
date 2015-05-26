package com.bignerdranch.android.runtracker;

import android.support.v4.app.Fragment;

/**
 * Created by oliviadodge on 5/24/2015.
 */
public class RunMapActivity extends SingleFragmentActivity {
    /**
     * a key for apssing a run ID as a long
     */
    public static final String EXTRA_RUN_ID =
            "com.bignerdranch.android.runtracker.run_id";

    @Override
    protected Fragment createFragment() {
        long runId = getIntent().getLongExtra(EXTRA_RUN_ID, -1);
        if (runId != -1) {
            return RunMapFragment.newInstance(runId);
        } else {
            return new RunMapFragment();
        }
    }
}
