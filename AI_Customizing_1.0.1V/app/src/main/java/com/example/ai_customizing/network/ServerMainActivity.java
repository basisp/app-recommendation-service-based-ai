package com.example.ai_customizing.network;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.ai_customizing.AppUsageFetchWorker;
import com.example.ai_customizing.MainActivity;
import com.example.ai_customizing.R;
import com.example.ai_customizing.model.Member;
import com.google.gson.JsonObject;

import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerMainActivity extends AppCompatActivity {
    private EditText UserEditText, passEditText;
    private Button registerButton, loginButton, mainButton;
    private static final int USAGE_ACCESS_PERMISSION_REQUEST_CODE = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_main_activity);

        // 사용자 기록 엑세스 권한 요청 로직 실행
        if (!isUsageAccessGranted()) {
            requestUsageAccessPermission(); // 권한 요청
        }


        UserEditText = findViewById(R.id.UserEditText);
        passEditText = findViewById(R.id.passEditText);
        registerButton = findViewById(R.id.registerButton);
        loginButton = findViewById(R.id.Loginbutton);
//        mainButton = findViewById(R.id.MainButton);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://43.202.148.2:8080/")
                .addConverterFactory(GsonConverterFactory.create()) // JSON 변환기 추가
                .build();

        Retrofit_interface retrofit_interface = retrofit.create(Retrofit_interface.class);

        // 로그인 버튼 클릭 리스너
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = UserEditText.getText().toString().trim();
                String password = passEditText.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(ServerMainActivity.this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // JsonObject를 사용하여 JSON 생성
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("username", username);
                jsonObject.addProperty("password", password);

                Call<Member> call = retrofit_interface.loginMember(jsonObject); // 메소드 이름 변경

                call.enqueue(new Callback<Member>() {
                    @Override
                    public void onResponse(Call<Member> call, retrofit2.Response<Member> response) {
                        if (response.code() == 401) {
                            // 401 Unauthorized: 잘못된 사용자 이름 또는 비밀번호
                            Toast.makeText(ServerMainActivity.this, "로그인 실패: 잘못된 사용자 이름 또는 비밀번호", Toast.LENGTH_SHORT).show();
                        } else if (!response.isSuccessful()) {
                            // 다른 비정상적인 응답 처리
                            System.out.println("전송된 JSON 응답 이상함: " + jsonObject.toString());
                            Toast.makeText(ServerMainActivity.this, "로그인 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 200) {
                            // 로그인 성공: 토큰 저장 로직
                            Headers headers = response.headers();

                            // 1. Access Token 가져오기 (헤더에서 access 키를 통해 가져옴)
                            String accessToken = headers.get("access");
                            if (accessToken != null) {
                                System.out.println("Access Token: " + accessToken);
                            }

                            // 2. Refresh Token 가져오기 (쿠키에서 "refresh" 값 가져오기)
                            List<Cookie> cookies = Cookie.parseAll(response.raw().request().url(), headers);
                            String refreshToken = null;
                            for (Cookie cookie : cookies) {
                                if ("refresh".equals(cookie.name())) {
                                    refreshToken = cookie.value();
                                    System.out.println("Refresh Token: " + refreshToken);
                                    break;
                                }
                            }

                            // 3. TokenManager를 이용해 토큰을 저장
                            if (accessToken != null && refreshToken != null) {
                                TokenManager tokenManager = new TokenManager(ServerMainActivity.this);
                                tokenManager.saveTokens(accessToken, refreshToken);  // 액세스 토큰과 리프레시 토큰 저장
                                Member member = new Member(ServerMainActivity.this);
                                member.saveUsername(username); // 유저 이름을 저장
                            }

                            // 4. member 이용해 사용자 이름 저장.
                            Member member = new Member(ServerMainActivity.this);
                            member.saveUsername(username);

                            // JSON 데이터 출력
                            System.out.println("전송된 JSON: " + jsonObject.toString());
                            System.out.println("accessToken: " + accessToken + "refreshToken: " + refreshToken);
                            Toast.makeText(ServerMainActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();

                            // 로그인 상태를 true로 설정
                            SharedPreferences preferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.apply(); // 변경 사항 저장

                            //사용자 파일 정리
                            //startBackgroundAppUsageFetcher();

                            // MainActivity로 이동하는 Intent 생성
                            Intent intent = new Intent(ServerMainActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<Member> call, Throwable t) {
                        // 예외 메시지를 통해 어떤 종류의 오류인지 파악
                        if (t instanceof IOException) {
                            System.out.println("네트워크 연결 오류: " + t.getMessage());
                        } else if (t instanceof HttpException) {
                            System.out.println("HTTP 오류: " + t.getMessage());
                        } else {
                            System.out.println("알 수 없는 오류: " + t.getMessage());
                        }

                        // 실패 로그 출력
                        System.out.println("전송된 JSON 응답에 실패함: " + jsonObject.toString() + t.getMessage());
                        Toast.makeText(ServerMainActivity.this, "로그인 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


//        // 버튼 클릭 리스너 설정
//        mainButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                 // 로그인 상태를 true로 설정
//                SharedPreferences preferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
//                SharedPreferences.Editor editor = preferences.edit();
//                editor.putBoolean("isLoggedIn", true);
//                editor.apply(); // 변경 사항 저장
//
//                // MainActivity로 이동하는 Intent 생성
//                Intent intent = new Intent(ServerMainActivity.this, MainActivity.class);
//                startActivity(intent);
//            }
//        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ServerMainActivity.this, ServerRegister.class);
                startActivity(intent);
            }
        });
    }


    private void requestUsageAccessPermission() {
        // !수정: 사용자 기록 엑세스 권한을 요청하는 메서드
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivityForResult(intent, USAGE_ACCESS_PERMISSION_REQUEST_CODE);
    }

    private boolean isUsageAccessGranted() {
        // !수정: 사용 기록 접근 권한이 허용되었는지 확인하는 메서드
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 권한이 부여되지 않았을 때 처리
        if (requestCode == USAGE_ACCESS_PERMISSION_REQUEST_CODE) {
            if (isUsageAccessGranted()) {
                // 권한이 승인되었으면 권한 승인 완료 로그 출력
                Toast.makeText(this, "사용자 기록 엑세스 권한이 승인되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                // 권한이 승인되지 않으면 Toast 메시지를 띄우고 다시 권한 요청
                Toast.makeText(this, "사용자 기록 엑세스 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                requestUsageAccessPermission(); // 다시 권한 요청
            }
        }
    }


    private void startBackgroundAppUsageFetcher() {
        WorkManager workManager = WorkManager.getInstance(this);

        // WorkRequest 생성
        OneTimeWorkRequest fetchAppUsageWork = new OneTimeWorkRequest.Builder(AppUsageFetchWorker.class)
                .build();

        // WorkManager 실행
        workManager.enqueue(fetchAppUsageWork);
    }
}
