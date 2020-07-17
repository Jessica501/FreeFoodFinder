package com.example.freefood;

import android.location.Location;

public interface UsesLocation {
    void getMyLocation();
    void onLocationChanged(Location location);
    void startLocationUpdates();
}
