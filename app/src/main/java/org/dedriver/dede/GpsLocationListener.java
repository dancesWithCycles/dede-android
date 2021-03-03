package org.dedriver.dede;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import androidx.annotation.NonNull;

import timber.log.Timber;

public class GpsLocationListener implements LocationListener {
    private final LocationUpdateService locationUpdateService;

    public GpsLocationListener(LocationUpdateService locationUpdateService) {
        this.locationUpdateService = locationUpdateService;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
//        Timber.d("location: %s",location);
        MainActivity.gpsLocation = location;
        //update UI
        MainActivity.updateLocation();
        this.locationUpdateService.launchNotification();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
//        Timber.d("onStatusChanged: provider: %s, status: %s", provider, status);
        /*todo Does the removal of this override yield to runtime exceptions?*/
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Timber.d("onProviderEnabled");
        /*todo Does the removal of this override yield to runtime exceptions?*/
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Timber.d("onProviderDisabled");
        /*todo Does the removal of this override yield to runtime exceptions?*/
    }
}
