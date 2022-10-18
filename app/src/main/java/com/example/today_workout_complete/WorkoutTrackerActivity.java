package com.example.today_workout_complete;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

public class WorkoutTrackerActivity extends AppCompatActivity implements BLEControllerListener {
    private String TAG = WorkoutTrackerActivity.class.getSimpleName();

//    private Routin routin = null;
    private RecyclerView workoutTrackerRecyclerView = null;
    private RecyclerViewAdapter recyclerViewAdapter = null;
    private ListView workoutTrackerListView = null;
    private WorkoutTrackerListViewAdapter adapter = null;
    private int exerciseSelected = 0;
    private TextView workoutTrackerRestTimeTextView;

    private LineChart chart;
    public Thread chartThread;

    static Queue<Float> queue = new LinkedList<>();
    private Float preEmgValue = 0f;

    private Button readyStartButton;
    static boolean isWorkout;

    private List<Integer> emgData;                          // set의 EMG 데이터를 저장하기 위한 Integer형 리스트
    private List<Integer> maximumValueOfSets;               // 각 emgData의 가장 큰 값들을 저장하는 Integer형 리스트
    private List<Integer> minimumValueOfSets;               // 각 emgData의 가장 작은 값들을 저장하는 Integer형 리스트

    private int setsTotal;
    private int setsCount;
    private JSONObject workoutJSON;
    private JSONArray setsSON;
    SimpleDateFormat simpleDateFormat;
    private long setsStartingTime;              // 운동 시작 시간
    private long setStartingTime;               // 세트 시작 시간
    private long breakStartingTime;             // 휴식 시작 시간

    // 블루투스 관련 변수
    private static BLEController bleController = null;
    private TextView bluetoothConnectionStatusTextView;
    private Button connectButton;
    private boolean connected;
    private String deviceAddress;

    // 웹 관련 변수
    private RequestingServer requestingServer;
    private final String myEmgDataURL = "http://118.67.132.81:3000/api/myPage/emgData";

    private int routinPosition;
    private RoutinJsonArray routinJsonArray;
    private SharedPreferences spref;
    private SharedPreferences.Editor editor;
    private String nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_tracker);

        MenuFragment menuFragment = new MenuFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.menuFragmentFrame, menuFragment).commit();

        // 블루투스 관련 코드
        readyStartButton = (Button) findViewById(R.id.readyStartButton);
        bluetoothConnectionStatusTextView = (TextView) findViewById(R.id.bluetoothTextView);
        if(bleController == null) bleController = BLEController.getInstance(this, bluetoothConnectionStatusTextView, readyStartButton);
        connectButton = findViewById(R.id.bluetoothConnectionButton);
        connected = false;
        isWorkout = false;

        checkBLESupport();
        checkPermissions();

        // JSON 초기화
        workoutJSON = new JSONObject();
        simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");

        Intent intent = getIntent();
        routinPosition = intent.getIntExtra("routinPosition", 0);
        routinJsonArray = RoutinJsonArray.getInstance();
        spref = getSharedPreferences(WorkoutActivity.MY_ROUTIN_PREFS_NAME, Context.MODE_PRIVATE);
        editor = spref.edit();
        nickname = getSharedPreferences(WebViewActivity.MY_NICKNAME_PREFS_NAME, Context.MODE_PRIVATE).getString(WebViewActivity.MY_NICKNAME_PREFS_NAME, "");

