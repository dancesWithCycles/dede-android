package org.dedriver.dede.rest;

import org.dedriver.dede.model.LocationData;
import org.dedriver.dede.model.LocationResult;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiInterface {
    String URL = "https://dedriver.org:42001/";

    @POST("postdata")
    Call<LocationResult> getStringScalar(@Body LocationData body);
}