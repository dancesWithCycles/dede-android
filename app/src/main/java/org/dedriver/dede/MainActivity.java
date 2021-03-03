package org.dedriver.dede;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    /*Remember to (1) make the HTTP client static to avoid huge numbers of coincident sockets.*/
    public static final HttpClient httpClient = new DefaultHttpClient();

    public static final User user = new User();
    private static final int GPS_PERMISSION_CODE = 1;

    public static Location gpsLocation = null;

    public static boolean threadPostRunning = true;
    protected static TextView textViewHttpPostTs = null;
    private static String buttonStartText = "Start";
    private static TextView textViewId = null;
    private static TextView textViewGpsLat = null;
    private static TextView textViewGpsLon = null;
    private static TextView textViewGpsTs = null;
    private static Button buttonStart = null;
    private final PostLocation postLocation = new PostLocation();
    public View.OnClickListener StartOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            Timber.d("buttonStart: %s", buttonStart.getText());
//            Timber.d("getString(R.string.button_start_start): %s", getString(R.string.button_start_start));
            if (buttonStart.getText() == "Start") {
                /*start service LocationUpdateService*/
                if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        checkBackgroundLocation()) {
                    final Intent intent = new Intent(MainActivity.this, LocationUpdateService.class);
                    ComponentName componentName;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        componentName = startForegroundService(intent);
                    } else {
                        componentName = startService(intent);
                    }

                    /*launch location post service*/
                    Thread threadPost = new Thread(postLocation);
                    threadPost.start();
                    threadPostRunning = true;
                    buttonStart.setText("Stop");
                    buttonStartText = "Stop";
                } else {
                    Timber.w("onClick: permissions not granted");
                    requestPermission();
                }
            } else if (buttonStart.getText() == "Stop") {
                final Intent intent = new Intent(MainActivity.this, LocationUpdateService.class);
                stopService(intent);

                /*release location post service*/
                threadPostRunning = false;
                buttonStart.setText("Start");
                buttonStartText = "Start";
            } else {
                Timber.w("start button showing unexpected behavior");
            }
        }
    };

    static void insertData(final String uuid, final double latitude, final double longitude,
                           final long timestamp, final String alias, final String vehicle) {

        class PostJsonAsyncTask extends AsyncTask<String, Void, String> {
            private final Handler handlerMain = new Handler(Looper.getMainLooper());

            @Override
            protected String doInBackground(String... strings) {

                /*create JSON object*/
                JSONObject postData = new JSONObject();
                try {
                    postData.put("uuid", uuid);
                    postData.put("latitude", latitude);
                    postData.put("longitude", longitude);
                    postData.put("timestamp", timestamp);
                    postData.put("alias", alias);
                    postData.put("vehicle", vehicle);
//
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (httpClient == null) {
                }

                String address = user.getUrl() + ":" + user.getPort() + user.getRoute();
//                Timber.d("address: %s",address);
                HttpPost httpPostRequest = new HttpPost(address);

                if (httpPostRequest != null) {
                    /*todo string or byte entity?*/
                    HttpEntity entity = null;
                    try {
                        entity = new ByteArrayEntity(postData.toString().getBytes("UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        Timber.e("doInBackground: HTTP entity unavailable: %s", e);
                        e.printStackTrace();
                    }

                    if (entity != null) {
                        httpPostRequest.setHeader("Content-Type", "application/json");
                        httpPostRequest.setEntity(entity);

                        HttpResponse httpResponse = null;
                        try {
                            httpResponse = httpClient.execute(httpPostRequest);
                        } catch (IOException e) {
                            Timber.e("doInBackground: execute post failed");
                            e.printStackTrace();
                            return "execute post failed";
                        }
                        /*TODO Why is it necessary to consume response?*/
                        HttpEntity entityRsp = httpResponse.getEntity();
                        if (entityRsp != null) {
                            try {
                                entityRsp.consumeContent();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        handlerMain.post(new Runnable() {
                            @Override
                            public void run() {
                                textViewHttpPostTs.setText("Latest POST Timestamp: " + timestamp);
                            }
                        });
                        return "Data Inserted Successfully";
                    } else {
                        Timber.w("doInBackground: HTTP entity unavailable");
                        return "HTTP entity unavailable";
                    }
                } else {
                    Timber.w("doInBackground: http post instance unavailable");
                    return "http post instance unavailable";
                }
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                Timber.d("result: %s", result);
            }
        }

        PostJsonAsyncTask postJsonAsyncTask = new PostJsonAsyncTask();
        /*todo Why is id/uuid excluded in the following argument list?*/
        postJsonAsyncTask.execute(Double.toString(latitude), Double.toString(longitude), Long.toString(timestamp), alias);
    }

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

        textViewId.setText(getString(R.string.text_view_id_text) + user.getUuid().getUuid());
        textViewHttpPostTs.setText(getString(R.string.text_view_http_post_ts_text));
        buttonStart.setOnClickListener(StartOnClickListener);
        buttonStart.setText(buttonStartText);

        loadTextSizeFromPreference(PreferenceManager.getDefaultSharedPreferences(this));
    }

    /*TODO boilerplate and copy code is ugly. Act! */
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestPermissions(new String[]
                        {Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);
            } else {
                requestPermissions(new String[]
                        {Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

            }
        }
    }

    /*TODO boilerplate and copy code is ugly. Act! */
    public boolean checkBackgroundLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ActivityCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
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
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        /*load preferences from XML
         * do not override user settings*/
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        /*register preferences*/
        setupSharedPreferences();

        initViews();

        /*set up logging*/
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    private void setupSharedPreferences() {
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
