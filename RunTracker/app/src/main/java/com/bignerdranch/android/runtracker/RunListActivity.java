package com.bignerdranch.android.runtracker;

import android.support.v4.app.Fragment;

/**
 * Created by oliviadodge on 4/30/2015.
 */
public class RunListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new RunListFragment();
    }
}
