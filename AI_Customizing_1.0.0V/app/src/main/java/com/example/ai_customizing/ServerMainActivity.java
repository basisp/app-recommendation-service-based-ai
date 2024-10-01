package com.example.ai_customizing;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerMainActivity extends AppCompatActivity {
    private EditText UserEditText, passEditText;
    private Button registerButton, loginButton, mainButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_main_activity);

        UserEditText = findViewById(R.id.UserEditText);
        passEditText = findViewById(R.id.passEditText);
        registerButton = findViewById(R.id.registerButton);
        loginButton = findViewById(R.id.Loginbutton);
        mainButton = findViewById(R.id.MainButton);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://3.39.227.199:8080/")
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
                            }

                            // JSON 데이터 출력
                            System.out.println("전송된 JSON: " + jsonObject.toString());
                            Toast.makeText(ServerMainActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();

                            System.out.println("accessToken: " + accessToken + "refreshToken: " + refreshToken);
                            Toast.makeText(ServerMainActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();
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


        // 버튼 클릭 리스너 설정
        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 // 로그인 상태를 true로 설정
                SharedPreferences preferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("isLoggedIn", true);
                editor.apply(); // 변경 사항 저장

                // MainActivity로 이동하는 Intent 생성
                Intent intent = new Intent(ServerMainActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ServerMainActivity.this, ServerRegister.class);
                startActivity(intent);
            }
        });
    }


    private List<Cookie> extractCookies(Response response) {
        List<Cookie> cookies = new ArrayList<>();
        List<String> cookieHeaders = response.headers("Set-Cookie"); // Set-Cookie 헤더를 가져옴
        HttpUrl url = response.request().url(); // 요청의 URL을 가져옴

        for (String cookieHeader : cookieHeaders) {
            // 쿠키 파싱
            Cookie cookie = Cookie.parse(url, cookieHeader);
            if (cookie != null) {
                cookies.add(cookie);
            }
        }
        return cookies;
    }
}
