package com.example.today_workout_complete;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;

public class WebViewActivity extends AppCompatActivity {
    /*
    * Login, JOin, MainPage, MyPage, Community
    */
    private String TAG = WorkoutTrackerActivity.class.getSimpleName();
    private LinearLayout menuBarLinearLayout;
    public static final String MY_NICKNAME_PREFS_NAME = "MyNicknamePrefsFile";
    private SharedPreferences spref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        spref = getSharedPreferences(MY_NICKNAME_PREFS_NAME, Context.MODE_PRIVATE);
        editor = spref.edit();

        WebView webView = (WebView) findViewById(R.id.webView);
        WebSettings settings = webView.getSettings();
        webView.getSettings().setJavaScriptEnabled(true);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.setNetworkAvailable(true);
        settings.setDatabaseEnabled(true);
        File dir = getCacheDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        settings.setAppCachePath(dir.getPath());
        settings.setAppCacheEnabled(true);

        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        //Denied starting an intent without a user gesture, URI ~~ 에러 해결 위해 필요
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d(TAG, consoleMessage.message() + '\n' + consoleMessage.messageLevel() + '\n' + consoleMessage.sourceId());
                if(consoleMessage.message().contains("nickname:")){
                    Log.d(TAG, consoleMessage.message().substring(9));
                    editor.putString(MY_NICKNAME_PREFS_NAME, consoleMessage.message().substring(9));
                    editor.commit();
                    menuBarLinearLayout.setVisibility(View.VISIBLE);
                } else if(consoleMessage.message().contains("logout")){
                    editor.putString(MY_NICKNAME_PREFS_NAME, "");
                    editor.commit();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
                return super.onConsoleMessage(consoleMessage);
            }
        });

        webView.getSettings().setLoadWithOverviewMode(true);

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        if(url == null) {
            url = "http://118.67.132.81:8080";
        } else if(url.equals("http://118.67.132.81:8080/login") || url.equals("http://118.67.132.81:8080/login/join")){
            menuBarLinearLayout = findViewById(R.id.menuBarlinearLayout);
            menuBarLinearLayout.setVisibility(View.GONE);
        }
        Log.d(TAG, "URL: " + url);

        webView.loadUrl(url);

        // navigation
        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "HOME", Toast.LENGTH_SHORT);
                webView.loadUrl("http://118.67.132.81:8080");
            }
        });

        ImageButton exerciseButton = findViewById(R.id.exerciseButton);
        exerciseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WebViewActivity.this, WorkoutActivity.class);
                startActivity(intent);
            }
        });
        ImageButton calenderButton = findViewById(R.id.calenderButton);
        calenderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WebViewActivity.this, CalenderActivity.class);
                startActivity(intent);
            }
        });
        ImageButton myPageButton = findViewById(R.id.myPageButton);
        myPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG", "mypage");
                webView.loadUrl("http://118.67.132.81:8080/mypage");
            }
        });
    }
}