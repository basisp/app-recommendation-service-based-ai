package com.example.ai_customizing.network;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ai_customizing.R;
import com.example.ai_customizing.model.Member;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerRegister extends AppCompatActivity {
    private EditText usernameEditText, passEditText;
    private Button registerButton;

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
                            System.out.println("전송된 JSON1 응답 이상함: " + jsonObject.toString()); // 터미널에 출력
                            Toast.makeText(ServerRegister.this, "회원가입 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            // 요청에 사용된 JSON 데이터를 출력
                            System.out.println("전송된 JSON2: " + jsonObject.toString()); // 터미널에 출력
                            Toast.makeText(ServerRegister.this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
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
}
