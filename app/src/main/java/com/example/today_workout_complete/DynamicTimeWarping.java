package com.example.today_workout_complete;

import android.util.Log;

import java.util.ArrayList;

public class DynamicTimeWarping {
    private String TAG = DynamicTimeWarping.class.getSimpleName();
    private Float[][] dtwMatrix;
    private final int SAKOE_CHIBA_BAND = 5;                 // 1초
    private final int JUDGEMENT_COUNT_NUMBER = 4;           // 0.8초

    public Float[] cutPeriod(Float[] emgData) {
        Float[] newEmgData;
        int startIndex = 0;
        int lastIndex = emgData.length-1;
        int judgmentCount = 0;

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

        String logText = "";
        for(Float emg : emgData) logText += emg+ " ";
        Log.d(TAG, "emgData");
        Log.d(TAG, logText);
        return newEmgData;
    }

    public Float[] getNormalizedEmgData(Float[] emgData, Float min, Float max){
        if(min == max){
            for(int i = 0; i < emgData.length; i++) emgData[i] = 0f;
            return emgData;
        }
        for(int i = 0; i < emgData.length; i++) emgData[i] = (emgData[i] - min)/(max - min);
        return emgData;
    }

    public Float[][] getDTWMatrix(Float[] otherEmgData, Float[] myEmgData) {
        String emgDataText = "";
        for(Float emg : otherEmgData) emgDataText += emg+ " ";
        Log.d(TAG, "otherEmgData");
        Log.d(TAG, emgDataText);
        emgDataText = "";
        for(Float emg : myEmgData) emgDataText += emg+ " ";
        Log.d(TAG, "myEmgData");
        Log.d(TAG, emgDataText);

        dtwMatrix = new Float[otherEmgData.length+1][myEmgData.length+1];

        Log.d(TAG, "@@@@@@@@@@@@@@@@@@ 초기 세팅 @@@@@@@@@@@@@@@@@@@@  " + (13f - 13f));
        int sakoeChibaBand = SAKOE_CHIBA_BAND;
        int limitChibaBand = sakoeChibaBand * 2 - 1;
        int chibaBandInitialHeight = sakoeChibaBand - 1;
        int startPoint = 1;
        boolean isColumnMAx = false;
        boolean isRowMAx = false;

        for(int i = 0; i < dtwMatrix.length; i++){
            for(int j = 0; j < dtwMatrix[i].length; j++){
                int maxColumnIndex = startPoint == 1 ? sakoeChibaBand + 1 : i + sakoeChibaBand - chibaBandInitialHeight;

                isColumnMAx = maxColumnIndex >= dtwMatrix[i].length;
                isRowMAx = dtwMatrix.length - i <= chibaBandInitialHeight + 1 && !isColumnMAx && j >= startPoint;
                if(isRowMAx || (i != 0 && j >= startPoint && j < maxColumnIndex)){
                    dtwMatrix[i][j] = 0f;
                } else {
                    dtwMatrix[i][j] = Float.MAX_VALUE;
                }
            }
            if(!isColumnMAx && sakoeChibaBand == limitChibaBand) startPoint++;
            if(i != 0 && sakoeChibaBand < limitChibaBand) sakoeChibaBand++;
        }

        dtwMatrix[0][0] = 0f;


        showDtwMatrix();

        // 매트릭스 거리 연산
        for(int i=1; i < dtwMatrix.length; i++) {
            for(int j=1; j < dtwMatrix[i].length; j++) {
                if(dtwMatrix[i][j] == Float.MAX_VALUE) {
                    if(dtwMatrix[i][j-1] != Float.MAX_VALUE) break;
                    continue;
                }

                Float dist = Math.abs((otherEmgData[i-1] - myEmgData[j-1]));
                if(dist.isNaN()){
                    Log.d(TAG, i + "  " + j);
                    Log.d(TAG, otherEmgData[i-1] + "  " + myEmgData[j-1]);
                    Log.d(TAG, "otherEmgData[i-1] - myEmgData[j-1] = " + (otherEmgData[i-1] - myEmgData[j-1]));
                    Log.d(TAG, "Math.abs((otherEmgData[i-1] - myEmgData[j-1])) = " + Math.abs((otherEmgData[i-1] - myEmgData[j-1])));
                }
                Float min = dtwMatrix[i-1][j-1];

                if(min > dtwMatrix[i-1][j]) {
                    if(dtwMatrix[i-1][j] > dtwMatrix[i][j-1])
                        min = dtwMatrix[i][j-1];
                    else
                        min = dtwMatrix[i-1][j];
                }
                else if(min > dtwMatrix[i][j-1]) min = dtwMatrix[i][j-1];

                dtwMatrix[i][j] = dist + min;

                if(dtwMatrix[i][j].isNaN()){
                    dtwMatrix[i][j] = 0f;
                    Log.d(TAG, i + "   " + j);
                    Log.d(TAG, dist + "   " + min);

                }
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

    public Float getDtwDistance(Float[] otherEmgData, Float[] myEmgData) {
        Float[][] dtwMatrix = getDTWMatrix(otherEmgData, myEmgData);

        return dtwMatrix[otherEmgData.length][myEmgData.length];
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
