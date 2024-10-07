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
            Log.e(TAG, "readFileAll : 파일이 존재하지 않습니다: " + fileName);
            return null; // 파일이 없을 경우 빈 리스트 반환
        }

        String jsonString = readFileContent(context, fileName);
        if (jsonString == null) {
            Log.e(TAG, "readFileAll :파일 내용 읽기 중 오류 발생: " + fileName);
            return null; // 오류 발생 시 빈 리스트 반환
        }

        try {
            JSONArray jsonArray = new JSONArray(jsonString);

            if (jsonArray.length() == 0) {
                Log.e(TAG, "readFileAll :JSON 배열이 비어 있습니다.");
                return null; // 빈 리스트 반환
            }

            Log.d(TAG, "readFileAll : 앱 정보가 성공적으로 잘 읽어졌습니다: " + fileName);
            return jsonArray;
        } catch (JSONException e) {
            Log.e(TAG, "readFileAll : JSON 파싱 중 오류 발생: ", e);
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
                Customizing_widget.packageName = mostUsedApp.getString("package_name");

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

    public static String getPackageName(Context context, int UsageNum) {
        try {
            // 파일 읽기 시도
            JSONArray Appjson = Utils.readFileAll(context, "All_app.json");

            // !수정: 파일이 null인지 확인
            if (Appjson == null) {
                Log.e(TAG, "getPackageName에서 JSON 파일을 읽을 수 없습니다: All_app.json");
                return null;  // 파일이 없거나 읽기 실패 시 null 반환
            }

            // !수정: UsageNum이 배열 크기보다 큰 경우 예외 처리
            if (UsageNum >= Appjson.length()) {
                Log.e(TAG, "유효하지 않은 UsageNum: " + UsageNum);
                return null;  // 유효하지 않은 인덱스에 대해 null 반환
            }

            // JSON 객체 추출
            JSONObject AppjsonObjec = Appjson.getJSONObject(UsageNum);

            // 패키지 이름 반환
            return AppjsonObjec.getString("package_name");

        } catch (JSONException e) {
            // !추가: JSON 처리 중 예외 발생 시
            Log.e(TAG, "getPackageName에서 JSON 파싱 중 오류 발생", e);
            return null;  // 오류 발생 시 null 반환
        } catch (Exception e) {
            // !추가: 그 외 예외 발생 시
            Log.e(TAG, "getPackageName에서 알 수 없는 오류 발생", e);
            return null;  // 오류 발생 시 null 반환
        }
    }

    public static String getUsageTime(Context context, int UsageNum) {
        try {
            // JSON 파일 읽기 시도
            JSONArray Appjson = Utils.readFileAll(context, "All_app.json");

            // !추가: 파일이 없거나 null인 경우 처리
            if (Appjson == null) {
                Log.e(TAG, "getUsageTime : JSON 파일이 존재하지 않거나 읽을 수 없습니다: All_app.json");
                return null;  // 적절한 기본 메시지 반환
            }

            // !추가: UsageNum이 배열 범위를 넘지 않는지 확인
            if (UsageNum >= Appjson.length()) {
                Log.e(TAG, "getUsageTime : 유효하지 않은 UsageNum: " + UsageNum);
                return null;  // 잘못된 인덱스에 대한 처리
            }

            // JSON 객체에서 해당 인덱스의 데이터 추출
            JSONObject AppjsonObjec = Appjson.getJSONObject(UsageNum);

            // 사용 시간을 String에서 long으로 변환 후 HMS 포맷으로 변환
            return StaticMethodClass.convertMillisToHMS(Long.parseLong(AppjsonObjec.getString("usageTime")));

        } catch (JSONException e) {
            // !추가: JSON 파싱 중 오류 발생 시 예외 처리
            Log.e(TAG, "getUsageTime : JSON 파싱 중 오류 발생", e);
            return null;
        } catch (NumberFormatException e) {
            // !추가: Long.parseLong 변환 중 오류 발생 시 처리
            Log.e(TAG, "getUsageTime : 숫자 형식 변환 오류", e);
            return null;
        } catch (Exception e) {
            // !추가: 그 외 모든 예외 처리
            Log.e(TAG, "getUsageTime : 알 수 없는 오류 발생", e);
            return null;
        }
    }
}
