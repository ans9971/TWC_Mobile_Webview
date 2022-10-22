package com.example.today_workout_complete;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorkoutActivity extends AppCompatActivity {
    private String TAG = WorkoutActivity.class.getSimpleName();

    private ListView routinListView = null;
    private workoutListViewAdapter adapter = null;
    public static final String MY_ROUTIN_PREFS_NAME = "MyRoutinPrefsFile";
    private SharedPreferences spref;
    private SharedPreferences.Editor editor;
    private RoutinJsonArray routinJsonArray = null;
    private float preCurX = 0f;
    private int prePosition = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        MenuFragment menuFragment = new MenuFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.menuFragmentFrame, menuFragment).commit();

        routinListView = (ListView) findViewById(R.id.routinListView);
        adapter = new workoutListViewAdapter();

        // 루틴 프리퍼런스 가져오기
        spref = getSharedPreferences(MY_ROUTIN_PREFS_NAME, Context.MODE_PRIVATE);
        editor = spref.edit();

        routinJsonArray = RoutinJsonArray.getInstance();
        Log.d(TAG, "routinJsonArray.getRoutinArray().length(): " + routinJsonArray.getRoutinArray().length());

        if(routinJsonArray.getRoutinArray().length() < 1){
            String routinJSonArrayString = spref.getString(MY_ROUTIN_PREFS_NAME, "");

            Log.d(TAG, routinJSonArrayString);
            if(routinJSonArrayString.equals("") || !routinJSonArrayString.startsWith("[") || routinJSonArrayString.length() < 10){
                Log.d(TAG, "없음");
                //      임시 루틴 데이터 생성 및 Adapter 안에 데이터 담는 코드
                Random random = new Random();
                String[] strList = {"push_up", "dips", "pull_up", "chin_up"};
                String[] routinNameList = {"front", "back", "side", "under"};

                for(int i = 0; i < 4; i++){
                    List<Exercise> exerciseList = new ArrayList<>();
                    ArrayList<Integer> repsList = new ArrayList<>();
                    int setCount = random.nextInt(10) + 2;
                    for (int k = 0; k < setCount; k++) repsList.add(random.nextInt(7));
                    for(int j = 0; j < 4; j++)  exerciseList.add(new Exercise(strList[j], setCount, repsList, random.nextInt(100), "chest"));
                    routinJsonArray.addRotin(new Routin(routinNameList[i], exerciseList, random.nextInt(10), random.nextInt(10000000) + ""));
                }

                Log.d(TAG, routinJsonArray.stringfyRoutinArray());
                editor.putString(MY_ROUTIN_PREFS_NAME, routinJsonArray.stringfyRoutinArray());
                editor.commit();
            } else {
                try {
                    RoutinJsonArray.setInstance(new JSONArray(routinJSonArrayString));
                    routinJsonArray = RoutinJsonArray.getInstance();
                    Log.d(TAG, "존재!");
                    Log.d(TAG, routinJsonArray.stringfyRoutinArray());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        for(int i = 0; i < routinJsonArray.getRoutinArray().length(); i++) adapter.addItem(routinJsonArray.getRoutin(i));

        // 리스트뷰에 Adapter 설정
        routinListView.setAdapter(adapter);
    }
    public class workoutListViewAdapter extends BaseAdapter {
        ArrayList<JSONObject> routinItems = new ArrayList<>();

        @Override
        public int getCount() {
            return routinItems.size();
        }

        public void addItem(JSONObject item) {
            routinItems.add(item);
        }

        @Override
        public Object getItem(int position) {
            return routinItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            final Context context = viewGroup.getContext();
            final JSONObject routinItem = routinItems.get(position);

            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listview_list_routin, viewGroup, false);
            } else {
                View view = new View(context);
                view = (View) convertView;
            }

            LinearLayout routinModifyLinearLayout = (LinearLayout) convertView.findViewById(R.id.routinModifyLinearLayout);
            TextView routinNameTextView = (TextView) convertView.findViewById(R.id.routinNameTextView);
            TextView exerciseCountTextView = (TextView) convertView.findViewById(R.id.exerciseCountTextView);
            TextView lastExerciseDateTextView = (TextView) convertView.findViewById(R.id.lastExerciseDateTextView);
            Button routinNameModifyButton = (Button) convertView.findViewById(R.id.routinNameModifyButton);
            Button routinDeleteButton = (Button) convertView.findViewById(R.id.routinDeleteButton);
            routinModifyLinearLayout.setVisibility(View.GONE);

            // 루틴 이름 변경
            routinNameModifyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder routinNameModifyAlert = new AlertDialog.Builder(WorkoutActivity.this);
                    EditText routinNameEditText = new EditText(WorkoutActivity.this);

                    routinNameModifyAlert.setView(routinNameEditText);
                    String title = "변경할 루틴 이름을 입력해주세요";
                    routinNameModifyAlert.setTitle(title);

                    routinNameModifyAlert.setIcon(R.drawable.ic_baseline_fitness_center_24);
                    routinNameModifyAlert.setPositiveButton("변경", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String newRoutinName = routinNameEditText.getText().toString();

                            routinJsonArray.modifyRoutinName(position, routinNameTextView.getText().toString(), newRoutinName);
                            Log.d(TAG, routinJsonArray.stringfyRoutinArray());
                            editor.putString(MY_ROUTIN_PREFS_NAME, routinJsonArray.stringfyRoutinArray());
                            editor.commit();
                            routinNameTextView.setText(newRoutinName);
                            Toast.makeText(getApplicationContext(), "변경되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    routinNameModifyAlert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(getApplicationContext(), "취소되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    routinNameModifyAlert.show();
                }
            });

            // 루틴 삭제
            routinDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder routinDeleteAlert = new AlertDialog.Builder(WorkoutActivity.this);

                    String title = "정말 삭제하시겠습니까?";
                    routinDeleteAlert.setTitle(title);

                    routinDeleteAlert.setIcon(R.drawable.ic_baseline_fitness_center_24);
                    routinDeleteAlert.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            routinJsonArray.deleteRoutin(position);
                            Log.d(TAG, routinJsonArray.stringfyRoutinArray());
                            editor.putString(MY_ROUTIN_PREFS_NAME, routinJsonArray.stringfyRoutinArray());
                            editor.commit();
                            adapter = new workoutListViewAdapter();
                            for(int j = 0; j < routinJsonArray.getRoutinArray().length(); j++) adapter.addItem(routinJsonArray.getRoutin(j));
                            routinListView.setAdapter(adapter);
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

            // 스크롤 제스처로 수정/삭제 버튼 visible 제어
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
                            routinModifyLinearLayout.setVisibility(View.VISIBLE);
                        } else if(preCurX < curX) {
                            routinModifyLinearLayout.setVisibility(View.GONE);
                        }
                        preCurX = curX;
                        prePosition = position;
                    }
                    return false;
                }
            });
            try {
                routinNameTextView.setText(routinItem.getString("routinName"));
                exerciseCountTextView.setText("운동 개수: " + routinItem.getJSONArray("exercises").length() + ", 운동 시간: " + routinItem.getInt("lastExerciseTimeTook"));
                lastExerciseDateTextView.setText("마지막 운동 날짜: "+ routinItem.getString("lastExerciseDate"));
                Log.d(TAG, "getView() - [ "+position+" ] "+routinItem.getString("routinName"));
            } catch (JSONException e) {
                e.printStackTrace();
            }


            //각 아이템 선택 event
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Toast.makeText(context, routinItem.getString("routinName") +" 입니다! ", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(WorkoutActivity.this, TrainingActivity.class);
                    intent.putExtra("routinPosition", position);
                    startActivity(intent);
                }
            });
            return convertView;  //뷰 객체 반환
        }
    }

    public void onClickAddRoutin(View view){
        Intent intent = new Intent(WorkoutActivity.this, ExerciseSelectedActivity.class);
        startActivity(intent);
    }
}