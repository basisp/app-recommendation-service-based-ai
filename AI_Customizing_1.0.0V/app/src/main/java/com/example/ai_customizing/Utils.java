package com.example.ai_customizing;

import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class Utils {

    private static final String TAG = "Utils";

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

    public static JSONArray readFileApp(Context context, String fileName) {
        // 파일 존재 여부 확인
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
            JSONObject jsonObject = new JSONObject(jsonString); // JSONObject로 변환
            JSONArray jsonArray = jsonObject.getJSONArray("apps"); // "apps" 배열 추출

            if (jsonArray.length() == 0) {
                Log.e(TAG, "JSON 배열이 비어 있습니다.");
                return null; // 빈 리스트 반환
            }

            Log.d(TAG, "앱 정보가 성공적으로 잘 읽어졌습니다: " + fileName);
            return jsonArray; // JSONArray 반환
        } catch (JSONException e) {
            Log.e(TAG, "JSON 파싱 중 오류 발생: ", e);
        }

        return null; // 예외 발생 시 null 반환
    }

    // JSON 파일에서 첫 번째 앱 정보를 읽어오는 메서드
    public static ExtendedApplicationInfo readFileOne(Context context, String fileName) {
        ExtendedApplicationInfo appInfo = null;

        String jsonString = readFileContent(context, fileName);
        if (jsonString == null) {
            Log.e(TAG, "파일 내용 읽기 중 오류 발생: " + fileName);
            return appInfo; // 오류 발생 시 null 반환
        }

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            if (jsonArray.length() > 0) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String packageName = jsonObject.getString("packageName");
                String categoryName = jsonObject.getString("category");
                long usageTime = jsonObject.getLong("usageTime");
                appInfo = new ExtendedApplicationInfo(packageName, categoryName, usageTime);
                Log.d(TAG, "첫 번째 앱 정보가 성공적으로 읽어졌습니다: " + appInfo);
            } else {
                Log.d(TAG, "JSON 배열에 앱 정보가 없습니다.");
            }

        } catch (JSONException e) {
            Log.e(TAG, "JSON 파싱 중 오류 발생: ", e);
        }

        return appInfo; // 첫 번째 앱 정보를 반환
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
}
