package com.example.today_workout_complete;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CalenderActivity extends AppCompatActivity {
    String time,kcal,menu;
    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    Cursor cursor;
    MaterialCalendarView materialCalendarView;
    Retrofit retrofit;
    private RetrofitAPI retrofitAPI;
    TextView datatext;
    ArrayList<String> calendarShow = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calender);
        datatext=findViewById(R.id.textView4);
        materialCalendarView = (MaterialCalendarView)findViewById(R.id.calendarView);

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

        String[] result = {"2017,03,18","2017,04,18","2017,05,18","2017,06,18"};

        new ApiSimulator(result).executeOnExecutor(Executors.newSingleThreadExecutor());

        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                int Year = date.getYear();
                int Month = date.getMonth() + 1;
                int Day = date.getDay();

                Log.i("Year test", Year + "");
                Log.i("Month test", Month + "");
                Log.i("Day test", Day + "");

                String shot_Day = Year + "," + Month + "," + Day;

                Log.i("shot_Day test", shot_Day + "");
                materialCalendarView.clearSelection();

                Toast.makeText(getApplicationContext(), shot_Day , Toast.LENGTH_SHORT).show();
            }
        });

        setRetrofitInit();

        Call<List<UserInfo>> nicknamedata = retrofitAPI.getData("테스뚜");
        nicknamedata.enqueue(new Callback<List<UserInfo>>() {
            @Override
            public void onResponse(Call<List<UserInfo>> call, Response<List<UserInfo>> response) {
                try {
                    List<UserInfo> userInfos = response.body();
                    for(UserInfo user : userInfos){
                        ArrayList<String> data1=new ArrayList<>();
                        String year=user.getCreation_datetime().substring(0,4);
                        String month=user.getCreation_datetime().substring(5,7);
                        String day=user.getCreation_datetime().substring(8,10);
                        calendarShow.add(user.getCreation_datetime());
                        String content = "내가 운동한 날짜 : ";
                        content +=year+"년"+month+"월"+day+"일"+"\n";
                        datatext.append(content);
                    }
                }catch (Exception e){
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

        ApiSimulator(String[] Time_Result){
            this.Time_Result = Time_Result;
        }

        @Override
        protected List<CalendarDay> doInBackground(@NonNull Void... voids) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Calendar calendar = Calendar.getInstance();
            ArrayList<CalendarDay> dates = new ArrayList<>();

            /*특정날짜 달력에 점표시해주는곳*/
            /*월은 0이 1월 년,일은 그대로*/
            //string 문자열인 Time_Result 을 받아와서 ,를 기준으로짜르고 string을 int 로 변환
//            for(int i = 0 ; i < Time_Result.length ; i ++){
//                CalendarDay day = CalendarDay.from(calendar);
//                String[] time = Time_Result[i].split(",");
//                int year = Integer.parseInt(time[0]);
//                int month = Integer.parseInt(time[1]);
//                int dayy = Integer.parseInt(time[2]);
//
//                dates.add(day);
//                calendar.set(year,month-1,day);
//            }
            calendar.add(Calendar.MONTH, -2);

            //month는 아래 입력칸에서 +1 달로 인식됨
            // ex) 10월 15일에 입력하고싶어서 2022,10,15에하면 앱 캘린더엔 11월 15일에 표시되있음
            for(int i=0;i<calendarShow.size();i++){
                ArrayList<String> data1=new ArrayList<>();
                String yearString=calendarShow.get(i).substring(0,4);
                String monthString=calendarShow.get(i).substring(5,7);
                String dayString=calendarShow.get(i).substring(8,10);
                int year=Integer.parseInt(yearString);
                int month=Integer.parseInt(monthString)-1;
                int day=Integer.parseInt(dayString);
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
            materialCalendarView.addDecorator(new EventDecorator(Color.GREEN, calendarDays,CalenderActivity.this));
        }

    }
}