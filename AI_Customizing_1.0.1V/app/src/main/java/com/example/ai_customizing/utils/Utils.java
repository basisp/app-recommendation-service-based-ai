package com.example.ai_customizing.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.example.ai_customizing.widget.Customizing_widget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class Utils {

    private static final String TAG = "Utils";
    public static JSONObject readFileApp_data(Context context, String fileName) {
        // !수정: 파일 존재 여부 확인
        File file = context.getFileStreamPath(fileName);
        if (!file.exists()) {
            Log.e(TAG, "파일이 존재하지 않습니다: " + fileName);
            return null; // 파일이 없을 경우 null 반환
        }

        String jsonString = readFileContent(context, fileName);
        if (jsonString == null) {
            Log.e(TAG, "파일 내용 읽기 중 오류 발생: " + fileName);
            return null; // 오류 발생 시 null 반환
        }

        try {
            // !수정: 전체 파일을 JSONObject로 파싱
            JSONObject mainJsonObject = new JSONObject(jsonString);

            // "apps" 배열이 존재하는지 확인
            if (mainJsonObject.has("apps")) {
                Log.d(TAG, "앱 정보가 성공적으로 잘 읽어졌습니다: " + fileName);
                return mainJsonObject; // !수정: 전체 객체 반환
            } else {
                Log.e(TAG, "'apps' 배열이 존재하지 않습니다.");
                return null; // "apps" 배열이 없을 경우 null 반환
            }

        } catch (JSONException e) {
            Log.e(TAG, "JSON 파싱 중 오류 발생: ", e);
        }

        return null;
    }




    //오류시 null 반환
    public static JSONArray readFileAll(Context context, String fileName) {

        // 파일 존재 여부 확인
        File file = context.getFileStreamPath(fileName);
        if (!file.exists()) {
            Log.e(TAG, "파일이 존재하지 않습니다: " + fileName);
            return null; // 파일이 없을 경우 빈 리스트 반환
        }

        String jsonString = readFileContent(context, fileName);
        if (jsonString == null) {
            Log.e(TAG, "파일 내용 읽기 중 오류 발생: " + fileName);
            return null; // 오류 발생 시 빈 리스트 반환
        }

        try {
            JSONArray jsonArray = new JSONArray(jsonString);

            if (jsonArray.length() == 0) {
                Log.e(TAG, "JSON 배열이 비어 있습니다.");
                return null; // 빈 리스트 반환
            }

            Log.d(TAG, "앱 정보가 성공적으로 잘 읽어졌습니다: " + fileName);
            return jsonArray;
        } catch (JSONException e) {
            Log.e(TAG, "JSON 파싱 중 오류 발생: ", e);
        }

        return null;
    }

    // 파일에서 내용을 읽어오는 공통 메서드 (내부 저장소 -> 외부 저장소 순으로 확인)
    private static String readFileContent(Context context, String fileName) {
        StringBuilder stringBuilder0 = new StringBuilder();

        // 내부 저장소에서 파일 읽기 시도
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.openFileInput(fileName)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder0.append(line);
            }
            Log.d(TAG, "내부 저장소에서 파일 읽기 성공: " + fileName);
            return stringBuilder0.toString();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "내부 저장소에 파일이 없습니다. " + fileName);
        } catch (IOException e) {
            Log.e(TAG, "내부 저장소에서 파일 읽기 중 오류 발생: ", e);
            return null;
        }

        // 파일이 없거나 읽기 중 오류가 발생하면 null을 반환합니다.
        return null;
    }


    //파일을 일어와 json이 아닌 이벤트 실행을 위한 인텐트로 변환
    public static Intent getMostUsedAppIntent(Context context, String category) {
        // 파일 이름은 "카테고리명_app.json" 형식
        String fileName = category + "_app.json";

        try {
            // 파일 읽기
            FileInputStream fis = context.openFileInput(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder jsonString = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            reader.close();

            // JSON 파싱
            JSONArray appArray = new JSONArray(jsonString.toString());
            if (appArray.length() > 0) {
                JSONObject mostUsedApp;
                if(fileName.equals("All_app.json")){
                    mostUsedApp = appArray.getJSONObject(Customizing_widget.categoryNumber);
                    Log.d(TAG,"모든 앱에서 가장 많이 사용한 앱 정보 가져옴");
                }
                else {
                    mostUsedApp = appArray.getJSONObject(0);
                    Log.d(TAG,"카테고리 에서 가장 많이 사용한 앱 정보 가져옴");
                }
                Customizing_widget.packageName = mostUsedApp.getString("packageName");

                // 해당 앱의 인텐트를 반환
                PackageManager packageManager = context.getPackageManager();
                Log.d(TAG,"Utils에서 정상적으로 launchIntent 반환 완료.");
                return packageManager.getLaunchIntentForPackage(Customizing_widget.packageName);
            }

        } catch (IOException | JSONException e) {
            Log.d(TAG,"파일 읽기/파싱 중 오류.");
            e.printStackTrace();
        }

        return null;
    }

    public static String getPackageName(Context context, int UsageNum) throws JSONException {
        JSONArray Appjson = Utils.readFileAll(context, "All_app.json");
        assert Appjson != null;
        JSONObject AppjsonObjec = Appjson.getJSONObject(UsageNum);

        return AppjsonObjec.getString("packageName");
    }

    public static String getUsageTime(Context context, int UsageNum) throws JSONException {
        JSONArray Appjson = Utils.readFileAll(context, "All_app.json");
        assert Appjson != null;
        JSONObject AppjsonObjec = Appjson.getJSONObject(UsageNum);

        return StaticMethodClass.convertMillisToHMS(Long.parseLong(AppjsonObjec.getString("usageTime")));
    }
}
