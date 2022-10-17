package com.example.today_workout_complete;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private String TAG = "VideoActivity";
    private VideoView videoView;
    private SharedPreferences spref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spref = getSharedPreferences(WebViewActivity.MY_NICKNAME_PREFS_NAME, Context.MODE_PRIVATE);
        editor = spref.edit();
        String nickname = spref.getString(WebViewActivity.MY_NICKNAME_PREFS_NAME, "");
        Log.d(TAG, "nickname: " + nickname);

        if(!nickname.equals("")){
            Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
            startActivity(intent);
        }

//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://118.67.132.81/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
//        retrofitAPI.getData("nickname").enqueue(new Callback<List<UserInfo>>(){ // 생성한 인터페이스 getData 에 찾을 id를 넣고 enqueue 해주기
//
//             @Override
//             public void onResponse(Call<List<UserInfo>> call, Response<List<UserInfo>> response){ // 성공적으로 응답하면 실행
//
//                 if(response.isSuccessful()){
//                     List<UserInfo> data = response.body();
//                     Log.d("TEST","성공했나?");
////                     Log.d("TEST",data.get(0).getTitle());
//                     //data 에서 필요한 내용 꺼내 쓰기
//                 }
//             }
//
//             @Override
//             public void onFailure(Call<List<UserInfo>> call, Throwable t){
//                 //실패하면 실행
//                 Log.d("TEST","실패?");
//                 t.printStackTrace();
//             }
//        });

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        videoView = (VideoView) findViewById(R.id.video_view);

        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.test));

        videoView.start();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
            public void onPrepared(MediaPlayer mp){
                mp.setLooping(true);
            }
        });

        Button join_button = findViewById(R.id.joinButton);
        join_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                intent.putExtra("url", "http://118.67.132.81:8080/login/join");
                startActivity(intent);
            }
        });
        Button login_button = findViewById(R.id.loginButton);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                intent.putExtra("url", "http://118.67.132.81:8080/login");
                startActivity(intent);
            }
        });
    }
}