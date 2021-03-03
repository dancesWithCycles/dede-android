package org.dedriver.dede;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.dedriver.dede.activity.MainActivity;

import timber.log.Timber;

public class LocationUpdateService extends Service {
    public final static int NOTIFICATION_ID = 1001;
    public static NotificationManager notificationManager = null;
    private static GpsLocationListener gpsLocationListener = null;
    private LocationManager locationManager = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        /*handle notifications*/
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, getNotification());

        enableLocationUpdate();

        /*todo What is the different between the following return values? */
        return START_STICKY;
        /*return super.onStartCommand(intent, flags, startId);*/
    }

    @SuppressLint("MissingPermission")
    private void enableLocationUpdate() {
        Timber.d("enableLocationUpdate started");
        gpsLocationListener = new GpsLocationListener(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (isLocationEnabled()) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, gpsLocationListener);
            } else {
                Timber.w("onStartCommand: location is disabled");
                Toast.makeText(this, "location is disabled", Toast.LENGTH_SHORT).show();
            }
        } else {
            Timber.w("onStartCommand: GPS service unavailable");
            Toast.makeText(this, "GPS service unavailable", Toast.LENGTH_SHORT).show();
        }
        Timber.d("enableLocationUpdate finished");
    }

    public Notification getNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, "RunLocation")
                .setContentTitle("Dede")
                .setContentText("Die Dede App erfasst GPS-Daten.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent);
        return notification.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "RunLocation",
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public ComponentName startService(Intent service) {
        return super.startService(service);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        /*release location manager*/
        if (locationManager != null) {
            locationManager.removeUpdates(gpsLocationListener);
        } else {
//            todo Why is location manager on device nexus with android 5.1.1 unavailable?
            Timber.e("onDestroy: location manager unavailable");
        }
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