//        routin = (Routin) intent.getSerializableExtra("routin");
        ArrayList<String> exerciseNameList = new ArrayList<>();
        try {
            for(int i = 0; i < routinJsonArray.getRoutin(routinPosition).getJSONArray("exercises").length(); i++){
                exerciseNameList.add(routinJsonArray.getExercise(routinPosition, i).getString("exerciseName"));
            }
        } catch (JSONException e){
            Log.d(TAG, e.toString());
        }
        workoutTrackerRecyclerView = (RecyclerView) findViewById(R.id.workoutTrackerRecyclerView);
        recyclerViewAdapter = new RecyclerViewAdapter(exerciseNameList);
        workoutTrackerRecyclerView.setAdapter(recyclerViewAdapter);
        workoutTrackerRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        workoutTrackerListView = (ListView) findViewById(R.id.workoutTrackerListView);
        adapter = new WorkoutTrackerListViewAdapter();
        try {
            updateWorkoutTrackerListView();
        } catch (JSONException e) {
            e.printStackTrace();
        }



        // 실시간 그래프 코드
        chart = (LineChart) findViewById(R.id.chart);

        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setTextColor(Color.WHITE);
        chart.animateXY(2000, 2000);
        chart.invalidate();

        emgData = new ArrayList<>();

        LineData data = new LineData();
        chart.setData(data);

        feedMultiple();
        chartThread.start();
    }

    public void updateWorkoutTrackerListView() throws JSONException{
        adapter = new WorkoutTrackerListViewAdapter();
        // 운동 변수 초기화
        setsCount = routinJsonArray.getExercise(routinPosition, exerciseSelected).getInt("setCount");
        setsTotal = setsCount;
        for(int i = 0; i < setsCount; i++){
            adapter.addItem(routinJsonArray.getExercise(routinPosition, exerciseSelected).getJSONArray("reps").getInt(i));
        }
        workoutTrackerListView.setAdapter(adapter);

        workoutTrackerRestTimeTextView = (TextView) findViewById(R.id.workoutTrackerRestTimeTextView);
        workoutTrackerRestTimeTextView.setText("휴식시간: " + routinJsonArray.getExercise(routinPosition, exerciseSelected).getInt("breakTime"));
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView workoutTrackerRecylerTextView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                workoutTrackerRecylerTextView = (TextView) itemView.findViewById(R.id.workoutTrackerRecyclerTextView);
                workoutTrackerRecylerTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view){
                        String clikckExercise = ((TextView) view).getText().toString();
                        Log.d(TAG, clikckExercise);
                        try {
                            for(int i = 0; i < routinJsonArray.getRoutin(routinPosition).length(); i++){
                                Log.d(TAG, routinJsonArray.getExercise(routinPosition, i).getString("exerciseName"));
                                if(clikckExercise.equals(routinJsonArray.getExercise(routinPosition, i).getString("exerciseName"))){
                                    exerciseSelected = i;
                                    break;
                                }
                            }
                            updateWorkoutTrackerListView();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
        private ArrayList<String> exerciseNameList = null;

        public RecyclerViewAdapter(ArrayList<String> exerciseNameList) {
            this.exerciseNameList = exerciseNameList;
        }
        // 아이템 뷰를 위한 뷰홀더 객체를 생성하여 리턴
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.recycler_list_workout_tracker, parent, false);
            ViewHolder vh = new ViewHolder(view);
            return vh;
        }
        // position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String exerciseName = exerciseNameList.get(position);

            holder.workoutTrackerRecylerTextView.setText(exerciseName);
        }
        @Override
        public int getItemCount() {
            return exerciseNameList.size();
        }
    }

    public void onClickBackButton(View view){
        finish();
    }

    public class WorkoutTrackerListViewAdapter extends BaseAdapter {
        ArrayList<Integer> exerciseItems = new ArrayList<>();

        @Override
        public int getCount() {
            return exerciseItems.size();
        }

        public void addItem(Integer item) {
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
            final Integer reps = exerciseItems.get(position);

            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listview_list_workout_tracker, viewGroup, false);
            } else {
                View view = new View(context);
                view = (View) convertView;
            }

            TextView setsNumberTextView = (TextView) convertView.findViewById(R.id.setsNumberTextView);
            EditText workoutTrackerRepsEditTextNumber = (EditText) convertView.findViewById(R.id.workoutTrackerRepsEditTextNumber);

            setsNumberTextView.setText(String.valueOf(position));
            workoutTrackerRepsEditTextNumber.setText(String.valueOf(reps));
            Log.d(TAG, "getView() - [ "+position+" ] "+ reps);

            workoutTrackerRepsEditTextNumber.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
                @Override
                public void afterTextChanged(Editable editable) {
                    Log.d(TAG, "workoutTrackerRepsEditTextNumber " + editable);
                    try {
                        routinJsonArray.updateReps(routinPosition, exerciseSelected, position, Integer.parseInt(editable.toString()));
                        editor.putString(WorkoutActivity.MY_ROUTIN_PREFS_NAME, routinJsonArray.stringfyRoutinArray());
                        editor.commit();
//                        routin.getExercises().get(exerciseSelected).getReps().set(position, Integer.parseInt(editable.toString()));
                    } catch (NumberFormatException | JSONException e){
                        Log.d(TAG, e.toString());
//                        routin.getExercises().get(exerciseSelected).getReps().set(position, 0);
                    }
                }
            });

            //각 아이템 선택 event
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, position + "  "   + reps, Toast.LENGTH_SHORT).show();
                }
            });
            return convertView;  //뷰 객체 반환
        }
    }


    /*
    차트 관련 메소드
    */

    private void addEntry() {
        LineData data = chart.getData();
        if (data != null){
            Float emgValue = queue.poll();
            if(emgValue == null) return;
            if(emgValue < 0.5f){
                Log.d(TAG, "START or END, isWorkout?" + isWorkout);
                controlWorkout();
                return;
            }

            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            if(emgValue == null){
                emgValue = preEmgValue;
            }

            emgData.add(emgValue.intValue());

            data.addEntry(new Entry(set.getEntryCount(), emgValue), 0);
            data.notifyDataChanged();

            chart.notifyDataSetChanged();
            chart.setVisibleXRangeMaximum(10);
            chart.moveViewToX(data.getEntryCount());
        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setFillAlpha(110);
        set.setFillColor(Color.parseColor("#d7e7fa"));
        set.setColor(Color.parseColor("#0B80C9"));
        set.setCircleColor(Color.parseColor("#FFA1B4DC"));
        set.setCircleColorHole(Color.BLUE);
        set.setValueTextColor(Color.WHITE);
        set.setDrawValues(false);
        set.setLineWidth(2);
        set.setCircleRadius(6);
        set.setDrawCircleHole(false);
        set.setDrawCircles(false);
        set.setValueTextSize(9f);
        set.setDrawFilled(true);

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setHighLightColor(Color.rgb(244, 117, 117));

        return set;
    }

    public void feedMultiple() {
        if (chartThread != null) chartThread.interrupt();

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                addEntry();
            }
        };

        chartThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    runOnUiThread(runnable);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }
        });
    }

    /*
    =================================== 블루투스 연결 코드 ===============================
    */

    public void onClickBluetoothConnectionButton(View view){
        Log.d(TAG, "onClickBluetoothConnectionButton...");
        bleController.connectToDevice(deviceAddress);
    }

    public void onClickReadyStartButton(View view) throws JSONException {
        Log.d(TAG, "onClickReadyButton...");
        if (readyStartButton.getText().equals("준비")){
            if(connected) bleController.sendData("R" + routinJsonArray.getExercise(routinPosition, exerciseSelected).getInt("setCount"));
            readyStartButton.setText("시작");
        } else if(readyStartButton.getText().equals("시작")){
            readyStartButton.setText("종료");
        } else {
            readyStartButton.setText("준비");
        }

    }

    // EMG 데이터 저장 코드
    public void controlWorkout() {

//        WorkoutTrackerActivity.isWorkout = !WorkoutTrackerActivity.isWorkout;

        if (WorkoutTrackerActivity.isWorkout){
            // 세트 시작 시

            // 첫 세트인 경우 파일로 저장할 json 객체 초기화
            if (setsTotal == (setsCount--)) {
                setsStartingTime = System.currentTimeMillis();
                setStartingTime = System.currentTimeMillis();

                emgData = new ArrayList<>();
                maximumValueOfSets = new ArrayList<>();
                minimumValueOfSets = new ArrayList<>();
                try {
                    Log.d(TAG, "첫 세트 시작! 세트 수는 " + setsTotal);
                    workoutJSON.put("nickname", nickname);
                    workoutJSON.put("workout_name", "push_up");
                    workoutJSON.put("measured_muscle", "chest");
                    workoutJSON.put("starting_time", simpleDateFormat.format(setsStartingTime));
                    workoutJSON.put("sensing_interval", 500);
                    workoutJSON.put("number_of_sets", setsTotal);

                    setsSON = new JSONArray();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {    // 첫 세트 이후 세트 시작, (휴식시간 포함, 다음 S 신호를 보내는 경우)
                try {
                    // set JSON 초기화
                    JSONObject tempJSON = new JSONObject();
                    long now = System.currentTimeMillis();
                    long breakTime = now-breakStartingTime;
                    long setTime = now - setStartingTime - breakTime;

                    Integer maximumData = Collections.max(emgData);
                    Integer minimumData = Collections.min(emgData);
                    maximumValueOfSets.add(maximumData);
                    minimumValueOfSets.add(minimumData);

                    Log.d(TAG, "남은 세트 수는 " + setsCount);
                    tempJSON.put("time", setTime);
                    tempJSON.put("break_time", breakTime);
                    tempJSON.put("maximum_value_of_set", maximumData);
                    tempJSON.put("minimum_value_of_set", minimumData);
                    tempJSON.put("emg_data", emgData.toString());

                    setsSON.put(tempJSON);

                    emgData = new ArrayList<>();
                    setStartingTime = now;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                WorkoutTrackerActivity.feedMultiple();
            }
//            else {
//                WorkoutTrackerActivity.chartThread.start();
//            }
        } else {
            // 세트 종료 시,
//            chartThread = null;

            // 휴식시간 체크
            breakStartingTime = System.currentTimeMillis();

            // 모든 세트가 끝난 경우
            if(setsCount <= 0){
                try {
                    Log.d(TAG,"setsCount: " + setsCount);

                    // 마지막 set JSON 초기화
                    JSONObject tempJSON = new JSONObject();
                    long now = System.currentTimeMillis();
                    long breakTime = now-breakStartingTime;
                    long setTime = now - setStartingTime - breakTime;

                    Integer maximumData = Collections.max(emgData);
                    Integer minimumData = Collections.min(emgData);
                    maximumValueOfSets.add(maximumData);
                    minimumValueOfSets.add(minimumData);

                    tempJSON.put("time", setTime);
                    tempJSON.put("break_time", breakTime);
                    tempJSON.put("maximum_value_of_set", maximumData);
                    tempJSON.put("minimum_value_of_set", minimumData);
                    tempJSON.put("emg_data", emgData.toString());

                    setsSON.put(tempJSON);

                    workoutJSON.put("total_workout_time", System.currentTimeMillis()-setsStartingTime);        // 총 운동 시간
                    workoutJSON.put("maximum_value_of_sets", Collections.max(maximumValueOfSets));
                    workoutJSON.put("minimum_value_of_sets", Collections.max(minimumValueOfSets));
                    workoutJSON.put("sets", setsSON);
                    // 웹 서버에 전송
                    requestingServer = new RequestingServer(this, workoutJSON.toString());
                    String response = requestingServer.execute(myEmgDataURL).get();
                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                Log.d(TAG,workoutJSON.toString());
            }

        }
//        bleController.sendData("R" + setsTotal);
        Log.d(TAG,"isWorkout " + WorkoutTrackerActivity.isWorkout);
    }

    @Override
    public void BLEControllerConnected() {
        Log.d(TAG, "[BLE]\tConnected");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                disconnectButton.setEnabled(true);
                connected = true;
                bleController.sendData("B");
            }
        });
    }

    @Override
    public void BLEControllerDisconnected() {
        Log.d(TAG, "[BLE]\tDisconnected");
//        disableButtons();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectButton.setEnabled(true);
            }
        });
//        this.isLEDOn = false;
    }

    @Override
    public void BLEDeviceFound(String name, String address) {
        Log.d(TAG,"Device " + name + " found with address " + address);
        this.deviceAddress = address;
        this.connectButton.setEnabled(true);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"\"Access Fine Location\" permission not granted yet!");
            Log.d(TAG,"Whitout this permission Blutooth devices cannot be searched!");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    42);
        }
    }

    private void checkBLESupport() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(!BluetoothAdapter.getDefaultAdapter().isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, 1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.deviceAddress = null;
        this.bleController = BLEController.getInstance(this, bluetoothConnectionStatusTextView, readyStartButton);
        this.bleController.addBLEControllerListener(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"[BLE]\tSearching for BlueCArd...");
            this.bleController.init();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.bleController.removeBLEControllerListener(this);

        if (chartThread != null)
            chartThread.interrupt();
    }

}