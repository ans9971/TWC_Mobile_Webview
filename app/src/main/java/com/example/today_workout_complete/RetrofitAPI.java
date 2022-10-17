package com.example.today_workout_complete;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RetrofitAPI {
    public static final String API_URL = "http://118.67.132.81/:8080";
    @GET("/api/calendarEmgDate")
    Call<List<UserInfo>> getData(@Query("nickname") String nickname);
}
