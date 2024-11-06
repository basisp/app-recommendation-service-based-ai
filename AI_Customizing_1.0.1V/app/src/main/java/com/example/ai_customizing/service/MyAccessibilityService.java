
package com.example.ai_customizing.service;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import androidx.core.app.NotificationCompat;

import com.example.ai_customizing.R;
import com.example.ai_customizing.model.Member;
import com.example.ai_customizing.network.SendServer;
import com.example.ai_customizing.utils.StaticMethodClass;
import com.example.ai_customizing.utils.Utils;
import com.example.ai_customizing.callbacks.CategoryCallback;
import com.example.ai_customizing.widget.Customizing_widget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MyAccessibilityService extends AccessibilityService implements CategoryCallback {
    private final String TAG = "MyAccessibilityService";
    private String lastPackageName = "";
    private String Category;
    private final List<String> recentApps = new ArrayList<>(); // 최근 실행된 앱 목록 , 재할당 가능성 없는 경고
    private final List<Long> TimeStamp = new ArrayList<Long>();
    SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        // Android 8.0 이상에서 알림 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "DATASYN_CHANNEL",
                    "앱 추천 서비스",
                    NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        sharedPreferences = getApplicationContext().getSharedPreferences("AppUsagePrefs", Context.MODE_PRIVATE);

        // 포그라운드 서비스로 시작하기
        Notification notification = new NotificationCompat.Builder(this, "DATASYN_CHANNEL")
                .setContentTitle("Widget Customizing Service")
                .setContentText("앱 추천 서비스가 실행 중입니다.")
                .setSmallIcon(R.drawable.ic_service_icon) // 알림 아이콘
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        startForeground(1, notification);
    }

    @Override
    public void onDestroy(){
        stopForeground(true); //포어그라운드 서비스 종료
    }

    private void updateAllWidgets() {
        // AppWidgetManager 인스턴스 얻기
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        // 현재 위젯의 ID 가져오기
        ComponentName widgetComponent = new ComponentName(this, Customizing_widget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent);

        // 위젯 전체 업데이트 호출
        for (int appWidgetId : appWidgetIds) {
            // updateAppWidget 직접 호출
            Customizing_widget.updateAppWidgetClick(this, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "onAccessibilityEvent 호출됨");
        lastPackageName = sharedPreferences.getString("lastPackageName", "null");
        //이벤트 타입 윈도우 상태 변화.
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName().toString();
            addTimeStamp(); //타임 스템프 시스템 시간 찍기(밀리초)
            PackageManager packageManager = getPackageManager();
            try {
                ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);

                if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 || StaticMethodClass.isExceptionSystemApp(appInfo.packageName)) {
                    if (!lastPackageName.equals(packageName)) {
                        incrementClickCount(this, packageName);
                        Log.d(TAG, "앱 실행됨: " + packageName);
                        lastPackageName = packageName;
                        sharedPreferences.edit().putString("lastPackageName", lastPackageName).apply();
                        // 최근 앱 리스트에 추가
                        addRecentApp(packageName);
                        //내부 데이터에서 앱을 찾았는지 못 찾았는지.
                        boolean Discovery = false;
                        //json 파일에서 앱 패키 이름이 있는지 검사
                        JSONArray jsonArray = Utils.readFileAll(this, "All_app.json");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject existingAppData = jsonArray.getJSONObject(i);
                            String existingPackageName = existingAppData.getString("package_name");

                            // 패키지가 동일한것을 찾는 과정
                            if (existingPackageName.equals(packageName)) {
                                String existingUsageCategory = existingAppData.getString("category");
                                Log.d(TAG, "All_app.json에 해당하는 앱 정보 발견, 카테고리를 가져옵니다.");
                                Category = existingUsageCategory;
                                Discovery = true;
                                break;
                            }
                        }


                        // Json파일로 저장, 필요시 서버로 보내는 함수 포함되어있음
                        if(Discovery){ // 앱 정보를 찾았을 때
                            SaveJsonFile();
                        }
                        else { // 앱 정보를 찾지 못했을 때
                            SaveJsonFile(packageName, this);
                            Log.d(TAG, "All_app.json에 해당하는 앱을 찾는데 실패했습니다. 구글 스크래핑을 이용합니다.");
                        }

                    } else {
                        Log.d(TAG, "실행 리스트에 있는 앱 무시됨 - " + packageName);
                    }
                } else {
                    Log.d(TAG, "시스템 앱 실행됨, 무시됨: " + packageName);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                Log.d(TAG, "앱 패키지 정보를 찾지 못함 - " + packageName);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void addRecentApp(String packageName) {
        // 최근 앱 목록에 추가
        recentApps.add(packageName);
        // 리스트가 3개를 초과하면 첫 번째 앱 제거
        if (recentApps.size() > 3) {
            recentApps.remove(0);
        }
    }

    private void addTimeStamp() {
        // 최근 앱 목록에 추가
        TimeStamp.add(System.currentTimeMillis());
        // 리스트가 3개를 초과하면 첫 번째 앱 제거
        if (TimeStamp.size() > 3) {
            TimeStamp.remove(0);
        }
    }

    @Override
    public void onInterrupt() {
        // 서비스가 중단될 때 처리할 로직 (필요 시 구현)
        // 파일 경로 정의 (어플리케이션의 파일 디렉토리 안에 위치한다고 가정)
        File file = new File(getApplicationContext().getFilesDir(), "app_data.json");

        // 파일이 존재하는지 확인하고 삭제
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                Log.d("MyAccessibilityService", "app_data.json 파일이 성공적으로 삭제되었습니다.");
            } else {
                Log.e("MyAccessibilityService", "app_data.json 파일 삭제에 실패했습니다.");
            }
        } else {
            Log.d("MyAccessibilityService", "app_data.json 파일이 존재하지 않습니다.");
        }
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

    //구글 스크래핑addRecentAppCategory
    private void SaveJsonFile(String packageName, Context context) {
        StaticMethodClass.getCategoryFromPlayStore(packageName, new CategoryCallback() {
            @Override
            public void onCategoryFetched(String category) throws JSONException {
                Category = category;
                Log.d(TAG, "패키지 이름: " + packageName + "\n가져온 카테고리: " + category );
                SaveJsonFile();
            }
        });
    }
    @Override
    public void onCategoryFetched(String category) throws JSONException {
        Category = category; // 카테고리 저장
        Log.d(TAG, "가져온 카테고리: " + category);
        SaveJsonFile(); // JSON 파일 저장 호출
    }

    private void SaveJsonFile() throws JSONException {
        // 다운로드 폴더에서 JSON 파일을 읽어옴
        JSONObject mainJsonObject = Utils.readFileApp_data(this, "app_data.json");

        JSONArray jsonArray;


        if (mainJsonObject == null || !mainJsonObject.has("apps")) {
            // 파일이 없거나 "apps" 배열이 없으면 새로 생성
            mainJsonObject = new JSONObject();
            jsonArray = new JSONArray();
            Log.d(TAG, "app_data.json을 새로 생성합니다.");

            // username 필드를 mainJsonObject에 추가
            Member member = new Member(this);
            mainJsonObject.put("username", member.getUsername());
        } else {
            // JSON 파일에서 "apps" 배열을 가져옴
            jsonArray = mainJsonObject.getJSONArray("apps");
            Log.d(TAG, "app_data.json에 새로운 데이터를 추가합니다.");

            // username이 없을 경우 기본 값을 설정
            if (!mainJsonObject.has("username")) {
                Member member = new Member(this);
                mainJsonObject.put("username", member.getUsername());
            }
        }

        try {
            // 새로운 앱 데이터 추가
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("package_name", recentApps.get(recentApps.size() - 1));
            jsonObject.put("category", Category);
            jsonObject.put("timestamp", TimeStamp.get(TimeStamp.size() - 1));

            // "apps" 배열에 새로운 데이터 추가
            jsonArray.put(jsonObject);

            // "apps" 배열의 크기가 4개 이상이면 첫 번째 데이터를 제거
            if (jsonArray.length() > 3) {
                JSONArray newJsonArray = new JSONArray();
                for (int i = jsonArray.length() - 3; i < jsonArray.length(); i++) { // 마지막 3개 요소만 유지
                    newJsonArray.put(jsonArray.get(i));
                }
                jsonArray = newJsonArray;
            }

            // "apps" 배열을 mainJsonObject에 다시 넣음
            mainJsonObject.put("apps", jsonArray);

        } catch (JSONException e) {
            Log.e(TAG, "app_data.json 정렬중 오류 발생", e);
            e.printStackTrace();
        }

        // JSON 객체를 파일에 저장
        saveJsonToFileORSERVER(mainJsonObject);
    }

    private void saveJsonToFileORSERVER(JSONObject mainJsonObject) throws JSONException { // JSONArray -> JSONObject로 수정
        String fileName = "app_data.json";

        // 내부 저장소에 JSON 파일 저장
        try (FileOutputStream outputStream = openFileOutput(fileName, Context.MODE_PRIVATE)) {
            outputStream.write(mainJsonObject.toString(4).getBytes()); // 들여쓰기 포함하여 JSON 데이터를 저장
            Log.d(TAG, "JSON 파일이 내부 저장소에 저장되었습니다: " + fileName);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "JSON 파일 저장 실패: " + fileName);
        }




        SendServer sendServer = new SendServer(this);
        sendServer.sendJsonArrayToServer(mainJsonObject, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 실패 시 처리 로직
                Log.e(TAG, "서버 송신/수신 실패: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);  // !수정: 서버 응답을 JSONObject로 처리
                        JSONArray appArray = jsonResponse.getJSONArray("apps");  // !수정: "apps" 배열을 추출

                        // !추가: JSONArray 데이터를 문자열로 출력
                        Log.d(TAG, "전체 JSONArray 데이터: " + appArray.toString());

                        // 패키지 이름과 카테고리 추출 및 저장
                        for (int i = 0; i < appArray.length(); i++) {
                            JSONObject appInfo = appArray.getJSONObject(i);
                            String packageName = appInfo.getString("package_name");  // !수정: key명을 서버 응답에 맞게 변경
                            String category = appInfo.getString("category");

                            // 패키지 이름과 카테고리를 SharedPreferences에 저장
                            SharedPreferences preferences = getSharedPreferences("AppData", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("package_name" + i, packageName);
                            editor.putString("category" + i, category);
                            editor.apply();
                        }

                        updateAllWidgets();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.e(TAG, "서버 오류 응답: " + response.code());
                }
            }
        });
    }
}
