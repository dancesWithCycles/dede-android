package org.dedriver.dede.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import org.dedriver.dede.BuildConfig;
import org.dedriver.dede.LocationUpdateService;
import org.dedriver.dede.PostLocation;
import org.dedriver.dede.R;
import org.dedriver.dede.User;

import java.util.Objects;
import java.util.UUID;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final User user = new User();
    private static final int GPS_PERMISSION_CODE = 1;

    public static Location gpsLocation = null;

    public static boolean threadPostRunning = true;
    public static TextView textViewHttpPostTs = null;
    private static String buttonStartText = "Start";
    private static TextView textViewId = null;
    private static TextView textViewGpsLat = null;
    private static TextView textViewGpsLon = null;
    private static TextView textViewGpsTs = null;
    private static Button buttonStart = null;
    private final PostLocation postLocation = new PostLocation();
    private boolean currentlyTracking = false;
    public View.OnClickListener StartOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (buttonStart.getText() == "Start") {
                /*start service LocationUpdateService*/
                startLocUpService();
            } else if (buttonStart.getText() == "Stop") {
                /*stop service LocationUpdateService*/
                stopLocUpService();
            } else {
                Timber.e("start button showing unexpected behavior");
            }
        }
    };

    public static void updateLocation() {
        if (gpsLocation != null) {
            if (textViewGpsLat != null) {
                textViewGpsLat.setText("GPS Breitengrad: " + gpsLocation.getLatitude());
            }
            if (textViewGpsLon != null) {
                textViewGpsLon.setText("GPS Längengrad: " + gpsLocation.getLongitude());
            }
            if (textViewGpsTs != null) {
                textViewGpsTs.setText("GPS Zeitstempel: " + gpsLocation.getTime());
            }
        }
    }

    private void enableLocationService() {
        final Intent intent = new Intent(MainActivity.this, LocationUpdateService.class);
        ComponentName componentName;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            componentName = startForegroundService(intent);
        } else {
            componentName = startService(intent);
        }
    }

    private void startLocUpService() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Timber.d("onStartCommand: permission FINE_LOCATION granted");
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Timber.d("onStartCommand: permission COARSE_LOCATION granted");

                // enable location updates
                enableLocationService();

                /*launch location post service*/
                Thread threadPost = new Thread(postLocation);
                threadPost.start();
                threadPostRunning = true;
                buttonStart.setText("Stop");
                buttonStartText = "Stop";

                // update preferences
                SharedPreferences sharedPreferences = this.getSharedPreferences("org.dedriver.dede.prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                currentlyTracking = true;
                editor.putBoolean("currentlyTracking", true);
                editor.apply();

            } else {
                Timber.w("onStartCommand: permission COARSE_LOCATION NOT granted");
                requestPermission();
            }
        } else {
            Timber.w("onStartCommand: permission FINE_LOCATION NOT granted");
            requestPermission();
        }
    }

    private void stopLocUpService() {
        final Intent intent = new Intent(MainActivity.this, LocationUpdateService.class);
        stopService(intent);

        /*release location post service*/
        threadPostRunning = false;
        buttonStart.setText("Start");
        buttonStartText = "Start";

        SharedPreferences sharedPreferences = this.getSharedPreferences("org.dedriver.dede.prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        currentlyTracking = false;
        editor.putBoolean("currentlyTracking", false);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO Why do I have to reset the button text?
        buttonStart.setText(buttonStartText);
    }

    private void initViews() {
        textViewId = findViewById(R.id.textViewId);
        textViewGpsLat = findViewById(R.id.textViewGpsLat);
        textViewGpsLon = findViewById(R.id.textViewGpsLon);
        textViewGpsTs = findViewById(R.id.textViewGpsTs);
        textViewHttpPostTs = findViewById(R.id.textViewHttpPostTs);
        buttonStart = findViewById(R.id.buttonStart);

        /*MODE_PRIVATE: By setting this mode, the file can only be accessed using calling application*/
        SharedPreferences sharedPreferences = this.getSharedPreferences("org.dedriver.dede.prefs", Context.MODE_PRIVATE);
        Timber.d("appID: %s", sharedPreferences.getString("appID", ""));
        textViewId.setText(getString(R.string.text_view_id_text) + sharedPreferences.getString("appID", ""));
//TODO cleanup        textViewId.setText(getString(R.string.text_view_id_text) + user.getUuid().getUuid());
        textViewHttpPostTs.setText(getString(R.string.text_view_http_post_ts_text));
        buttonStart.setOnClickListener(StartOnClickListener);
        buttonStart.setText(buttonStartText);

        loadTextSizeFromPreference(PreferenceManager.getDefaultSharedPreferences(this));
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Timber.d("request FINE, COARSE location");
            requestPermissions(new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    // This function is called when user accept or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when user is prompt for permission.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == GPS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Showing the toast message
                Toast.makeText(MainActivity.this,
                        "GPS Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(MainActivity.this,
                        "GPS Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*enable home icon,chose graphics, enable click*/
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        /*load preferences from XML
         * do not override user settings*/
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        /*register preferences*/
        setupSharedPreferences();

        /*set up logging*/
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        SharedPreferences sharedPreferences = this.getSharedPreferences("org.dedriver.dede.prefs", Context.MODE_PRIVATE);
        boolean firstTimeLoadingApp = sharedPreferences.getBoolean("firstTimeLoadingApp", true);
        if (firstTimeLoadingApp) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("firstTimeLoadingApp", false);
            editor.putString("appID", UUID.randomUUID().toString());
            Timber.d("appID: %s", sharedPreferences.getString("appID", ""));
            editor.apply();
        }

        initViews();

    }

    private void setupSharedPreferences() {
//        TODO Shall I call PreferenceManager or this?
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settingsmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Timber.d("options item id: %s", id);
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Timber.d("key: %s", key);
        if (key.equals(getString(R.string.pref_text_size_key))) {
            loadTextSizeFromPreference(sharedPreferences);
        } else if (key.equals(getString(R.string.pref_server_address_key))) {
            loadURLFromPreference(sharedPreferences);
        } else if (key.equals(getString(R.string.pref_server_port_key))) {
            loadPortFromPreference(sharedPreferences);
        } else if (key.equals(getString(R.string.pref_alias_key))) {
            loadAliasFromPreference(sharedPreferences);
        } else if (key.equals(getString(R.string.pref_vehicle_key))) {
            loadVehicleFromPreference(sharedPreferences);
        } else {
            Timber.w(("unsupported preference"));
        }

    }

    private void changeTextSize(Float i) {
        textViewId.setTextSize(i);
        textViewGpsLat.setTextSize(i);
        textViewGpsTs.setTextSize(i);
        textViewGpsLon.setTextSize(i);
        textViewHttpPostTs.setTextSize(i);
        buttonStart.setTextSize(i);
    }

    private void changeURL(String url) {
        Timber.d("url: %s", url);
        user.setUrl(url);
    }

    private void changePort(String port) {
        Timber.d("port: %s", port);
        user.setPort(port);
    }

    private void changeAlias(String alias) {
        Timber.d("alias: %s", alias);
        user.setAlias(alias);
    }

    private void changeVehicle(String vehicle) {
        Timber.d("vehicle: %s", vehicle);
        user.setVehicle(vehicle);
    }

    private void loadTextSizeFromPreference(SharedPreferences sharedPreferences) {
        float textSize = Float.parseFloat(sharedPreferences.getString(getString(R.string.pref_text_size_key), getString(R.string.pref_text_size_default)));
        Timber.d("textSize: %s", textSize);
        changeTextSize(textSize);
    }

    private void loadURLFromPreference(SharedPreferences sharedPreferences) {
        String url = sharedPreferences.getString(getString(R.string.pref_server_address_key), getString(R.string.pref_server_address_default));
        Timber.d("url: %s", url);
        changeURL(url);
    }

    private void loadPortFromPreference(SharedPreferences sharedPreferences) {
        String port = sharedPreferences.getString(getString(R.string.pref_server_port_key), getString(R.string.pref_server_port_default));
        Timber.d("port: %s", port);
        changePort(port);
    }

    private void loadAliasFromPreference(SharedPreferences sharedPreferences) {
        String alias = sharedPreferences.getString(getString(R.string.pref_alias_key), getString(R.string.pref_alias_default));
        Timber.d("alias: %s", alias);
        changeAlias(alias);
    }

    private void loadVehicleFromPreference(SharedPreferences sharedPreferences) {
        String vehicle = sharedPreferences.getString(getString(R.string.pref_vehicle_key), getString(R.string.pref_vehicle_default));
        Timber.d("vehicle: %s", vehicle);
        changeVehicle(vehicle);
    }

}
