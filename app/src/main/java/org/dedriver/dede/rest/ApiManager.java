package org.dedriver.dede.rest;

import org.dedriver.dede.model.LocationData;
import org.dedriver.dede.model.LocationResult;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import timber.log.Timber;

public class ApiManager {

    private static ApiInterface service;
    private static ApiManager apiManager;

    private ApiManager() {
        Timber.d("ApiManager started");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiInterface.URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(ApiInterface.class);
        Timber.d("ApiManager finished");
    }

    public static ApiManager getInstance() {
        Timber.d("getInstance started");
        if (apiManager == null) {
            apiManager = new ApiManager();
        }
        Timber.d("getInstance finished");
        return apiManager;
    }

    public void createLocation(LocationData location, Callback<LocationResult> callback) {
        Timber.d("createLocation started");
        Call<LocationResult> call = service.getStringScalar(location);
        call.enqueue(callback);
        Timber.d("createLocation finished");
    }
}