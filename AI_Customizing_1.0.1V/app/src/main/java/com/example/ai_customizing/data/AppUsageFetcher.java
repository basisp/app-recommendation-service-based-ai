package com.example.ai_customizing.data;

import static com.example.ai_customizing.utils.Utils.saveAppToFileAll;
import static com.example.ai_customizing.utils.Utils.saveAppToFileCategory;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.example.ai_customizing.callbacks.CategoryCallback;
import com.example.ai_customizing.model.ExtendedApplicationInfo;
import com.example.ai_customizing.utils.StaticMethodClass;
import com.example.ai_customizing.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class AppUsageFetcher {
    private final String TAG = "AppUsageFetcher";
    private final UsageStatsManager usageStatsManager;
    private final PackageManager packageManager;
    private Context context;
    private int totalTasks;
    AtomicInteger completedTasks;
    public AppUsageFetcher(Context context) {
        usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        packageManager = context.getPackageManager();
        this.context = context;
    }

    public void fetchAndSaveAppsByCategory(Context context, OnAppDataFetchedListener listener) throws PackageManager.NameNotFoundException {
        //sharedPreferences 설정
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppUsagePrefs", Context.MODE_PRIVATE);
        long lastUpdateTime = sharedPreferences.getLong("lastUpdateTime", 0);
        long currentTime = System.currentTimeMillis();
        List<UsageStats> usageStatsList = Collections.emptyList();
        long timeDifference = currentTime - lastUpdateTime;

        if (lastUpdateTime == 0) {
            // 첫 실행 시 한 달 데이터 처리
            Log.d(TAG, "앱 사용 기록을 처음으로 정리합니다 (지난 한 달)");
            usageStatsList = getUsageStatsForLastMonth();
            sharedPreferences.edit().putLong("lastUpdateTime", currentTime).apply();
        } else if (timeDifference >= 24 * 60 * 60 * 1000) {
            // 24시간 이후 데이터 업데이트
            Log.d(TAG, "24시간 이후로 앱 사용 기록을 업데이트합니다.");

            // 마지막 업데이트 시간으로부터 현재 시간까지의 범위로 앱 사용 기록을 가져옵니다.
            usageStatsList = getUsageStatsBetween(lastUpdateTime, currentTime, context);

            // 현재 시간을 lastUpdateTime으로 업데이트합니다.
            sharedPreferences.edit().putLong("lastUpdateTime", currentTime).apply();
        } else {
            Log.d(TAG, "24시간 이내이므로 데이터를 업데이트하지 않습니다.");
        }


        // 현재 시간을 lastUpdateTime으로 업데이트합니다.
        sharedPreferences.edit().putLong("lastUpdateTime", currentTime).apply();

        if (usageStatsList.isEmpty()) {
            Log.e("AppUsageFetcher", "사용 기록이 없습니다.");
            return;
        }

        // 앱 사용 시간을 기준으로 정렬
        usageStatsList.sort((o1, o2) -> Long.compare(o2.getTotalTimeInForeground(), o1.getTotalTimeInForeground()));

        // 새 데이터를 저장할 List 생성
        List<ExtendedApplicationInfo> appList = new ArrayList<>();



        // 앱 사용 기록 먼저 정리
        for (UsageStats usageStats : usageStatsList) {
            String packageName = usageStats.getPackageName();
            try {
                ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);

                Optional<ExtendedApplicationInfo> matchingApp = appList.stream()
                        .filter(app -> app.getPackageName().equals(packageName))
                        .findFirst();
                if (matchingApp.isPresent()){ //packageName이 이미 존재하는 패키지라면
                    ExtendedApplicationInfo extendedInfo = matchingApp.get();
                    extendedInfo.setUsageTime(usageStats.getTotalTimeInForeground());
                    Log.d(TAG, "이미 extendedInfo안에 존재하는 패키지 // " +
                            "패키지 이름: " + packageName + "\n저장된 카테고리: " + extendedInfo.getCategoryName() +  "\n사용 시간: " + usageStats.getTotalTimeInForeground());


                    saveAppToFileCategory(context, extendedInfo);
                    saveAppToFileAll(context, extendedInfo);
                }
                else if(StaticMethodClass.isExceptionSystemApp(appInfo.packageName)){
                    //특정 패키지의 앱이라면 SYSTEM 카테고리를 붙임
                    ExtendedApplicationInfo extendedInfo = new ExtendedApplicationInfo
                            (packageName, "SYSTEM", usageStats.getTotalTimeInForeground());

                    appList.add(extendedInfo); // List에 추가
                    saveAppToFileCategory(context, extendedInfo);
                    saveAppToFileAll(context, extendedInfo);
                }
                else if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 ) { //시스탬 앱이 아닌 것만
                    // Google Play 스토어에서 카테고리 정보를 비동기적으로 가져옴
                    fetchCategoryForApp(packageName, context, usageStats.getTotalTimeInForeground(), appList);
                }
//                else {
//                    completedTasks.incrementAndGet();
//                    Log.d(TAG, "사용 하지 않는 시스탬 앱 // 전체 작업 수 :"  + totalTasks + "진행된 작업 수 : " + completedTasks.get());
//                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "앱 정보를 찾을 수 없습니다: " + packageName, e);
            }
        }



        // 모든 패키지를 가져와 사용하지 않은 앱도 All_app.json 파일에 추가하는 로직
        List<ApplicationInfo> allApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        // 전체 비동기 작업 추적
        totalTasks = allApps.size(); // 전체 작업 수
        completedTasks = new AtomicInteger(0); // 완료된 작업 수 추적
        Log.d(TAG, "UsageStats 반복문 시작전 확인 // 전체 작업 수 :"  + totalTasks + " 진행된 작업 수 : " + completedTasks.get());



        //전체 앱 정리(사용하지 않았지만 설치된 앱 탐색)
        for (ApplicationInfo appInfo : allApps) {
            String packageName = appInfo.packageName;
            // 시스템 앱은 제외
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                // List<ExtendedApplicationInfo>에서 해당 패키지가 이미 있는지 확인
                boolean isAlreadyInList = appList.stream().anyMatch(app -> app.getPackageName().equals(packageName));
                // List에 없는 앱만 처리
                if (!isAlreadyInList) {
                    // 시스템 앱이 아닌 경우 fetchCategoryForApp 사용
                    fetchCategoryForApp(packageName, context, 0, appList, new OnAppDataFetchedListener() {
                        @Override
                        public void onDataFetched() {
                            listener.onDataFetched();
                        }
                    });
                }
                else {
                    completedTasks.incrementAndGet();
                    Log.d(TAG, "이미 완료된 작업 // 전체 작업 수 :"  + totalTasks + "진행된 작업 수 : " + completedTasks.get());
                    Log.d(TAG, "appInfo : allApps => 이미 All_app.json에 추가된 앱");
               }
            }
            else{ // 쓸모없는 시스탬 앱을 경우
                completedTasks.incrementAndGet();
                Log.d(TAG, "시스탬 앱 // 전체 작업 수 :"  + totalTasks + "진행된 작업 수 : " + completedTasks.get());
            }
        }
    }
    //24시간의 사용 기록 가져오기 -> 정확히 24시간이 아닌 24시간 이후에 지난 시간을 더해서 사용하는 getUsageStatsBetween()로 대체
