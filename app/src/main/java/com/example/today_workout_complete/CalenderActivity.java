package com.example.today_workout_complete;

import android.content.Context;
import android.database.Cursor;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CalenderActivity extends AppCompatActivity {
    String time, kcal, menu;
    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    Cursor cursor;
    MaterialCalendarView materialCalendarView;
    Retrofit retrofit;
    private RetrofitAPI retrofitAPI;
    TextView datatext;
    private String nickname;
    ArrayList<String> calendarShow = new ArrayList<>();
    private RecyclerViewAdapter recyclerViewAdapter;
    private RecyclerView caleanderRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calender);
        materialCalendarView = (MaterialCalendarView) findViewById(R.id.calendarView);
        MenuFragment menuFragment = new MenuFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.menuFragmentFrame, menuFragment).commit();

        materialCalendarView.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setMinimumDate(CalendarDay.from(2017, 0, 1)) // 달력의 시작
                .setMaximumDate(CalendarDay.from(2030, 11, 31)) // 달력의 끝
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();

        materialCalendarView.addDecorators(
                new SundayDecorator(),
                new SaturdayDecorator(),
                oneDayDecorator);

        String[] result = {"2017,03,18", "2017,04,18", "2017,05,18", "2017,06,18"};
        nickname = getSharedPreferences(WebViewActivity.MY_NICKNAME_PREFS_NAME, Context.MODE_PRIVATE).getString(WebViewActivity.MY_NICKNAME_PREFS_NAME, "");

        new ApiSimulator(result).executeOnExecutor(Executors.newSingleThreadExecutor());
        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                int Year = date.getYear();
                int Month = date.getMonth() + 1;
                int Day = date.getDay();

                Call<List<EmgData>> sensordata = retrofitAPI.getEmgData(Year, nickname, Month, Day);
                sensordata.enqueue(new Callback<List<EmgData>>() {
                    @Override
                    public void onResponse(Call<List<EmgData>> call, Response<List<EmgData>> response) {
                        // caleanderRecyclerView
                        ArrayList<String> workoutDataList = new ArrayList<>();
                        caleanderRecyclerView = (RecyclerView) findViewById(R.id.caleanderRecyclerView);


                        List<EmgData> EmgDataList = response.body();
                        if(EmgDataList.size() == 0){
                            workoutDataList.add("해당날짜에 운동하지 않았습니다!");
                            recyclerViewAdapter = new RecyclerViewAdapter(EmgDataList);
                            caleanderRecyclerView.setAdapter(recyclerViewAdapter);
                            caleanderRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            return;
                        }else{
                            recyclerViewAdapter = new RecyclerViewAdapter(EmgDataList);
                            caleanderRecyclerView.setAdapter(recyclerViewAdapter);
                            caleanderRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        }

                    }

                    @Override
                    public void onFailure(Call<List<EmgData>> call, Throwable t) {
                        datatext.setText(t.getMessage());
                        Log.v("err는", call.toString());
                    }
                });
            }
        });
        setRetrofitInit();
        Call<List<UserInfo>> nicknamedata = retrofitAPI.getData(nickname);
        nicknamedata.enqueue(new Callback<List<UserInfo>>() {
            @Override
            public void onResponse(Call<List<UserInfo>> call, Response<List<UserInfo>> response) {
                try {
                    List<UserInfo> userInfos = response.body();
                    for (UserInfo user : userInfos) {
                        int idx = user.getEmg_data_path().indexOf("_");
                        String emgDate = user.getEmg_data_path().substring(idx + 1);
                        String year = emgDate.substring(0, 4);
                        String month = emgDate.substring(4, 6);
                        String day = emgDate.substring(6, 8);
                        calendarShow.add(emgDate);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<List<UserInfo>> call, Throwable t) {
                datatext.setText(t.getMessage());
            }
        });
    }

    private void setRetrofitInit() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://118.67.132.81:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofitAPI = retrofit.create(RetrofitAPI.class);

    }

    private class ApiSimulator extends AsyncTask<Void, Void, List<CalendarDay>> {

        String[] Time_Result;

        ApiSimulator(String[] Time_Result) {
            this.Time_Result = Time_Result;
        }

        @Override
        protected List<CalendarDay> doInBackground(@NonNull Void... voids) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

//            String content1=" ";
            Calendar calendar = Calendar.getInstance();
            ArrayList<CalendarDay> dates = new ArrayList<>();

            calendar.add(Calendar.MONTH, -2);

            //month는 아래 입력칸에서 +1 달로 인식됨
            // ex) 10월 15일에 입력하고싶어서 2022,10,15에하면 앱 캘린더엔 11월 15일에 표시되있음
            for (int i = 0; i < calendarShow.size(); i++) {
                ArrayList<String> data1 = new ArrayList<>();
                String yearString = calendarShow.get(i).substring(0, 4);
                String monthString = calendarShow.get(i).substring(4, 6);
                String dayString = calendarShow.get(i).substring(6, 8);
                int year = Integer.parseInt(yearString);
                int month = Integer.parseInt(monthString) - 1;
                int day = Integer.parseInt(dayString);
                dates.add(CalendarDay.from(year, month, day));
            }
            return dates;
        }

        @Override
        protected void onPostExecute(@NonNull List<CalendarDay> calendarDays) {
            super.onPostExecute(calendarDays);
            if (isFinishing()) {
                return;
            }
            materialCalendarView.addDecorator(new EventDecorator(Color.GREEN, calendarDays, CalenderActivity.this));
        }

    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<CalenderActivity.RecyclerViewAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.recycler_list_calendar, parent, false);
            ViewHolder vh = new CalenderActivity.RecyclerViewAdapter.ViewHolder(view);
            return vh;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String workoutData = "내가 한 운동 : "+workoutDataList.get(position).getWorkout_name();
            holder.workoutDataListTextView.setText(workoutData);
            LineData lineData = new LineData();
            Random random = new Random();
            for (int i = 0; i < workoutDataList.get(position).getNumber_of_sets(); i++) {
                ArrayList<Entry> dataList = new ArrayList<>();
                for(int j = 0; j < workoutDataList.get(position).getSets().get(i).getEmg_data().length; j++){
                    dataList.add(new Entry(j * 200, workoutDataList.get(position).getSets().get(i).getEmg_data()[j]));
                }
                LineDataSet myLineDataSet = new LineDataSet(dataList, (i+1)+"set");
                final int alphaColor = -2147483647;
                myLineDataSet.setColor(random.nextInt(16777215) + alphaColor);
                myLineDataSet.setCircleHoleRadius(1f);
                myLineDataSet.setCircleRadius(1f);
                myLineDataSet.setLineWidth(2f);
                lineData.addDataSet(myLineDataSet);
            }
            holder.chart.setData(lineData);
            holder.chart.invalidate();
        }
        private List<EmgData> workoutDataList;
        public RecyclerViewAdapter(List<EmgData> workoutDataList) {
            this.workoutDataList = workoutDataList;
        }

        @Override
        public int getItemCount() {
            return workoutDataList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView workoutDataListTextView;
            LineChart chart;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                workoutDataListTextView = (TextView) itemView.findViewById(R.id.workoutDataListTextView);
                chart = (LineChart) itemView.findViewById(R.id.calendarChart);
            }
        }
    }
}