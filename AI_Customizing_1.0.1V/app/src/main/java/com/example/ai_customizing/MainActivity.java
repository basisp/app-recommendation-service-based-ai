package com.example.ai_customizing;


import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
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
import android.os.Process;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_customizing.adepter.AppInfoAdapter;
import com.example.ai_customizing.data.AppUsageFetcher;
import com.example.ai_customizing.model.AppInfo;
import com.example.ai_customizing.network.ServerMainActivity;
import com.example.ai_customizing.service.MyAccessibilityService;
import com.example.ai_customizing.service.NewAppInstallReceiver;
import com.example.ai_customizing.utils.Utils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<AppInfo> appInfoList1 = new ArrayList<>();
    List<AppInfo> appInfoList2 = new ArrayList<>();
    String TAG = "MainActivity";
    SharedPreferences sharedPreferences;
    AppInfoAdapter adapter1 = new AppInfoAdapter(appInfoList1);
    AppInfoAdapter adapter2 = new AppInfoAdapter(appInfoList2);

    private final NewAppInstallReceiver newAppInstallReceiver = new NewAppInstallReceiver();

    private final ActivityResultLauncher<Intent> usageAccessLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // 사용자가 권한 요청 화면을 닫은 후 결과를 확인합니다.
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // 결과가 OK인 경우 권한을 확인합니다.
                    if (hasUsageStatsPermission()) {
                        Log.d(TAG, "사용 기록 권한이 허용되었습니다.");
                    } else {
                        Log.e(TAG, "사용 기록 권한이 거부되었습니다.");
                    }
                } else {
                    Log.e(TAG, "사용 기록 권한 요청이 취소되었습니다.");
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

        //사용 기록 권한 요청합니다. -> 모든 어플리케이션의 정보를 json 파일로 변경 불가.
        RequestaccessTousage();



        // 브로드 캐스트 등록
        Broadcastregister();

        //내부 저장된 마지막 업데이트 시간을 받아오기 위한 선언
        sharedPreferences = this.getSharedPreferences("AppUsagePrefs", Context.MODE_PRIVATE);


        //기존 메인 화면 작업 (수정 꼭 필요)
        try {
            ExistinMainScreenTasks();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }


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
        int clickCount1 = MyAccessibilityService.getClickCount(this, appInfoList2.get(0).getPackageName());
        int clickCount2 = MyAccessibilityService.getClickCount(this, appInfoList2.get(1).getPackageName());
        int clickCount3 = MyAccessibilityService.getClickCount(this, appInfoList2.get(2).getPackageName());
        int clickCount4 = MyAccessibilityService.getClickCount(this, appInfoList2.get(3).getPackageName());

        // 클릭 횟수 초기화
        adapter2.updateTextAtPosition(0, String.valueOf(clickCount1)+"회");
        adapter2.updateTextAtPosition(1, String.valueOf(clickCount2)+"회");
        adapter2.updateTextAtPosition(2, String.valueOf(clickCount3)+"회");
        adapter2.updateTextAtPosition(3, String.valueOf(clickCount4)+"회");


        //모든 사용 시록 정리 함수\
        long lastUpdateTime = sharedPreferences.getLong("lastUpdateTime", 0);
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastUpdateTime >= 24 * 60 * 60 * 1000 || lastUpdateTime == 0){ //최종 업데이트로부터 24시간이 지나야 질문함 혹은 처음 실행했을 때.
            showUsageDialog();
        }
    }


    //새로운 앱 다운 시 신호 보내는 브로드캐스터 등록 메소드
    private void Broadcastregister() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");
        registerReceiver(newAppInstallReceiver, intentFilter);
    }


    //메인 화면 설정 메소드
    private void ExistinMainScreenTasks() throws JSONException, PackageManager.NameNotFoundException {
        // 리사이클 뷰 세로 정렬
        RecyclerView recyclerView1 = findViewById(R.id.recyclerView_usage_time);
        recyclerView1.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        RecyclerView recyclerView2 = findViewById(R.id.recyclerView_click_count);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        PackageManager packageManager = this.getPackageManager();


        //가장 많이 사용한 앱 패키지 이름 가져오기
        String packageName1 = Utils.getPackageName(this, 0);
        String packageName2 = Utils.getPackageName(this, 1);
        String packageName3 = Utils.getPackageName(this, 2);
        String packageName4 = Utils.getPackageName(this, 3);


        //사용시간 가져오기
        String AppUsage1 = Utils.getUsageTime(this, 0);
        String AppUsage2 = Utils.getUsageTime(this, 1);
        String AppUsage3 = Utils.getUsageTime(this, 2);
        String AppUsage4 = Utils.getUsageTime(this, 3);

        // 3. 선택한 앱의 아이콘, Label 가져오기.
        ApplicationInfo appInfo1 = packageManager.getApplicationInfo(packageName1, PackageManager.GET_META_DATA);
        ApplicationInfo appInfo2 = packageManager.getApplicationInfo(packageName2, PackageManager.GET_META_DATA);
        ApplicationInfo appInfo3 = packageManager.getApplicationInfo(packageName3, PackageManager.GET_META_DATA);
        ApplicationInfo appInfo4 = packageManager.getApplicationInfo(packageName4, PackageManager.GET_META_DATA);

        Drawable appIcon1 = packageManager.getApplicationIcon(packageName1);
        Drawable appIcon2 = packageManager.getApplicationIcon(packageName2);
        Drawable appIcon3 = packageManager.getApplicationIcon(packageName3);
        Drawable appIcon4 = packageManager.getApplicationIcon(packageName4);

        String Label1 = packageManager.getApplicationLabel(appInfo1).toString();
        String Label2 = packageManager.getApplicationLabel(appInfo2).toString();
        String Label3 = packageManager.getApplicationLabel(appInfo3).toString();
        String Label4 = packageManager.getApplicationLabel(appInfo4).toString();




        // 4. 선택한 앱의 클릭 횟수 및 사용 시간 가져오기
        int clickCount1 = MyAccessibilityService.getClickCount(this, packageName1);
        int clickCount2 = MyAccessibilityService.getClickCount(this, packageName2);
        int clickCount3 = MyAccessibilityService.getClickCount(this, packageName3);
        int clickCount4 = MyAccessibilityService.getClickCount(this, packageName4);


        appInfoList1.add(new AppInfo(appIcon1, Label1, AppUsage1, packageName1));
        appInfoList1.add(new AppInfo(appIcon2, Label2, AppUsage2, packageName2));
        appInfoList1.add(new AppInfo(appIcon3, Label3, AppUsage3, packageName3));
        appInfoList1.add(new AppInfo(appIcon4, Label4, AppUsage4, packageName4));


        appInfoList2.add(new AppInfo(appIcon1, Label1, String.valueOf(clickCount1)+"회", packageName1));
        appInfoList2.add(new AppInfo(appIcon2, Label2, String.valueOf(clickCount2)+"회", packageName2));
        appInfoList2.add(new AppInfo(appIcon3, Label3, String.valueOf(clickCount3)+"회", packageName3));
        appInfoList2.add(new AppInfo(appIcon4, Label4, String.valueOf(clickCount4)+"회", packageName4));


        recyclerView1.setAdapter(adapter1);
        recyclerView2.setAdapter(adapter2);
    }


    public void RequestaccessTousage() {
        AppOpsManager appOps = (AppOpsManager) this.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
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
                Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    // 사용 기록 권한 요청
    private void requestUsageAccessPermission() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        usageAccessLauncher.launch(intent); // 권한 요청 화면을 열기
    }

    //모든 사용 기록 정리 함수
    private void showUsageDialog() {
        new AlertDialog.Builder(this)
                .setTitle("사용 기록")
                .setMessage("앱 사용 기록을 정리하시겠습니까?")
                .setPositiveButton("확인", (dialog, which) -> {
                    if (hasUsageStatsPermission()) {
                        try {
                            AppUsageFetcher appUsageFetcher = new AppUsageFetcher(this);
                            appUsageFetcher.fetchAndSaveAppsByCategory(this);
                        } catch (PackageManager.NameNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        requestUsageAccessPermission(); // 권한 요청
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }



}