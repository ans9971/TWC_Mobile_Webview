package com.example.today_workout_complete;
//package kr.co.company.listview01;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class Exercise_list extends AppCompatActivity {
    private String TAG = WorkoutActivity.class.getSimpleName();

    ListView list;
    String[] titles = {
            "push_up",
            "dips",
            "pull_up",
            "chin_up"
    };
    Integer[] images = {
            R.drawable.ic_baseline_person_outline_24,
            R.drawable.ic_launcher_background,
            R.drawable.ic_baseline_home_24,
            R.drawable.ic_baseline_fitness_center_24,
            R.drawable.ic_baseline_person_outline_24,
            R.drawable.ic_launcher_background,
            R.drawable.ic_baseline_home_24,
            R.drawable.ic_baseline_fitness_center_24,
    };
    private RoutinJsonArray routinJsonArray;
    private SharedPreferences spref;
    private SharedPreferences.Editor editor;
    private boolean isSelecttingExercise;
    private ArrayList<String> exercises;
    private int DEFAULT_SET_COUNT = 3;
    private int DEFAULT_REPS = 5;
    private int DEFAULT_BREAK_TIME = 50;
    private Boolean isAddExercise;
    private int routinPosition;
    private String measuredMuscle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_list);

        MenuFragment menuFragment = new MenuFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.menuFragmentFrame, menuFragment).commit();

        Intent intent = getIntent();
        isAddExercise = intent.getBooleanExtra("isAddExercise", false);
        routinPosition = intent.getIntExtra("routinPosition", 0);
        measuredMuscle = intent.getStringExtra("measuredMuscle");

        CustomList adapter = new CustomList(Exercise_list.this);
        list = (ListView)findViewById(android.R.id.list);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, i + "   " + titles[i] + "  " + l);
                Log.d(TAG, "isSelecttingExercise: " + isSelecttingExercise);
                if(isSelecttingExercise){
                    exercises.add(titles[i]);
                    Toast.makeText(getApplicationContext(), titles[i] +"!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "exerciseName: " + titles[i]);
                } else {
                    Toast.makeText(getApplicationContext(), "추가하실려면 선택 버튼을 눌러주세요!", Toast.LENGTH_SHORT);
                }
            }
        });

        routinJsonArray = RoutinJsonArray.getInstance();
        spref = getSharedPreferences(WorkoutActivity.MY_ROUTIN_PREFS_NAME, Context.MODE_PRIVATE);
        editor = spref.edit();

        isSelecttingExercise = false;
        exercises = new ArrayList<>();

        // 뒤로가기 버튼
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public class CustomList extends ArrayAdapter<String>{
        private final Activity context;
        public CustomList(Exercise_list context){
            super(context, R.layout.listitem, titles);
            this.context = context;
        }
        public View getView(int position, View vie, ViewGroup parent){
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.listitem, null, true);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.image);
            TextView title = (TextView) rowView.findViewById(R.id.title);
            TextView rating = (TextView) rowView.findViewById(R.id.rating);
            title.setText(titles[position]);
            imageView.setImageResource(images[position]);
            rating.setText("9.0" + position);
            return rowView;
        }
    }

    public void onClickSelectedButton(View view){
        Button selectedButton = (Button) view;
        if(!isSelecttingExercise){
            selectedButton.setText("완료");
            isSelecttingExercise = !isSelecttingExercise;
            return;
        }
        if(exercises.size() == 0){
            Toast.makeText(getApplicationContext(), "선택된 운동이 없습니다!", Toast.LENGTH_SHORT);
        } else {
            // 루틴 이름 입력 창 띄우기
            AlertDialog.Builder routinNameSaveAlert = new AlertDialog.Builder(this);
            EditText routinNameEditText = new EditText(this);
            if(!isAddExercise) routinNameSaveAlert.setView(routinNameEditText);

            String title = isAddExercise ? "추가하시겠습끼?" : "루틴 이름을 지어주세요!";
            routinNameSaveAlert.setTitle(title);

            routinNameSaveAlert.setIcon(R.drawable.ic_baseline_fitness_center_24);
            routinNameSaveAlert.setPositiveButton("추가하기", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {           // 루틴 저장
                    ArrayList<Exercise> exercisesList = new ArrayList<>();
                    ArrayList<Integer> defaultReps = new ArrayList<>();

                    for(int j = 0; j < DEFAULT_REPS; j++) defaultReps.add(DEFAULT_REPS);
                    for(int k = 0; k < exercises.size(); k++) {
                        if(isAddExercise){
                            routinJsonArray.addExercise(routinPosition, new Exercise(exercises.get(k), DEFAULT_SET_COUNT, defaultReps, DEFAULT_BREAK_TIME, measuredMuscle));
                        } else{
                            exercisesList.add(new Exercise(exercises.get(k), DEFAULT_SET_COUNT, defaultReps, DEFAULT_BREAK_TIME, measuredMuscle));
                        }
                    }
                    if(!isAddExercise) routinJsonArray.addRotin(new Routin(routinNameEditText.getText().toString(), exercisesList, 0 , ""));

                    editor.putString(WorkoutActivity.MY_ROUTIN_PREFS_NAME, routinJsonArray.stringfyRoutinArray());
                    editor.commit();
                    String toastText = isAddExercise ? "추가되었습니다!" : routinNameEditText.getText().toString() +"루틴이 추가되었습니다!";
                    Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
                }
            });
            routinNameSaveAlert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(getApplicationContext(), "취소되었습니다.", Toast.LENGTH_SHORT).show();
                }
            });
            routinNameSaveAlert.show();
        }
        selectedButton.setText("선택");
        isSelecttingExercise = !isSelecttingExercise;
    }
}