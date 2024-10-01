package com.example.ai_customizing;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.*;
import java.io.IOException;

public class SendServer {
    private static final String SERVER_URL = "http://3.39.227.199:8080/exchange"; // 파일 전송을 위한 서버 URL
    private static final String REISSUE_URL = "http://3.39.227.199:8080/reissue"; // 리프레시 토큰을 통한 액세스 토큰 재발급 URL

    private final OkHttpClient client;
    private final TokenManager tokenManager;

    private final String TAG = "SendServer";

    public SendServer(CategoryCallback context) {
        this.client = new OkHttpClient();
        this.tokenManager = new TokenManager((Context) context);
    }

    public void sendJsonArrayToServer(JSONArray jsonArray, Callback callback) {
        String accessToken = tokenManager.getAccessToken();
        if (accessToken == null) {
            callback.onFailure(null, new IOException("엑세스 토큰 없음(NULL)"));
            return;
        }
        Log.d(TAG, "accessToken : " + accessToken);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonArray.toString(), JSON); // JSONArray를 문자열로 변환

        Request request = new Request.Builder()
                .url(SERVER_URL)
                .addHeader("access", accessToken) // 헤더에 액세스 토큰 포함
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { // 전달 실패
                Log.e(TAG, "보내는데 오류남 : ", e);
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException { // 서버 응답 성공
                if (response.isSuccessful()) {

                    // 정상 응답 처리
                    Log.d(TAG, "정상적으로 전달됨");
                    callback.onResponse(call, response); // Response 객체를 전달
                } else if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    // 액세스 토큰 만료 처리
                    String refreshToken = tokenManager.getRefreshToken();
                    if (refreshToken != null) {
                        Log.d(TAG, "토큰 만료, 리프레시 토큰을 통해 재발급 요청.");
                        reissueAccessToken(refreshToken, jsonArray, callback); // JSONArray를 전달
                    } else {
                        callback.onFailure(call, new IOException("리프레시 토큰이 없음"));
                    }
                } else {
                    // 다른 오류 처리
                    Log.d(TAG, "응답은 받았는데 알 수 없는 오류 CallBack을 통한 오류 코드 전달");
                    callback.onFailure(call, new IOException("오류 코드: " + response.code()));
                }
            }
        });
    }

    // 리프레시 토큰을 사용하여 액세스 토큰을 재발급받는 메서드
    private void reissueAccessToken(String refreshToken, JSONArray jsonArray, Callback callback) {
        try {

            // OkHttp 클라이언트 초기화
            OkHttpClient client = new OkHttpClient();

            if (refreshToken == null) {
                Log.e(TAG, "리프레시 토큰이 null입니다.");
                return; // 또는 적절한 예외 처리
            }

            // 요청 바디 생성 (빈 JSON 객체)
            JSONObject emptyJsonObject = new JSONObject(); // 빈 JSON 객체 생성
            RequestBody body = RequestBody.create(
                    emptyJsonObject.toString(), // 빈 JSON 객체를 문자열로 변환
                    MediaType.parse("application/json; charset=utf-8")
            );

            // 요청 빌더 생성
            Request request = new Request.Builder()
                    .url(REISSUE_URL)
                    .addHeader("Cookie", "refresh=" + refreshToken) // Set-Cookie 대신 Cookie 헤더에 리프레시 토큰 추가
                    .post(body)
                    .build();

            // 요청 실행
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "리프레시 토큰 요청 실패: " + e.getMessage());
                    callback.onFailure(call, e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        // 응답에서 헤더 확인 및 토큰 추출
                        String accessToken = response.header("access");
                        String setCookieHeader = response.header("Set-Cookie");
                        String refreshTokenFromCookie = extractRefreshToken(setCookieHeader);

                        if (accessToken != null && refreshTokenFromCookie != null) {
                            // 두 토큰 모두 TokenManager에 저장
                            tokenManager.saveTokens(accessToken, refreshTokenFromCookie);
                            Log.d(TAG, "토큰이 성공적으로 초기화 됨 ");
                            Log.d(TAG, "액세스 토큰: " + accessToken);
                            Log.d(TAG, "리프레시 토큰: " + refreshTokenFromCookie);

                            // 새로운 액세스 토큰으로 서버에 JSON 배열 재전송
                            sendJsonArrayToServer(jsonArray, callback); // JSONArray를 전달
                        } else {
                            Log.e(TAG, "서버 응답에 토큰 값이 없습니다.");
                            callback.onFailure(call, new IOException("서버 응답에 토큰 값이 없습니다."));
                        }
                    } else {
                        // 응답 바디와 헤더 정보를 출력
                        String responseBody = response.body() != null ? response.body().string() : "응답 바디 없음";
                        String responseHeaders = response.headers().toString();

                        Log.e(TAG, "리프레시 토큰 재발급 실패. 서버 응답 코드: " + response.code());
                        Log.e(TAG, "응답 바디: " + responseBody);
                        Log.e(TAG, "응답 헤더: " + responseHeaders);

                        callback.onFailure(call, new IOException("리프레시 토큰 재발급 실패. 서버 응답 코드: " + response.code() + ", 바디: " + responseBody));
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "리프레시 토큰 재발급 중 오류 발생: " + e.getMessage());
            callback.onFailure(null, (IOException) e);
        }
    }


    // Set-Cookie 헤더에서 리프레시 토큰을 추출하는 메서드
    private String extractRefreshToken(String setCookieHeader) {
        if (setCookieHeader != null) {
            String[] cookies = setCookieHeader.split("; ");
            for (String cookie : cookies) {
                if (cookie.startsWith("refresh=")) {
                    return cookie.substring("refresh=".length());
                }
            }
        }
        return null;
    }

}
