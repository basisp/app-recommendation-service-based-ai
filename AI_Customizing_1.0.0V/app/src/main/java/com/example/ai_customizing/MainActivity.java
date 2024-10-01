package com.example.ai_customizing;


import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    List<AppInfo> appInfoList1 = new ArrayList<>();
    List<AppInfo> appInfoList2 = new ArrayList<>();
    String TAG = "MainActivity";

    AppInfoAdapter adapter1 = new AppInfoAdapter(appInfoList1);
    AppInfoAdapter adapter2 = new AppInfoAdapter(appInfoList2);

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String FIRST_RUN_KEY = "isFirstRun";

    private final NewAppInstallReceiver newAppInstallReceiver = new NewAppInstallReceiver();

    private final ActivityResultLauncher<Intent> usageAccessLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (hasUsageStatsPermission()) {
                    Log.d(TAG, "사용 기록 권한이 허용되었습니다.");
                } else {
                    Log.e(TAG, "사용 기록 권한이 거부되었습니다.");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // 로그인 상태 확인 (예: SharedPreferences에서 "isLoggedIn" 값 확인)
        SharedPreferences preferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            // 로그인이 되어 있지 않으면 server_main_activity.xml을 표시할 액티비티로 이동
            Intent intent = new Intent(MainActivity.this, ServerMainActivity.class);
            startActivity(intent);
            finish(); // 메인 액티비티를 종료하여 뒤로가기 시 메인 화면으로 돌아가지 않게 함
        } else {
            // 로그인이 되어 있으면 메인 화면을 표시
            setContentView(R.layout.activity_main);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        //이곳에서는 권환을 요청합니다.

//접근성을 요청합니다. -> 요청하지 않으면 실시간 앱 패턴 분석 불가.
        if (!isAccessibilityServiceEnabled(MyAccessibilityService.class)) {
            // 접근성 권한이 활성화되지 않았을 때 사용자에게 권한 설정을 유도
            new AlertDialog.Builder(this)
                    .setTitle("접근성 권한 필요")
                    .setMessage("앱을 사용하려면 접근성 권한이 필요합니다. 설정 화면으로 이동하시겠습니까?")
                    .setPositiveButton("이동", (dialog, which) -> requestAccessibilityPermission(this))
                    .setNegativeButton("취소", null)
                    .show();
        }

//사용 기록을 요청합니다. -> 모든 어플리케이션의 정보를 json 파일로 변경 불가.
        RequestaccessTousage();








        // SharedPreferences를 사용하여 첫 실행 여부를 확인
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (settings.getBoolean(FIRST_RUN_KEY, true)) {
            // Install_Allapps_SeverSender 호출
            Log.d(TAG, "앱이 처음 실행됨, 모든 앱 정보를 서버로 보냅니다.");
            try {
                AppUsageFetcher appUsageFetcher = new AppUsageFetcher(this);
                appUsageFetcher.fetchAndSaveAppsByCategory(this);
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }


            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(FIRST_RUN_KEY, false);
            editor.apply();
        }
        else {
            Log.d(TAG, "앱을 처음 실행하지 않음,  Install_Allapps_SeverSender 작동하지 않음");
        }





        // 브로드 캐스트 등록
        Broadcastregister();





        ImageView usageLogButton = findViewById(R.id.UsageLoge);
        usageLogButton.setOnClickListener(v -> {
            if (hasUsageStatsPermission()) {
                Log.d(TAG, "사용 기록 권한이 이미 허용되어 있습니다. 앱 사용 기록을 정리합니다.");
                // 권한이 이미 있으면 바로 사용 기록을 처리
                try {
                    AppUsageFetcher appUsageFetcher = new AppUsageFetcher(this);
                    appUsageFetcher.fetchAndSaveAppsByCategory(this);
                } catch (PackageManager.NameNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Log.d(TAG, "사용 기록 권한을 요청합니다.");
                requestUsageAccessPermission(); // 권한이 없으면 요청
            }
        });


        //기존 메인 화면 작업 (수정 꼭 필요)
        ExistinMainScreenTasks();




    }

    private void requestAccessibilityPermission(MainActivity mainActivity) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }

    private boolean isAccessibilityServiceEnabled(Class<? extends AccessibilityService> service) {
        AccessibilityManager am = (AccessibilityManager) this.getSystemService(this.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo enabledService : enabledServices) {
            if (enabledService.getId().contains(this.getPackageName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    protected void onResume() {
        super.onResume();
        int clickCount1 = MyAccessibilityService.getClickCount(this, appInfoList2.get(1).getPackageName());

        // 클릭 횟수 초기화
        adapter2.updateTextAtPosition(0, String.valueOf(clickCount1));



    }

    private void Broadcastregister() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");
        registerReceiver(newAppInstallReceiver, intentFilter);
    }

    private void ExistinMainScreenTasks() {
        // 리사이클 뷰 세로 정렬
        RecyclerView recyclerView1 = findViewById(R.id.recyclerView_usage_time);
        recyclerView1.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        RecyclerView recyclerView2 = findViewById(R.id.recyclerView_click_count);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));


        // 1. PackageManager를 사용하여 설치된 앱 목록을 가져옴
        PackageManager packageManager = this.getPackageManager();
        List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);


        // 1-2. 사용자 사용 앱만 필터링
        List<ApplicationInfo> filteredApps = new ArrayList<>();
        for (ApplicationInfo appInfo : installedApps) {
            // 사용자 설치 앱 포함
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                filteredApps.add(appInfo);
            } else {
                // 시스템 앱 중 사용자가 자주 사용하는 앱만 포함 (예: 전화, 캘린더 등)
                String packageName = appInfo.packageName;

                // 사용자가 자주 사용하는 시스템 앱 목록
                if (packageName.equals("com.samsung.android.dialer") ||     // 전화 앱
                        packageName.equals("com.google.android.calendar") ||  // 구글 캘린더
                        packageName.equals("com.google.android.youtube") ||   // 유튜브
                        packageName.equals("com.android.contacts") ||   // 주소록
                        packageName.equals("com.android.chrome") ||     // 크롬
                        packageName.equals("com.kakao.talk") ||         // 카카오톡 등
                        packageName.equals("com.naver.line")) {         // 네이버 앱
                    filteredApps.add(appInfo);
                }
            }
        }

        // 2. 랜덤으로 하나의 앱을 선택
        Random random1 = new Random(); Random random2 = new Random(); Random random3 = new Random(); Random random4 = new Random();
        int randomIndex1 = random1.nextInt(filteredApps.size()); int randomIndex2 = random2.nextInt(filteredApps.size()); int randomIndex3 = random3.nextInt(filteredApps.size());int randomIndex4 = random4.nextInt(filteredApps.size());
        ApplicationInfo randomApp1 = filteredApps.get(randomIndex1);  ApplicationInfo randomApp2 = filteredApps.get(randomIndex2); ApplicationInfo randomApp3 = filteredApps.get(randomIndex3); ApplicationInfo randomApp4 = filteredApps.get(randomIndex4);

        // 3. 선택한 앱의 아이콘, Label, packagename 가져오기
        Drawable appIcon1 = packageManager.getApplicationIcon(randomApp1); Drawable appIcon2 = packageManager.getApplicationIcon(randomApp2); Drawable appIcon3 = packageManager.getApplicationIcon(randomApp3); Drawable appIcon4 = packageManager.getApplicationIcon(randomApp4);
        String string1 = packageManager.getApplicationLabel(randomApp1).toString(); String string2 = packageManager.getApplicationLabel(randomApp2).toString();String string3 = packageManager.getApplicationLabel(randomApp3).toString();String string4 = packageManager.getApplicationLabel(randomApp4).toString();
        String packageName1 = randomApp1.packageName;



        // 4. 선택한 앱의 클릭 횟수 및 사용 시간 가져오기
        int clickCount1 = MyAccessibilityService.getClickCount(this, packageName1);


        appInfoList1.add(new AppInfo(appIcon1, string1, "2시간", randomApp1.packageName));
        appInfoList1.add(new AppInfo(appIcon2, string2, "1시간 30분", randomApp2.packageName));
        appInfoList1.add(new AppInfo(appIcon3, string3, "45분", randomApp3.packageName));
        appInfoList1.add(new AppInfo(appIcon4, string4, "45분", randomApp4.packageName));


        appInfoList2.add(new AppInfo(appIcon1, string1, String.valueOf(clickCount1), randomApp1.packageName));
        appInfoList2.add(new AppInfo(appIcon2, string2, "54회", randomApp1.packageName));
        appInfoList2.add(new AppInfo(appIcon3, string3, "30회", randomApp1.packageName));
        appInfoList2.add(new AppInfo(appIcon4, string4, "30회", randomApp1.packageName));


        recyclerView1.setAdapter(adapter1);
        recyclerView2.setAdapter(adapter2);
    }


    public void RequestaccessTousage() {
        AppOpsManager appOps = (AppOpsManager) this.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                this.getPackageName());

        if (mode != AppOpsManager.MODE_ALLOWED) {
            // 사용 기록 접근 권한이 없으면, 사용자에게 권한 설정을 유도
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            this.startActivity(intent);
        }
    }

    // 권한이 있는지 확인하는 메서드
    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    // 사용 기록 권한 요청
    private void requestUsageAccessPermission() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        usageAccessLauncher.launch(intent); // 권한 요청 화면을 열기
    }

    @SuppressLint("DefaultLocale")
    public static String convertMillisToHMS(long millis) {
        // 1초 = 1000밀리초, 1분 = 60초, 1시간 = 60분
        long seconds = (millis / 1000) % 60; // 남은 초
        long minutes = (millis / (1000 * 60)) % 60; // 남은 분
        long hours = (millis / (1000 * 60 * 60)); // 남은 시

        // 형식에 맞춰 문자열 반환
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}