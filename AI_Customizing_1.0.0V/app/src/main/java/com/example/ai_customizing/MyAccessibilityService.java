
package com.example.ai_customizing;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyAccessibilityService extends AccessibilityService implements CategoryCallback {
    private final String TAG = "MyAccessibilityService";
    private String lastPackageName = "";
    private CountDownLatch latch;
    private String Category;
    private final List<String> recentApps = new ArrayList<>(); // 최근 실행된 앱 목록 , 재할당 가능성 없는 경고
    private final List<Long> TimeStamp = new ArrayList<Long>();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //이벤트 타입 윈도우 상태 변화.
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName().toString();
            addTimeStamp(); //타임 스템프 시스템 시간 찍기(밀리초)
            PackageManager packageManager = getPackageManager();
            try {
                ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);

                if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 || StaticMethodClass.isExceptionSystemApp(appInfo.packageName)) {
                    if (!packageName.equals(lastPackageName)) {
                        Log.d(TAG, "앱 실행됨: " + packageName);
                        lastPackageName = packageName;

                        // 최근 앱 리스트에 추가, 카테고리도 추가.
                         addRecentApp(packageName);
                        // Json파일로 저장, 필요시 서버로 보내는 함수 포함되어있음
                        latch = new CountDownLatch(1);
                        SaveJsonFile(packageName, this);

                        incrementClickCount(this, packageName);
                    } else {
                        Log.d(TAG, "실행중인 앱, 무시됨: " + packageName);
                    }
                } else {
                    Log.d(TAG, "시스템 앱 실행됨, 무시됨: " + packageName);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                Log.d(TAG, "앱 패키지 정보를 찾지 못함 - " + packageName);
            }
        }
    }

    private void addRecentApp(String packageName) {
        // 이미 리스트에 같은 패키지 이름이 있는지 확인
        if (!recentApps.contains(packageName)) {
            // 최근 앱 목록에 추가
            recentApps.add(packageName);
            // 리스트가 3개를 초과하면 첫 번째 앱 제거
            if (recentApps.size() > 3) {
                recentApps.remove(0);
            }
        } else {
            Log.d(TAG, "실행 리스트에 있는 앱 - " + packageName);
        }
    }

    private void addTimeStamp() {
        // 이미 리스트에 같은 패키지 이름이 있는지 확인
        // 최근 앱 목록에 추가
        TimeStamp.add(System.currentTimeMillis());
        // 리스트가 3개를 초과하면 첫 번째 앱 제거
        if (TimeStamp.size() > 3) {
            TimeStamp.remove(0);
        }
    }


    private void SaveJsonFile() {
        // 다운로드 폴더에서 JSON 파일을 읽어옴
        JSONArray jsonArray = Utils.readFileAll(this, "app_data.json");
        if (jsonArray == null) {
            jsonArray = new JSONArray();
            Log.d(TAG, "자바 객체를 새로 생성합니다.");
        }

        try {
            latch.await(); // 모든 비동기 요청이 완료될 때까지 대기
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("timestamp", TimeStamp.get(TimeStamp.size() - 1));
            jsonObject.put("package_name", recentApps.get(recentApps.size() - 1));
            jsonObject.put("category", Category);

            // JSON 배열에 추가
            jsonArray.put(jsonObject);

            // JSON 배열의 크기가 4개 이상이면 첫 번째 데이터를 제거
            if (jsonArray.length() > 3) {
                JSONArray newJsonArray = new JSONArray();
                // 1번째 이후의 데이터만 새로운 JSONArray에 추가
                for (int i = 1; i < jsonArray.length(); i++) {
                    newJsonArray.put(jsonArray.get(i));
                }
                jsonArray = newJsonArray; // 업데이트된 JSON 배열로 교체
            }
        } catch (JSONException | InterruptedException e) {
            Log.e(TAG, "자바파일 정렬중 오류 발생", e);
            e.printStackTrace();
        }

        // JSON 배열을 파일에 저장
        saveJsonToFileORSERVER(jsonArray);
    }

//    //패키지 이름을 이용하여 앱의 라벨을 얻고 반환
//    public static String getAppLabelByPackageName(String packageName, Context context) {
//        try {
//            // PackageManager를 사용하여 ApplicationInfo 가져오기
//            PackageManager packageManager = context.getPackageManager();
//            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
//
//            // 앱의 라벨을 가져와 String으로 변환
//            CharSequence appLabel = packageManager.getApplicationLabel(applicationInfo);
//            return appLabel.toString();
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//            return null; // 패키지 이름을 찾지 못한 경우 null 반환
//        }
//    }

    @Override
    public void onInterrupt() {
        // 서비스가 중단될 때 처리할 로직 (필요 시 구현)
    }

    // 앱 실행 횟수를 저장하는 메서드
    private void incrementClickCount(Context context, String packageName) {
        SharedPreferences prefs = context.getSharedPreferences("app_click_counts", Context.MODE_PRIVATE);
        int clickCount = prefs.getInt(packageName, 0);  // 해당 앱의 실행 횟수 가져오기
        prefs.edit().putInt(packageName, clickCount + 1).apply();  // 실행 횟수 증가 후 저장
    }

    // 실행 횟수를 가져오는 메서드 (나중에 사용할 수 있음)
    public static int getClickCount(Context context, String packageName) {
        SharedPreferences prefs = context.getSharedPreferences("app_click_counts", Context.MODE_PRIVATE);
        return prefs.getInt(packageName, 0);
    }



    private void saveJsonToFileORSERVER(JSONArray jsonArray) {
        String fileName = "app_data.json";
        // 내부 저장소에 JSON 파일 저장
        try (FileOutputStream outputStream = openFileOutput(fileName, Context.MODE_PRIVATE)) {
            // JSONArray를 들여쓰기 포함하여 JSON 데이터를 파일에 저장
            outputStream.write(jsonArray.toString(4).getBytes());
            Log.d(TAG, "JSON 파일이 내부 저장소에 저장되었습니다: " + fileName);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "JSON 파일 저장 실패: " + fileName);
        }

        SendServer sendServer = new SendServer(this);
        sendServer.sendJsonArrayToServer(jsonArray, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 실패 시 처리 로직
                Log.e(TAG, "서버 전송 실패: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // 성공적으로 서버로부터 응답 받았을 때 처리
                    Log.d(TAG, "서버 응답 성공: " + response.body().string());
                } else {
                    // 오류 응답 처리
                    Log.e(TAG, "서버 오류 응답: " + response.code());
                }
            }
        });
    }





    //구글 스크래핑addRecentAppCategory
    private void SaveJsonFile(String packageName, Context context) {
        StaticMethodClass.getCategoryFromPlayStore(packageName, new CategoryCallback() {
            @Override
            public void onCategoryFetched(String category) {
                Category = category;
                Log.d(TAG, "패키지 이름: " + packageName + "\n가져온 카테고리: " + category );
                latch.countDown(); // 카테고리를 가져온 후 카운트 감소
                SaveJsonFile();

            }
        });
    }
    @Override
    public void onCategoryFetched(String category) {
        Category = category; // 카테고리 저장
        Log.d(TAG, "가져온 카테고리: " + category);
        latch.countDown(); // 카운트 감소
        SaveJsonFile(); // JSON 파일 저장 호출
    }
}
