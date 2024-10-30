package com.example.ai_customizing.network;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.ai_customizing.AppUsageFetchWorker;
import com.example.ai_customizing.R;
import com.example.ai_customizing.model.Member;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerRegister extends AppCompatActivity {
    private EditText usernameEditText, passEditText;
    private Button registerButton;
    private static final int USAGE_ACCESS_PERMISSION_REQUEST_CODE = 1001;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        usernameEditText = findViewById(R.id.RusernameEditText);
        passEditText = findViewById(R.id.RpassEditText);
        registerButton = findViewById(R.id.RegesterButton1); // 회원가입 버튼

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://43.202.148.2:8080/")
                .addConverterFactory(GsonConverterFactory.create()) // JSON 변환기 추가
                .build();

        Retrofit_interface retrofit_interface = retrofit.create(Retrofit_interface.class);
        OkHttpClient client = new OkHttpClient();

        // 회원가입 버튼 클릭 리스너
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passEditText.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(ServerRegister.this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // JsonObject를 사용하여 JSON 생성
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("username", username);
                jsonObject.addProperty("password", password);

                Call<Member> call = retrofit_interface.joinMember(jsonObject);

                call.enqueue(new Callback<Member>() {
                    @Override
                    public void onResponse(Call<Member> call, Response<Member> response) {
                        if (response.code() == 500) {
                            Toast.makeText(ServerRegister.this, "이미 사용 중인 Email입니다. 다시 입력해 주세요.", Toast.LENGTH_SHORT).show();
                        } else if (!response.isSuccessful()) {
                            // 응답이 성공적이지 않을 때 errorBody를 사용하여 서버의 응답 본문을 출력
                            String errorMessage;
                            try {
                                errorMessage = response.errorBody().string(); // !수정: 서버의 error body를 문자열로 변환
                            } catch (IOException e) {
                                errorMessage = "알 수 없는 오류"; // 오류 발생 시 기본 메시지
                            }
                            System.out.println("전송된 JSON1 응답 이상함: " + jsonObject.toString()); // 터미널에 출력
                            assert response.body() != null;
                            Toast.makeText(ServerRegister.this, "회원가입 실패: " + response.code() + " 보낸 body : " + errorMessage, Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 201 || response.code() == 200) {
                            // 요청에 사용된 JSON 데이터를 출력
                            System.out.println("전송된 JSON2: " + jsonObject.toString()); // 터미널에 출력
                            Toast.makeText(ServerRegister.this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
                            Member member = new Member(ServerRegister.this);
                            member.saveUsername(username); // 유저 이름을 저장

                            //사용자 파일 정리
                            startBackgroundAppUsageFetcher();
                        } else {
                            Toast.makeText(ServerRegister.this, "회원가입 실패, 알 수 없는 오류: " + response.code(), Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onFailure(Call<Member> call, Throwable t) {
                        System.out.println("전송된 JSON3 응답에 실패함: " + jsonObject.toString()); // 터미널에 출력
                        Toast.makeText(ServerRegister.this, "회원가입 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

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
