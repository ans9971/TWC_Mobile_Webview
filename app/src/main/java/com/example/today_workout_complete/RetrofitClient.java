package com.example.today_workout_complete;

import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://118.67.132.81:3000/";
    private static RetrofitAPI retrofitAPI = null;

    public RetrofitAPI getInstance(){
        if(retrofitAPI == null){
            retrofitAPI = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()))
                    .build().create(RetrofitAPI.class);
        }
        return retrofitAPI;
    }
}

//    private static Retrofit getInstance(){
//        Gson gson = new GsonBuilder().setLenient().create();
//        return new Retrofit.Builder()
//                .baseUrl(BASE_URL)
//                .addConverterFactory(GsonConverterFactory.create(gson))
//                .build();
//    }

