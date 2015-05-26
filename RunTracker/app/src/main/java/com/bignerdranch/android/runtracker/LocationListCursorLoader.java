package com.bignerdranch.android.runtracker;

import android.content.Context;
import android.database.Cursor;

/**
 * Created by oliviadodge on 5/24/2015.
 */
public class LocationListCursorLoader extends SQLiteCursorLoader {
    private long mRunId;

    public LocationListCursorLoader(Context c, long runId) {
        super(c);
        mRunId = runId;
    }

    @Override
    protected Cursor loadCursor() {
        return RunManager.get(getContext()).queryLocationsForRun(mRunId);
    }
}
