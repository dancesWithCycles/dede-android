package org.dedriver.dede;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import timber.log.Timber;

public class User {
    private final Uuid uuid;
    private final String route;
    private String url;
    private String port;
    private String alias;
    private String vehicle;

    public User() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(GlobalApplication.getContext());

        this.uuid = new Uuid(GlobalApplication.getContext());

        this.url = sharedPreferences.getString(
                GlobalApplication.getContext().getString(R.string.pref_server_address_key),
                GlobalApplication.getContext().getString(R.string.pref_server_address_default));

        this.port = sharedPreferences.getString(
                GlobalApplication.getContext().getString(R.string.pref_server_port_key),
                GlobalApplication.getContext().getString(R.string.pref_server_port_default));

        this.route = sharedPreferences.getString(
                GlobalApplication.getContext().getString(R.string.pref_server_route_key),
                GlobalApplication.getContext().getString(R.string.pref_server_route_default));

        this.alias = sharedPreferences.getString(
                GlobalApplication.getContext().getString(R.string.pref_alias_key),
                GlobalApplication.getContext().getString(R.string.pref_alias_default));

        this.vehicle = sharedPreferences.getString(
                GlobalApplication.getContext().getString(R.string.pref_vehicle_key),
                GlobalApplication.getContext().getString(R.string.pref_vehicle_default));

        Timber.d("url: %s, port: %s, route: %s, alias: %s, vehicle: %s", url, port, route, alias, vehicle);
    }

    public String getRoute() {
        return this.route;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getVehicle() {
        return this.vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public Uuid getUuid() {
        return uuid;
    }

}
