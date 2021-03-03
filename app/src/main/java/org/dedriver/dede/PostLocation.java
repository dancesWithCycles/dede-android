package org.dedriver.dede;

import android.location.Location;

import static org.dedriver.dede.MainActivity.gpsLocation;
import static org.dedriver.dede.MainActivity.insertData;
import static org.dedriver.dede.MainActivity.user;

public class PostLocation implements Runnable {
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

    private void postLocation(Location location){
        if (location != null) {
            if (locationOld == null) {
                /*todo Why is a static function and not a member method called?*/
                insertData(user.getUuid().getUuid(),
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getTime(),
                        user.getAlias(),
                        user.getVehicle());
                locationOld = location;
            } else {
                float distance = locationOld.distanceTo(location);
                long delta=location.getTime()-locationOld.getTime();

                /*Is distance greater than 50 m or age greater than 1000 ms?*/
                if (distance>=50||delta>=1000) {
                    /*todo Why is a static function and not a member method called?*/
                    insertData(user.getUuid().getUuid(),
                            location.getLatitude(),
                            location.getLongitude(),
                            location.getTime(),
                            user.getAlias(),
                            user.getVehicle());
                    locationOld = location;
                }
            }
        }
    }
}
