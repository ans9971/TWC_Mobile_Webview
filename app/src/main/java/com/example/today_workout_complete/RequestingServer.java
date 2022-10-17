package com.example.today_workout_complete;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RequestingServer extends AsyncTask<String, String, String> {

    String stringJsonObj;
    Activity activity;

    public RequestingServer(Activity activity, String stringJsonObj){
        this.activity = activity;
        this.stringJsonObj = stringJsonObj;
    }

    @Override
    protected String doInBackground(String... urls) {

        String tag = "RequestingServer";
        HttpURLConnection con = null;
        BufferedReader reader = null;

        try{
            Log.i(tag, "utls[0]: " + urls[0] );
            URL url = new URL(urls[0]);
            con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("POST");                                // GET or POST방식으로 보냄
            con.setRequestProperty("Cache-Control", "no-cache");            // 캐시 설정
            con.setRequestProperty("Content-Type", "application/json");     // application JSON 형식으로 전송
            con.setRequestProperty("Accept", "text/html");                  // 서버에 response 데이터를 html로 받음
            con.setDoOutput(true);                                          // Outstream으로 post 데이터를 넘겨주겠다는 의미
            con.setDoInput(true);                                           // Inputstream으로 서버로부터 응답을 받겠다는 의미
            con.connect();

            OutputStream outputStream = con.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

            writer.write(stringJsonObj);
            writer.flush();
            writer.close();

            InputStream stream = con.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                Log.i(tag, "line: " + line);
            }

            Log.i(tag, "buffer: " + buffer);

            return buffer.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (con != null)
                con.disconnect();
            try{
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }
}