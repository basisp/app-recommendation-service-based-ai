package com.example.ai_customizing.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.example.ai_customizing.model.ExtendedApplicationInfo;
import com.example.ai_customizing.widget.Customizing_widget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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

    //해당 인덱스의 패키지 네임을 얻어오는 함수 //  인덱스 범위 : 0~설치된 앱의 수
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

    public static void saveAppToFileCategory(Context context, ExtendedApplicationInfo extendedInfo) {
        String categoryName = extendedInfo.getCategoryName(); // CATEGORY_ 제거 후 카테고리 이름 가져오기
        Log.d(TAG, "saveAppToFileCategory : categoryName = " + categoryName);
        String fileNameCategory = categoryName + "_app.json"; //ex -> "VIDEO_PLAYERS_app.json"
        File fileCate = new File(context.getFilesDir(), fileNameCategory);

        try {
            // JSON 파일에 저장할 내용 생성
            JSONObject appData = new JSONObject();
            appData.put("package_name", extendedInfo.getPackageName());
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
                String existingPackageName = existingAppData.getString("package_name");

                // 패키지가 동일하다면 usageTime 합산
                if (existingPackageName.equals(appData.getString("package_name"))) {
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
            if (appListCate != null && !appData.getString("package_name").isEmpty()) {
                // 업데이트된 경우, 적절한 위치에 재배치
                if (isUpdated) {
                    JSONObject updatedAppData = appListCate.getJSONObject(updatedIndex);

                    // 새로운 위치 찾기
                    while (updatedIndex > 0) {
                        JSONObject existingAppData = appListCate.getJSONObject(updatedIndex-1); // 기존 파일에 대한 포인터 개념
                        long existingUsageTime = existingAppData.getLong("usageTime");


                        if (updatedAppData.getLong("usageTime") <= existingUsageTime) {
                            break;
                        }
                        else{// 업데이트된 앱의 사용 시간이 이전의 앱보다 더 많다면 한칸 위로 올라간다.
                            appListCate.put(updatedIndex, existingAppData);
                            appListCate.put(updatedIndex-1, appData);
                            Log.d(TAG, categoryName + "_app.json사용 시간 업데이트 후 중요도가 올라감. : " + updatedAppData.getString("package_name"));
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

    public static void saveAppToFileAll(Context context, ExtendedApplicationInfo extendedInfo) {
        String fileNameAll = "All_app.json"; //ex -> "VIDEO_PLAYERS_app.json"
        File fileAll = new File(context.getFilesDir(), fileNameAll);

        String categoryName = extendedInfo.getCategoryName(); // 카테고리 가져오기
        try {
            // JSON 파일에 저장할 내용 생성
            JSONObject appData = new JSONObject();
            appData.put("package_name", extendedInfo.getPackageName());
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
                Log.d("AppUsageFetcher", "All_app.JSON 파일이 잘 저장되었음\n\n");
                return;
            }

            // 동일한 패키지가 존재하는지 확인 후 사용 시간 업데이트
            boolean isUpdated = false;
            int updatedIndex = -1; // 업데이트된 인덱스 초기화
            // 동일한 패키지가 존재하는지 확인 후 존재하면 메소드 종료
            for (int i = 0; i < appListAll.length(); i++) {
                JSONObject existingAppData = appListAll.getJSONObject(i);
                String existingPackageName = existingAppData.getString("package_name");

                // 패키지가 동일하다면 usageTime 합산
                if (existingPackageName.equals(appData.getString("package_name"))) {
                    long existingUsageTime = existingAppData.getLong("usageTime");
                    long newUsageTime = existingUsageTime + appData.getLong("usageTime");
                    existingAppData.put("usageTime", newUsageTime); // 사용 시간 업데이트
                    isUpdated = true;
                    updatedIndex = i; // 업데이트된 인덱스 저장
                    Log.d(TAG, "All_app.JSON 파일 저장중 동일한 패키지("+ existingPackageName +")존재, 사용 시간 업데이트: "
                            + existingUsageTime + " + " + appData.getLong("usageTime") + " = " + newUsageTime);
                    break;
                }
            }

            // 피싱 오류 발생 여부 확인 후 추가
            if (appListAll != null && !appData.getString("package_name").isEmpty()) {
            // 업데이트된 경우, 적절한 위치에 재배치
                if (isUpdated) {
                    JSONObject updatedAppData = appListAll.getJSONObject(updatedIndex);

                    // 새로운 위치 찾기
                    while (updatedIndex > 0) {
                        JSONObject existingAppData = appListAll.getJSONObject(updatedIndex-1); // 기존 파일에 대한 포인터 개념
                        long existingUsageTime = existingAppData.getLong("usageTime");


                        if (updatedAppData.getLong("usageTime") <= existingUsageTime) {
                            break;
                        }
                        else{// 업데이트된 앱의 사용 시간이 이전의 앱보다 더 많다면 한칸 위로 올라간다.
                            appListAll.put(updatedIndex, existingAppData);
                            appListAll.put(updatedIndex-1, appData);
                            Log.d(TAG, "All_app.JSON 사용 시간 업데이트 후 중요도가 올라감. : " + updatedAppData.getString("package_name"));
                        }
                        updatedIndex--;
                    }

                    // 현재 데이터에는 appData의 시간이 있으므로 최신 사용 시간으로 업데이트.
                    appListAll.put(updatedIndex, updatedAppData);
                } else {
                    // 새로운 패키지인 경우
                    appListAll.put(appData); // 앱 데이터를 배열에 추가
                    Log.d("AppUsageFetcher", "All_app.JSON애 새로운 앱 추가 : " + extendedInfo.getPackageName());
                }

                // json 파일에 저장
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

    public static void DeleteAppToFileCategory(Context context, String packageName, String categoryName) {
        Log.d(TAG, "saveAppToFileCategory : categoryName = " + categoryName);
        String fileNameCategory = categoryName + "_app.json"; //ex -> "VIDEO_PLAYERS_app.json"
        File fileCate = new File(context.getFilesDir(), fileNameCategory);

        try {
            // JSON 파일에 저장할 내용 생성
            JSONObject appData = new JSONObject();
            appData.put("package_name", packageName);

            JSONArray appListCate;
            if (fileCate.exists()) {
                // 기존 파일이 존재하면 기존 데이터를 불러오기
                appListCate = Utils.readFileAll(context, fileNameCategory);
            } else {
                Log.d(TAG, "DeleteAppToFileCategory: 파일이 존재하지 않습니다.");
                return;
            }

            // 삭제할 패키지의 JsonArray 객체 상의 인덱스 확인

            for (int i = 0; i < appListCate.length(); i++) {
                JSONObject existingAppData = appListCate.getJSONObject(i);
                String existingPackageName = existingAppData.getString("package_name");

                // 삭제할 패키지가 찾았다면 1-1
                if (existingPackageName.equals(packageName)) {
                    // 해당 위치의 Json 객체를 삭제 1-2
                    appListCate.remove(i); // 해당 인덱스에 있는 객체 삭제
                    Log.d(TAG, "DeleteAppToFileCategory : 패키지(" + packageName + ")의 객체 확인");
                    break;
                }
            }

            // 3. 수정된 객체를 파일에 저장
            FileWriter writer = new FileWriter(fileCate);
            writer.write(appListCate.toString(2));
            writer.flush();
            writer.close();
            Log.d(TAG, "DeleteAppToFileCategory : 앱을 성공적으로 삭제");
        } catch (JSONException e) {
            Log.e(TAG, "DeleteAppToFileCategory :  생성 중 오류 발생", e);
        } catch (Exception e) {
            Log.e(TAG, "DeleteAppToFileCategory :  저장 중 오류 발생", e);
        }
    }

    public static void DeleteAppToFileAll(Context context, String packageName) {
        String fileNameAll = "All_app.json"; //ex -> "VIDEO_PLAYERS_app.json"
        File fileAll = new File(context.getFilesDir(), fileNameAll);
        String categoryName = "";

        try {
            // JSON 파일에 저장할 내용 생성
            JSONObject appData = new JSONObject();


            JSONArray appListCate;
            if (fileAll.exists()) {
                // 기존 파일이 존재하면 기존 데이터를 불러오기
                appListCate = Utils.readFileAll(context, fileNameAll);
            } else {
                Log.d(TAG, "DeleteAppToFileAll : 파일이 존재하지 않습니다.");
                return;
            }


            for (int i = 0; i < appListCate.length(); i++) {
                JSONObject existingAppData = appListCate.getJSONObject(i);
                String existingPackageName = existingAppData.getString("package_name");

                // 삭제할 패키지가 찾았다면 1-1
                if (existingPackageName.equals(packageName)) {
                    // 해당 위치의 카테고리 정보 받아옴
                    categoryName = existingAppData.getString("category");
                    // 해당 위치의 Json 객체를 삭제 1-2
                    appListCate.remove(i); // 해당 인덱스에 있는 객체 삭제
                    Log.d(TAG, "DeleteAppToFileAll : 패키지(" + packageName + ")의 객체 확인");
                    break;
                }
            }

            // 3. 수정된 객체를 파일에 저장
            FileWriter writer = new FileWriter(fileAll);
            writer.write(appListCate.toString(2));
            writer.flush();
            writer.close();
            Log.d(TAG, "DeleteAppToFileAll : 앱을 성공적으로 삭제");

            DeleteAppToFileCategory(context, packageName, categoryName);
        } catch (JSONException e) {
            Log.e(TAG, "DeleteAppToFileAll :  생성 중 오류 발생", e);
        } catch (Exception e) {
            Log.e(TAG, "DeleteAppToFileAll :  저장 중 오류 발생", e);
        }
    }
}
