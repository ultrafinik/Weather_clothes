package com.example.myapplication.Api;

import com.example.myapplication.model.WeatherAll;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface JSONPlaceHolderApi {
    @GET("weather")
    public Call<WeatherAll> getWeatherByCity(@Query("q") String city,@Query("appid") String appid,@Query("units") String units,@Query("lang") String lang);
}
