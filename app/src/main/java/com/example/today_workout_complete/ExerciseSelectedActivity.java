package com.example.today_workout_complete;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class ExerciseSelectedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        Intent exerciseSelectedIntent = getIntent();


        // exercise 에서 버튼
        ImageButton exerciseButton1 = findViewById(R.id.exerciseButton1);
        exerciseButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExerciseSelectedActivity.this, Exercise_list.class);
                intent.putExtra("isAddExercise", exerciseSelectedIntent.getBooleanExtra("isAddExercise", false));
                intent.putExtra("routinPosition", exerciseSelectedIntent.getIntExtra("routinPosition", 0));
                startActivity(intent);
            }
        });
        ImageButton exerciseButton2 = findViewById(R.id.exerciseButton2);
        exerciseButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExerciseSelectedActivity.this, Exercise_list.class);
                intent.putExtra("isAddExercise", exerciseSelectedIntent.getBooleanExtra("isAddExercise", false));
                intent.putExtra("routinPosition", exerciseSelectedIntent.getIntExtra("routinPosition", 0));
                startActivity(intent);
            }
        });

        // navigation
        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                startActivity(intent);
            }
        });
        ImageButton exerciseButton = findViewById(R.id.exerciseButton);
        exerciseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WorkoutActivity.class);
                startActivity(intent);
            }
        });
        ImageButton calenderButton = findViewById(R.id.calenderButton);
        calenderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CalenderActivity.class);
                startActivity(intent);
            }
        });
        ImageButton myPageButton = findViewById(R.id.myPageButton);
        myPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                intent.putExtra("url", "http://118.67.132.81:8080/mypage");
                startActivity(intent);
            }
        });
    }
}