//    private List<UsageStats> getUsageStatsForLast24Hours() {
//        Calendar calendar = Calendar.getInstance();
//        long endTime = calendar.getTimeInMillis(); // 현재 시간
//        calendar.add(Calendar.HOUR_OF_DAY, -24);   // 24시간 전
//        long startTime = calendar.getTimeInMillis();
//
//        return usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
//    }

    //한달간의 기록 가져이고
    private List<UsageStats> getUsageStatsForLastMonth() {
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.MONTH, -1); // 지난 한 달 동안의 데이터
        long startTime = calendar.getTimeInMillis();

        return usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
    }

    //마지막 업데이트 로부터 시간 가져오기
    private List<UsageStats> getUsageStatsBetween(long startTime, long endTime, Context context) {
        // UsageStatsManager를 사용하여 지정된 시간 범위 내의 앱 사용 통계를 가져오는 로직을 구현합니다.
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        // UsageStats를 가져옵니다.
        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, // 하루 단위로 조회
                startTime, // 시작 시간
                endTime // 종료 시간
        );

        return usageStatsList;
    }








    private void fetchCategoryForApp(String packageName, Context context, long usageTime, List<ExtendedApplicationInfo> appList, OnAppDataFetchedListener liste) {
        StaticMethodClass.getCategoryFromPlayStore(packageName, new CategoryCallback() {
            @Override
            public void onCategoryFetched(String category) {
                Log.d(TAG, "재정의2(콜백 사용) // 패키지 이름: " + packageName + "\n가져온 카테고리: " + category +  "\n사용 시간: " + usageTime);

                // ExtendedApplicationInfo 객체 생성 및 초기화
                ExtendedApplicationInfo extendedInfo = new ExtendedApplicationInfo(packageName, category, usageTime);
                appList.add(extendedInfo); // List에 추가

                // 파일에 앱 정보를 저장
                saveAppToFileCategory(context, extendedInfo);
                saveAppToFileAll(context, extendedInfo);

                completedTasks.incrementAndGet();
                Log.d(TAG, "카테고리 요청 // 전체 작업 수 :"  + totalTasks + "진행된 작업 수 : " + completedTasks.get() + "\n\n");
                if (completedTasks.get() == totalTasks) {
                    if (liste != null) {
                        liste.onDataFetched(); // 모든 작업 완료 후 콜백 호출
                    }
                }
            }
        });
    }

    private void fetchCategoryForApp(String packageName, Context context, long usageTime, List<ExtendedApplicationInfo> appList) {
        StaticMethodClass.getCategoryFromPlayStore(packageName, new CategoryCallback() {
            @Override
            public void onCategoryFetched(String category) {
                Log.d(TAG, "재정의1(콜백 사용XXX) 패키지 이름: " + packageName + "\n가져온 카테고리: " + category +  "\n사용 시간: " + usageTime);

                // ExtendedApplicationInfo 객체 생성 및 초기화
                ExtendedApplicationInfo extendedInfo = new ExtendedApplicationInfo(packageName, category, usageTime);
                appList.add(extendedInfo); // List에 추가

                // 파일에 앱 정보를 저장
                saveAppToFileCategory(context, extendedInfo);
                saveAppToFileAll(context, extendedInfo);
            }
        });
    }


}

