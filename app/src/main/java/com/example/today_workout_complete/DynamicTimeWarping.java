package com.example.today_workout_complete;

import android.util.Log;

import java.util.ArrayList;

public class DynamicTimeWarping {
    private String TAG = DynamicTimeWarping.class.getSimpleName();
    private Float[][] dtwMatrix;

    public Float[] cutPeriod(Float[] emgData) {
        final int JUDGEMENT_COUNT_NUMBER = 2;
        Float[] newEmgData;
        int startIndex = 0;
        int lastIndex = emgData.length-1;
        int judgmentCount = 0;				// 0.8초간 힘 주고 있는 경우로 가정

        Log.d(TAG, emgData.toString());
        // 첫 상승 구간 구하기
        for(int i = startIndex; judgmentCount < JUDGEMENT_COUNT_NUMBER && i < emgData.length; i++) {
            if(emgData[i] > emgData[i+1]){
                judgmentCount = 0;
                startIndex = ++i;
            }
            judgmentCount++;
            Log.d(TAG, "judgmentCount: " + judgmentCount);
        }
        Log.d(TAG, "startIndex: " + startIndex);

        // 마지막 하강 구간
        judgmentCount = 0;
        for(int i = lastIndex; judgmentCount < JUDGEMENT_COUNT_NUMBER && i > 0; i--) {
            if(emgData[i] > emgData[i-1]){
                judgmentCount = 0;
                lastIndex = --i;
            }
            judgmentCount++;
            Log.d(TAG, "judgmentCount: " + judgmentCount);
        }
        Log.d(TAG, "lastIndex: " + lastIndex);

        newEmgData = new Float[lastIndex-startIndex+1];
        for(int i = startIndex; i <= lastIndex; i++) newEmgData[i-startIndex] = emgData[i];

        String logText = "\n";
        for(Float emg : emgData) logText += emg+ " ";
        Log.d(TAG, "emgData");
        Log.d(TAG, "\n" + logText);
        logText = "\n";
        for(Float emg : newEmgData) logText += emg+ " ";
        Log.d(TAG, "newEmgData");
        Log.d(TAG, "\n" + logText);

        return newEmgData;
    }

    public Float[][] getDTWMatrix(Float[] timeSeries1, Float[] timeSeries2) {
        dtwMatrix = new Float[timeSeries1.length+1][timeSeries2.length+1];
        int SAKOE_CHIBA_BAND = 2;

        Log.d(TAG, "@@@@@@@@@@@@@@@@@@ 초기 세팅 @@@@@@@@@@@@@@@@@@@@");
        // 초기 세팅
        for(int i = 0; i < dtwMatrix.length; i++){
            for(int j = 0; j < dtwMatrix[i].length; j++) dtwMatrix[i][j] = 0f;
        }

        int index = 1;
        for(int i = 0; i < dtwMatrix.length; i++) {
            for(int j=index; j<dtwMatrix[i].length; j++) {
                try {
                    if(dtwMatrix[i][j] != null) dtwMatrix[i][j] = Float.MAX_VALUE;
                    if(dtwMatrix[j][i] != null) dtwMatrix[j][i] = Float.MAX_VALUE;
                } catch (Exception e) {
                    Log.d(TAG, "OUT");
                }

            }
            index += SAKOE_CHIBA_BAND;
        }

        showDtwMatrix();

        // 매트릭스 거리 연산
        for(int i=1; i < dtwMatrix.length; i++) {
            for(int j=1; j < dtwMatrix[i].length; j++) {
                if(dtwMatrix[i][j] == Float.MAX_VALUE) {
                    if(dtwMatrix[i][j-1] != Float.MAX_VALUE) break;
                    continue;
                }

                Float dist = Math.abs((timeSeries1[i-1] - timeSeries2[j-1]));
                Float min = dtwMatrix[i-1][j-1];

                if(min > dtwMatrix[i-1][j]) {
                    if(dtwMatrix[i-1][j] > dtwMatrix[i][j-1])
                        min = dtwMatrix[i][j-1];
                    else
                        min = dtwMatrix[i-1][j];
                }
                else if(min > dtwMatrix[i][j-1]) min = dtwMatrix[i][j-1];

                dtwMatrix[i][j] = dist + min;
            }
        }
        showDtwMatrix();
        return dtwMatrix;
    }

    public void showDtwMatrix() {
        String matrix = "";
        for(int i = 0; i < dtwMatrix.length; i++) {
            for(int j = 0; j < dtwMatrix[i].length; j++) matrix += dtwMatrix[i][j] + " ";
            Log.d(TAG, matrix);
            matrix = "";
        }

    }

    public Float getDtwDistance(Float[] timeSeries1, Float[] timeSeries2) {
        Float[][] dtwMatrix = getDTWMatrix(timeSeries1, timeSeries2);

        return dtwMatrix[timeSeries1.length][timeSeries2.length];
    }

    public ArrayList<int[]> getWarpingPath(Float[] ts1, Float[] ts2) {
        if(dtwMatrix == null) dtwMatrix = getDTWMatrix(ts1, ts2);
        ArrayList<int[]> warpingPath = new ArrayList<int[]>();

        int i=1, j=1;
        warpingPath.add(new int[]{1,1});
        while(i<dtwMatrix.length-1 && j<dtwMatrix[i].length-1) {
            if(dtwMatrix[i+1][j+1] != null && dtwMatrix[i][j+1] != null && dtwMatrix[i+1][j+1] > dtwMatrix[i][j+1]) {
                if(dtwMatrix[i][j+1] > dtwMatrix[i+1][j]) {
                    warpingPath.add(new int[]{i+1,j});
                    i++;
                } else {
                    warpingPath.add(new int[]{i,j+1});
                    j++;
                }
            } else {
                if(dtwMatrix[i+1][j+1] != null && dtwMatrix[i+1][j] != null && dtwMatrix[i+1][j+1] > dtwMatrix[i+1][j]) {
                    warpingPath.add(new int[]{i+1, j});
                    i++;
                } else {
                    warpingPath.add(new int[]{i+1, j+1});
                    i++;
                    j++;
                }
            }

            if(i == dtwMatrix.length-1) {
                while(j<dtwMatrix[i].length-1) {
                    warpingPath.add(new int[]{i, j+1});
                    j++;
                }
            } else if(j == dtwMatrix[i].length-1) {
                while(i<dtwMatrix.length-1) {
                    warpingPath.add(new int[]{i+1, j});
                    i++;
                }
            }
        }
        Log.d(TAG, "############### PATH #################");
        String dtwPath = "";
        for(int k = 0; k < warpingPath.size(); k++) dtwPath += dtwMatrix[warpingPath.get(k)[0]][warpingPath.get(k)[1]] + " ";
        Log.d(TAG, "dtwPath: " + dtwPath);
        Log.d(TAG, "######################################");

        return warpingPath;
    }
}
