package com.example.ai_customizing.data;

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
import java.util.Calendar;
import java.util.Collections;
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
        //알람 설정
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
                if(StaticMethodClass.isExceptionSystemApp(appInfo.packageName)){ //특정 패키지의 앱이라면 SYSTEM 카테고리를 붙임
                    ExtendedApplicationInfo extendedInfo = new ExtendedApplicationInfo(packageName, "SYSTEM", usageStats.getTotalTimeInForeground());
                    // 파일에 앱 정보를 저장
                    saveAppToFileCategory(context, extendedInfo);
                    saveAppToFileAll(context, extendedInfo);
                }
                else if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 ) { //시스탬 앱이 아닌 것만
                    // Google Play 스토어에서 카테고리 정보를 비동기적으로 가져옴
                    fetchCategoryForApp(packageName, context, usageStats.getTotalTimeInForeground());
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "앱 정보를 찾을 수 없습니다: " + packageName, e);
            }
        }


    }
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

    private void saveAppToFileCategory(Context context, ExtendedApplicationInfo extendedInfo) {
        String categoryName = extendedInfo.getCategoryName(); // CATEGORY_ 제거 후 카테고리 이름 가져오기
        String fileNameCategory = categoryName + "_app.json"; //ex -> "VIDEO_PLAYERS_app.json"
        File fileCate = new File(context.getFilesDir(), fileNameCategory);

        try {
            // JSON 파일에 저장할 내용 생성
            JSONObject appData = new JSONObject();
            appData.put("packageName", extendedInfo.getPackageName());
            appData.put("category", categoryName);
            appData.put("usageTime", extendedInfo.getUsageTime());

            JSONArray appListCate;
            if (fileCate.exists()) {
                // 기존 파일이 존재하면 기존 데이터를 불러오기
                appListCate = Utils.readFileAll(context, fileNameCategory);
            } else {
                appListCate = new JSONArray();
            }

            // 동일한 패키지가 존재하는지 확인 후 사용 시간 업데이트
            boolean isUpdated = false;
            int updatedIndex = -1; // 업데이트된 인덱스 초기화

            for (int i = 0; i < appListCate.length(); i++) {
                JSONObject existingAppData = appListCate.getJSONObject(i);
                String existingPackageName = existingAppData.getString("packageName");

                // 패키지가 동일하다면 usageTime 합산
                if (existingPackageName.equals(appData.getString("packageName"))) {
                    long existingUsageTime = existingAppData.getLong("usageTime");
                    long newUsageTime = existingUsageTime + appData.getLong("usageTime");
                    existingAppData.put("usageTime", newUsageTime); // 사용 시간 업데이트
                    isUpdated = true;
                    updatedIndex = i; // 업데이트된 인덱스 저장
                    Log.d("AppUsageFetcher", categoryName + "_app.json 파일 저장중 동일한 패키지("+ existingPackageName +")존재, 사용 시간 업데이트: "
                            + existingUsageTime + " + " + appData.getLong("usageTime") + " = " + newUsageTime);
                    break;
                }
            }

            // 피싱 오류 발생 여부 확인 후 추가
            if (appListCate != null && !appData.getString("packageName").isEmpty()) {
                // 업데이트된 경우, 적절한 위치에 재배치
                if (isUpdated) {
                    JSONObject updatedAppData = appListCate.getJSONObject(updatedIndex);

                    // 새로운 위치 찾기
                    while (updatedIndex > 0) {
                        JSONObject existingAppData = appListCate.getJSONObject(updatedIndex-1); // 기존 파일에 대한 포인터 개념
                        long existingUsageTime = existingAppData.getLong("usageTime");


                        if (updatedAppData.getLong("usageTime") < existingUsageTime) {
                            break;
                        }
                        else{// 업데이트된 앱의 사용 시간이 이전의 앱보다 더 많다면 한칸 위로 올라간다.
                            appListCate.put(updatedIndex, existingAppData);
                            appListCate.put(updatedIndex-1, appData);
                            Log.d(TAG, categoryName + "_app.json사용 시간 업데이트 후 중요도가 올라감. : " + updatedAppData.getString("packageName"));
                        }
                        updatedIndex--;
                    }

                    // 현재 데이터에는 appData의 시간이 있으므로 최신 사용 시간으로 업데이트.
                    appListCate.put(updatedIndex, updatedAppData);
                } else {
                    // 새로운 패키지인 경우
                    appListCate.put(appData); // 앱 데이터를 배열에 추가
                    Log.d("AppUsageFetcher", categoryName + "_app.json애 새로운 앱 추가 : " + extendedInfo.getPackageName());
                }

                // 배열을 파일에 저장
                FileWriter writer = new FileWriter(fileCate);
                writer.write(appListCate.toString(2));
                writer.flush();
                writer.close();
                Log.d("AppUsageFetcher", categoryName + "_app.json이 잘 저장되었음");
            }

        } catch (JSONException e) {
            Log.e("AppUsageFetcher", categoryName + "_app.json 생성 중 오류 발생", e);
        } catch (Exception e) {
            Log.e("AppUsageFetcher", categoryName + "_app.json 저장 중 오류 발생", e);
        }
    }



    private void saveAppToFileAll(Context context, ExtendedApplicationInfo extendedInfo) {
        String fileNameAll = "All_app.json"; //ex -> "VIDEO_PLAYERS_app.json"
        File fileAll = new File(context.getFilesDir(), fileNameAll);

        String categoryName = extendedInfo.getCategoryName(); // 카테고리 가져오기
        try {
            // JSON 파일에 저장할 내용 생성
            JSONObject appData = new JSONObject();
            appData.put("packageName", extendedInfo.getPackageName());
            appData.put("category", categoryName);
            appData.put("usageTime", extendedInfo.getUsageTime());

            JSONArray appListAll;
            if (fileAll.exists()) {
                // 기존 파일이 존재하면 기존 데이터를 불러오기
                appListAll = Utils.readFileAll(context, fileNameAll);
            } else {
                appListAll = new JSONArray();
                appListAll.put(appData); // 앱 데이터를 배열에 추가


                // 배열을 파일에 저장
                FileWriter writer = new FileWriter(fileAll);
                writer.write(appListAll.toString(2));
                writer.flush();
                writer.close();
                Log.d("AppUsageFetcher", "All_app.JSON 파일이 잘 저장되었음");
                return;
            }


            int updatedIndex = -1; // 업데이트된 인덱스 초기화

            // 동일한 패키지가 존재하는지 확인 후 사용 시간 업데이트
            for (int i = 0; i < appListAll.length(); i++) {
                JSONObject existingAppData = appListAll.getJSONObject(i);
                String existingPackageName = existingAppData.getString("packageName");

                // 패키지가 동일하다면 usageTime 합산후 메소드 종료
                if (existingPackageName.equals(appData.getString("packageName"))) {
                    long existingUsageTime = existingAppData.getLong("usageTime");
                    long newUsageTime = existingUsageTime + appData.getLong("usageTime");

                    existingAppData.put("usageTime", newUsageTime); // 사용 시간 업데이트

                    updatedIndex = i;
                    Log.d(TAG, "동일한 패키지("+ existingPackageName +")존재 All_app.json에서 사용시간 업데이트: "
                            + existingUsageTime + " + " + appData.getLong("usageTime") + " = " + newUsageTime);
                    break;
                }
            }

            // 피싱 오류 발생 여부 확인 후 추가
            if (appListAll != null && !appData.getString("packageName").isEmpty()) {
                // SYSTEM때문에 무조건 새로운 앱이더라도 새로운 위치를 찾아야 함.
                if(updatedIndex == -1) {
                    appListAll.put(appData);
                    updatedIndex = appListAll.length()-1;
                    Log.d(TAG, "All_app.json에 새로운 앱을 추가합니다. : " + extendedInfo.getPackageName());
                }

                //앱의 추가한 위치의 포인터를 받아옴
                JSONObject updatedAppData = appListAll.getJSONObject(updatedIndex);

                // 적절한 위치 찾기
                while (updatedIndex > 0) {
                    JSONObject existingAppData = appListAll.getJSONObject(updatedIndex - 1);
                    long existingUsageTime = existingAppData.getLong("usageTime");


                    if (updatedAppData.getLong("usageTime") < existingUsageTime) {
                        Log.d(TAG, "All_app.json - 올바른 위치//" + "(업데이트 or NewApp) 사용 시간 : " + updatedAppData.getLong("usageTime") + " // 위의 앱 시간 : " + existingUsageTime);
                        break;
                    } else {// 업데이트된 앱의 사용 시간이 이전의 앱보다 더 많다면 한칸 위로 올라간다.
                        appListAll.put(updatedIndex, existingAppData);
                        appListAll.put(updatedIndex - 1, appData);
                        Log.d(TAG, "All_app.json에 - (업데이트 or NewApp)사용 시간에 따른  중요도 상승("+updatedIndex+") : " + updatedAppData.getString("packageName") + "++ //" + existingAppData.getString("packageName") + "--" );
                    }
                    updatedIndex--;
                }

                // 현재 데이터에는 appData의 시간이 있으므로 최신 사용 시간으로 업데이트.
                appListAll.put(updatedIndex, updatedAppData);

                // 배열을 파일에 저장
                FileWriter writer = new FileWriter(fileAll);
                writer.write(appListAll.toString(2));
                writer.flush();
                writer.close();
                Log.d("AppUsageFetcher", "All_app.JSON 파일이 잘 저장되었음");
            }

        } catch (JSONException e) {
            Log.e("AppUsageFetcher", "All_app.JSON 생성 중 오류 발생", e);
        } catch (Exception e) {
            Log.e("AppUsageFetcher", "All_app.JSON 저장 중 오류 발생", e);
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
                saveAppToFileCategory(context, extendedInfo);
                saveAppToFileAll(context, extendedInfo);
            }
        });
    }


}

