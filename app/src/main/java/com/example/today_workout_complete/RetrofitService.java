package com.example.today_workout_complete;

import javax.xml.transform.Result;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RetrofitService {

    @GET("api/calendarEmgDate/{nickname}") // url을 제외한 End Point
    Call<Result> getResults(@Path("nickname") String body); // get방식

//    @POST("posts/post")
//    Call<Result> postInfo(@Header("token") String token, @Body String body); // post방식

}