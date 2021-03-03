package org.dedriver.dede;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import org.dedriver.dede.activity.MainActivity;
import org.dedriver.dede.model.LocationData;
import org.dedriver.dede.model.LocationResult;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static org.dedriver.dede.activity.MainActivity.gpsLocation;
import static org.dedriver.dede.activity.MainActivity.textViewHttpPostTs;
import static org.dedriver.dede.activity.MainActivity.user;

public class PostLocation implements Runnable {
    private final Handler handlerMain = new Handler(Looper.getMainLooper());
    private Location locationOld = null;

    @Override
    public void run() {
        while (MainActivity.threadPostRunning) {
            /*post location to back end*/
            postLocation(gpsLocation);
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void postLocation(Location location) {
        if (location != null) {
            if (locationOld == null) {
                locationOld = location;
            } else {
                float distance = locationOld.distanceTo(location);
                long delta = location.getTime() - locationOld.getTime();

                /*Is distance greater than 50 m or age greater than 1000 ms?*/
                if (distance >= 50 || delta >= 1000) {
                    post(location);
                    updateUiTimestamp(location);
                    locationOld = location;
                }
            }
        }
    }

    private void updateUiTimestamp(Location location) {
        handlerMain.post(new Runnable() {
            @Override
            public void run() {
                textViewHttpPostTs.setText("Latest POST Timestamp: " + location.getTime());
            }
        });
    }

    private void post(Location location) {
        GlobalApplication.apiManager.createLocation(new LocationData(
                user.getUuid().getUuid(),
                "label",
                user.getAlias(),
                Double.toString(location.getLatitude()),
                Double.toString(location.getLongitude()),
                "45",
                "2.3",
                user.getVehicle(),
                location.getTime()
        ), new Callback<LocationResult>() {
            @Override
            public void onResponse(Call<LocationResult> call, Response<LocationResult> response) {
//                Timber.d("response: %s", String.valueOf(response.code()));
                LocationResult locationResult = response.body();
                if (response.isSuccessful()) {
//                    TODO Is response handling important?
                } else if (locationResult != null) {
                    Timber.e("location POST failed: %s", locationResult.getError());
                } else {
//                    Timber.w("unknown response received");
//                    TODO Enable Dede back end to send qualified response!
                }
            }

            @Override
            public void onFailure(Call<LocationResult> call, Throwable t) {
                Timber.d("onFailure started");
                Timber.e(t);
                Timber.d("onFailure finished");
            }
        });
    }
}
