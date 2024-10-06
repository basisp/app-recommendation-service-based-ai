package com.example.ai_customizing.service;
// 최적화 가능성 매우우우우 높음
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.example.ai_customizing.utils.StaticMethodClass;

import org.json.JSONObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewAppInstallReceiver extends BroadcastReceiver {

    private static final String PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=";
    private static final String TAG = "NewAppInstallReceiver";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // 비동기 처리용 Executor





    @Override
    public void onReceive(Context context, Intent intent) {
        // 인텐트에서 액션과 설치된 앱의 패키지 이름을 가져옴
        String action = intent.getAction();
        String packageName = ""; // 초기화

        // getData()가 null인지 체크
        if (intent.getData() != null) {
            packageName = intent.getData().getSchemeSpecificPart();
        } else {
            Log.d(TAG, "인텐트에 정보가 없습니다.");
        }

        // 앱이 설치되었을 때 동작
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            Log.d(TAG, "새로운 앱이 설치되었습니다.: " + packageName);
            // 비동기로 설치된 앱의 정보를 처리
            final String finalPackageName = packageName;
            executorService.execute(() -> sendNewAppInfo(context, finalPackageName)); // finalPackageName 사용
        }
        else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)){
            Log.d(TAG, "앱 삭제는 기가 막히게 아네.: " + packageName);
        }
    }




    // 새로 설치된 앱의 정보를 터미널에 출력하고 서버로 전송
    private void sendNewAppInfo(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            String appName = pm.getApplicationLabel(appInfo).toString();
            String category = StaticMethodClass.getCategoryFromPlayStore(packageName);

            // JSON 객체 생성
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("packageName", packageName);
            jsonPayload.put("appName", appName);
            jsonPayload.put("category", category);

            // 터미널에 새로 설치된 앱 정보 출력
            System.out.println("새로 설치된 앱 정보:");
            System.out.println(jsonPayload.toString(4)); // 보기 좋게 들여쓰기

            // 서버로 앱 정보 전송 (SendServer 사용)
            // SendServer.sendJsonToServer(jsonPayload);
        } catch (Exception e) {
            Log.e(TAG, "에러, 새로운 앱에 대한 정보가 없음", e);
        }
    }


}
