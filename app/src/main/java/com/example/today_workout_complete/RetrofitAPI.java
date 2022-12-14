package com.example.today_workout_complete;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RetrofitAPI {
    public static final String API_URL = "http://118.67.132.81/:8080";
    @GET("/api/sendEmgData")
    Call<List<EmgData>> getEmgData(@Query("Year") int Year,@Query("nickname") String nickname,
                                   @Query("Month") int Month, @Query("Day") int Day);

    @GET("/api/calendarEmgDate")
    Call<List<UserInfo>> getData(@Query("nickname") String nickname);

    @GET("/api/sendEmgData")
    Call<List<EmgData>> getEmgData(@Query("nickname") String nickname);

    @POST("/api/myPage/emgData")
    Call<List<EmgData>> postEmgData(@Body EmgData emgData);
}
