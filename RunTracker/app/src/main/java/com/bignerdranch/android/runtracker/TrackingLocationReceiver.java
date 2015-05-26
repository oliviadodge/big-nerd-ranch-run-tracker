package com.bignerdranch.android.runtracker;

import android.content.Context;
import android.location.Location;

/**
 * Created by oliviadodge on 4/21/2015.
 */
public class TrackingLocationReceiver extends LocationReceiver {
    @Override
    protected void onLocationReceived(Context c, Location location) {
        RunManager.get(c).insertLocation(location);
    }
}
