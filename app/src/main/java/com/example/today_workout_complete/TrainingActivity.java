package com.example.today_workout_complete;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TrainingActivity extends AppCompatActivity {
    private String TAG = TrainingActivity.class.getSimpleName();

    private ListView exerciseListView;
    private exerciseListViewAdapter adapter;
    private List<LinearLayout> editLinearLayoutList;
    private List<Button> exerciseDeleteButtonList;
    private RoutinJsonArray routinJsonArray;
    private int routinPosition = 0;
    private SharedPreferences spref;
    private SharedPreferences.Editor editor;
    private float preCurX = 0f;
    private int prePosition = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        MenuFragment menuFragment = new MenuFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.menuFragmentFrame, menuFragment).commit();

        Intent intent = getIntent();
        routinPosition = intent.getIntExtra("routinPosition", 0);
        routinJsonArray = RoutinJsonArray.getInstance();

        spref = getSharedPreferences(WorkoutActivity.MY_ROUTIN_PREFS_NAME, Context.MODE_PRIVATE);
        editor = spref.edit();

        Log.d(TAG, "routin position: " + routinPosition);
        Log.d(TAG, "routin JsonArray: " + routinJsonArray.stringfyRoutinArray());

        TextView trainingTextView = (TextView) findViewById(R.id.trainingTextView);

        try {
            String traingingText = routinJsonArray.getRoutin(routinPosition).getString("routinName");
            if(traingingText != null) trainingTextView.setText(traingingText);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        editLinearLayoutList = new ArrayList<>();
        exerciseDeleteButtonList= new ArrayList<>();
        exerciseListView = (ListView) findViewById(R.id.exerciseListView);
        adapter = new exerciseListViewAdapter();

        try {
            for(int i = 0; i < routinJsonArray.getRoutin(routinPosition).getJSONArray("exercises").length(); i++) adapter.addItem(routinJsonArray.getExercise(routinPosition, i));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        exerciseListView.setAdapter(adapter);
    }

    public class exerciseListViewAdapter extends BaseAdapter {
        ArrayList<JSONObject> exerciseItems = new ArrayList<>();

        @Override
        public int getCount() {
            return exerciseItems.size();
        }

        public void addItem(JSONObject item) {
            exerciseItems.add(item);
        }

        @Override
        public Object getItem(int position) {
            return exerciseItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            final Context context = viewGroup.getContext();
            final JSONObject exerciseItem = exerciseItems.get(position);

            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listview_list_exercise, viewGroup, false);

            } else {
                View view = new View(context);
                view = (View) convertView;
            }


            TextView exerciseNameTextView = (TextView) convertView.findViewById(R.id.exerciseNameTextView);
            TextView exerciseInfomationTextView = (TextView) convertView.findViewById(R.id.exerciseInfomationTextView);
            EditText setEditTextNumber =  (EditText) convertView.findViewById(R.id.setEditTextNumber);
            EditText repsEditTextNumber =  (EditText) convertView.findViewById(R.id.repsEditTextNumber);
            EditText breakTimeEditTextNumber =  (EditText) convertView.findViewById(R.id.breakTimeEditTextNumber);
            Button exerciseDeleteButton = (Button) convertView.findViewById(R.id.exerciseDeleteButton);

            editLinearLayoutList.add((LinearLayout) convertView.findViewById(R.id.editLinearLayout));
            exerciseDeleteButtonList.add(exerciseDeleteButton);

            // 운동 삭제
            exerciseDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder routinDeleteAlert = new AlertDialog.Builder(TrainingActivity.this);

                    String title = "정말 삭제하시겠습니까?";
                    routinDeleteAlert.setTitle(title);

                    routinDeleteAlert.setIcon(R.drawable.ic_baseline_fitness_center_24);
                    routinDeleteAlert.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            routinJsonArray.deleteExercise(routinPosition, position);
                            Log.d(TAG, routinJsonArray.stringfyRoutinArray());
                            editor.putString(WorkoutActivity.MY_ROUTIN_PREFS_NAME, routinJsonArray.stringfyRoutinArray());
                            editor.commit();
                            adapter = new exerciseListViewAdapter();
                            try {
                                for(int j = 0; j < routinJsonArray.getRoutin(routinPosition).getJSONArray("exercises").length(); j++){
                                    adapter.addItem(routinJsonArray.getExercise(routinPosition, j));
                                }
                            } catch (JSONException e){
                                Log.d(TAG, e.toString());
                            }
                            exerciseListView.setAdapter(adapter);
                            Toast.makeText(getApplicationContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    routinDeleteAlert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(getApplicationContext(), "취소되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    routinDeleteAlert.show();
                }
            });

            // 스크롤 제스처로 삭제 버튼 visible 제어
            convertView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if(motionEvent.getAction() == MotionEvent.ACTION_MOVE){
                        float curX = motionEvent.getX();
                        float curY = motionEvent.getY();
                        Log.d(TAG, "손가락 움직임 : " + prePosition + "  " + preCurX+", "+curX+", "+curY);
                        if(prePosition != position){
                            preCurX = curX;
                            prePosition = position;
                            return false;
                        }
                        if(preCurX > curX){
                            exerciseDeleteButton.setVisibility(View.VISIBLE);
                        } else if(preCurX < curX) {
                            exerciseDeleteButton.setVisibility(View.GONE);
                        }
                        preCurX = curX;
                        prePosition = position;
                    }
                    return false;
                }
            });

            try {
                exerciseNameTextView.setText(exerciseItem.getString("exerciseName"));
                exerciseInfomationTextView.setText(exerciseItem.getInt("setCount") + "세트 세트당 " + exerciseItem.getJSONArray("reps").get(0) + "회 휴식시간 " + exerciseItem.getInt("breakTime") + "초");
                setEditTextNumber.setText(exerciseItem.getInt("setCount") + "");
                repsEditTextNumber.setText(exerciseItem.getJSONArray("reps").get(0) + "");
                breakTimeEditTextNumber.setText(exerciseItem.getInt("breakTime") + "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            setEditTextNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if(!hasFocus){
                        Log.d(TAG, "setEditTextNumber " + setEditTextNumber.getText().toString());
                        try {
                            routinJsonArray.updateSetCount(routinPosition, position, Integer.parseInt(setEditTextNumber.getText().toString()));
                            editor.putString(WorkoutActivity.MY_ROUTIN_PREFS_NAME, routinJsonArray.stringfyRoutinArray());
                            editor.commit();
                        } catch (NumberFormatException | JSONException e){
                            Log.d(TAG, e.toString());
                        }
                    }
                }
            });
            repsEditTextNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if(!hasFocus){
                        Log.d(TAG, "repsEditTextNumber " + repsEditTextNumber.getText().toString());
                        try {
                            routinJsonArray.updateAllReps(routinPosition, position, Integer.parseInt(repsEditTextNumber.getText().toString()));
                            editor.putString(WorkoutActivity.MY_ROUTIN_PREFS_NAME, routinJsonArray.stringfyRoutinArray());
                            editor.commit();
                        } catch (NumberFormatException | JSONException e){
                            Log.d(TAG, e.toString());
                        }
                    }
                }
            });
            breakTimeEditTextNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if(!hasFocus){
                        Log.d(TAG, "breakTimeEditTextNumber " + breakTimeEditTextNumber.getText().toString());
                        try {
                            routinJsonArray.updateBrakeTime(routinPosition, position, Integer.parseInt(breakTimeEditTextNumber.getText().toString()));
                            editor.putString(WorkoutActivity.MY_ROUTIN_PREFS_NAME, routinJsonArray.stringfyRoutinArray());
                            editor.commit();
//                            routin.getExercises().get(position).setBreak_time(Integer.parseInt(breakTimeEditTextNumber.getText().toString()));
                        } catch (NumberFormatException | JSONException e){
                            Log.d(TAG, e.toString());
                        }
                    }
                }
            });
            try {
                Log.d(TAG, "getView() - [ "+position+" ] " + exerciseItem.getString("exerciseName"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //각 아이템 선택 event
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Toast.makeText(context, exerciseItem.getString("exerciseName")+" 입니다! ", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            return convertView;  //뷰 객체 반환
        }
    }

    public void onClickBackButton(View view){
        finish();
    }

    public void onClickEditButton(View view) throws JSONException {
        Button editbutton = (Button) view;
        if(editbutton.getText().equals("편집")){
            editbutton.setText("완료");
            for(int i = 0; i < editLinearLayoutList.size(); i++){
                editLinearLayoutList.get(i).setVisibility(View.VISIBLE);
                exerciseDeleteButtonList.get(i).setVisibility(View.VISIBLE);
            }
        } else {
            Log.d(TAG, "onClickEditButton");
            editbutton.setText("편집");
            for(int i = 0; i < editLinearLayoutList.size(); i++){
                editLinearLayoutList.get(i).setVisibility(View.GONE);
                exerciseDeleteButtonList.get(i).setVisibility(View.GONE);
            }
            editLinearLayoutList = new ArrayList<>();
            exerciseDeleteButtonList = new ArrayList<>();
            adapter = new exerciseListViewAdapter();
            for(int i = 0; i < routinJsonArray.getRoutin(routinPosition).getJSONArray("exercises").length(); i++){
                adapter.addItem(routinJsonArray.getExercise(routinPosition, i));
            }
            exerciseListView.setAdapter(adapter);
        }
    }
    public void onClickAddExercise(View view){
        Intent intent = new Intent(TrainingActivity.this, ExerciseSelectedActivity.class);
        intent.putExtra("isAddExercise", true);
        intent.putExtra("routinPosition", routinPosition);
        startActivity(intent);
    }

    public void onClickGoToExerciseButton(View view){
        Intent intent = new Intent(TrainingActivity.this, WorkoutTrackerActivity.class);
        intent.putExtra("routinPosition", routinPosition);
        startActivity(intent);
    }
}