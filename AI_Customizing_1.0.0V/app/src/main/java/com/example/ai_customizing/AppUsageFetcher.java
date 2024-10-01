package com.example.ai_customizing;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.List;

public class AppUsageFetcher {
    private final String TAG = "AppUsageFetcher";
    private final UsageStatsManager usageStatsManager;
    private final PackageManager packageManager;

    public AppUsageFetcher(Context context) {
        usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        packageManager = context.getPackageManager();
    }

    public void fetchAndSaveAppsByCategory(Context context) throws PackageManager.NameNotFoundException {
        List<UsageStats> usageStatsList = getUsageStatsForLastMonth(); // 지난 한 달 동안 사용 기록 가져오기

        if (usageStatsList.isEmpty()) {
            Log.e("AppUsageFetcher", "사용 기록이 없습니다.");
            return;
        }

        // 앱 사용 시간을 기준으로 정렬
        usageStatsList.sort((o1, o2) -> Long.compare(o2.getTotalTimeInForeground(), o1.getTotalTimeInForeground()));



        // 앱 사용 통계와 사용자 설치 앱 목록을 결합
        for (UsageStats usageStats : usageStatsList) {
            String packageName = usageStats.getPackageName();
            try {
                ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);

                if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 || StaticMethodClass.isExceptionSystemApp(appInfo.packageName)) {
                    // Google Play 스토어에서 카테고리 정보를 비동기적으로 가져옴
                    fetchCategoryForApp(packageName, context, usageStats.getTotalTimeInForeground());
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "앱 정보를 찾을 수 없습니다: " + packageName, e);
            }
        }
    }

    private List<UsageStats> getUsageStatsForLastMonth() {
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.MONTH, -1); // 지난 한 달 동안의 데이터
        long startTime = calendar.getTimeInMillis();

        return usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
    }

    private void saveAppToFile(Context context, ExtendedApplicationInfo extendedInfo) {
        String categoryName = extendedInfo.getCategoryName(); // CATEGORY_ 제거 후 카테고리 이름 가져오기
        String fileName = categoryName + "_app.json"; //ex -> "VIDEO_PLAYERS_app.json"
        File file = new File(context.getFilesDir(), fileName);

        try {
            // JSON 파일에 저장할 내용 생성
            JSONObject appData = new JSONObject();
            appData.put("packageName", extendedInfo.getPackageName());
            appData.put("category", categoryName);
            appData.put("usageTime", extendedInfo.getUsageTime());

            JSONArray appList;
            if (file.exists()) {
                // 기존 파일이 존재하면 기존 데이터를 불러오기
                appList = Utils.readFileAll(context, fileName);
            } else {
                appList = new JSONArray();
            }


            // 동일한 패키지가 존재하는지 확인 후 사용 시간 업데이트
            boolean isUpdated = false;
            for (int i = 0; i < appList.length(); i++) {
                JSONObject existingAppData = appList.getJSONObject(i);
                String existingPackageName = existingAppData.getString("packageName");

                // 패키지가 동일하다면 usageTime 합산
                if (existingPackageName.equals(appData.getString("packageName"))) {
                    long existingUsageTime = existingAppData.getLong("usageTime");
                    long newUsageTime = existingUsageTime + appData.getLong("usageTime");
                    existingAppData.put("usageTime", newUsageTime); // 사용 시간 업데이트
                    isUpdated = true;
                    Log.d("AppUsageFetcher", "JSON 파일 저장중 동일한 패키지 존재, 사용 시간 업데이트: "
                            + existingUsageTime + " + " + appData.getLong("usageTime") + " = " + newUsageTime);
                    break;
                }
            }


            // 피싱 오류 발생 여부 확인 후 추가
            if (appList != null && !appData.getString("packageName").isEmpty() && !isUpdated) {
                appList.put(appData); // 앱 데이터를 배열에 추가
                Log.d("AppUsageFetcher", "새로운 앱 데이터를 JSON에 추가.");

                // 배열을 파일에 저장
                FileWriter writer = new FileWriter(file);
                writer.write(appList.toString(2));
                writer.flush();
                writer.close();
                Log.d("AppUsageFetcher", "JSON 파일이 잘 저장되었음");
            }

        } catch (JSONException e) {
            Log.e("AppUsageFetcher", "JSON 생성 중 오류 발생", e);
        } catch (Exception e) {
            Log.e("AppUsageFetcher", "앱 데이터를 파일에 저장하는 중 오류 발생", e);
        }
    }


    private void fetchCategoryForApp(String packageName, Context context, long usageTime) {
        StaticMethodClass.getCategoryFromPlayStore(packageName, new CategoryCallback() {
            @Override
            public void onCategoryFetched(String category) {
                Log.d(TAG, "패키지 이름: " + packageName + "\n가져온 카테고리: " + category +  "\n사용 시간: " + usageTime);
                // ExtendedApplicationInfo 객체 생성 및 초기화
                ExtendedApplicationInfo extendedInfo = new ExtendedApplicationInfo(packageName, category, usageTime);

                // 파일에 앱 정보를 저장
                saveAppToFile(context, extendedInfo);
            }
        });
    }
}

