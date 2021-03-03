package org.dedriver.dede;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;

import timber.log.Timber;

import static org.dedriver.dede.MainActivity.gpsLocation;

public class LocationUpdateService extends Service {
    public final static int NOTIFICATION_ID = 1001;
    public static NotificationManager notificationManager = null;
    private static GpsLocationListener gpsLocationListener = null;
    private LocationManager locationManager = null;

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        /*handle notifications*/
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, getNotification());

        /*check permission*/
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkBackgroundLocation()) {
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
        } else {
            Timber.w("onStartCommand: permissions not granted");
        }

        /*todo What is the different between the following return values? */
        return START_STICKY;
        /*return super.onStartCommand(intent, flags, startId);*/
    }

    public Notification getNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        NotificationCompat.Builder notification;
        if (gpsLocation != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss z");
            String stringDate = simpleDateFormat.format(gpsLocation.getTime());
            Timber.d("date: %s", stringDate);
            notification = new NotificationCompat.Builder(this, "RunLocation")
                    .setContentTitle("Dede")
                    .setContentText("GPS-Zeitstempel: " + stringDate)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent);
        } else {
            notification = new NotificationCompat.Builder(this, "RunLocation")
                    .setContentTitle("Dede")
                    .setContentText("Daten nicht verfügbar")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent);
        }
        return notification.build();
    }

    public void launchNotification() {
        notificationManager.notify(NOTIFICATION_ID, getNotification());
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

    public boolean checkBackgroundLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return ActivityCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
