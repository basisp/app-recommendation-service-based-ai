package com.example.ai_customizing;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.Headers;

public class TokenManager {
    private static final String PREF_NAME = "AuthTokens";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String REFRESH_TOKEN_KEY = "refresh_token";

    private final SharedPreferences sharedPreferences;

    public TokenManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

//    public void saveTokens(Headers headers, List<Cookie> cookies) {
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//
//        // Save Access Token from header
//        String accessToken = headers.get("access");
//        if (accessToken != null) {
//            editor.putString(ACCESS_TOKEN_KEY, accessToken);
//        }
//
//        // Save Refresh Token from cookie
//        for (Cookie cookie : cookies) {
//            if ("refresh".equals(cookie.name())) {
//                editor.putString(REFRESH_TOKEN_KEY, cookie.value());
//                break;
//            }
//        }
//
//        editor.apply();
//    }

    // 액세스 토큰과 리프레시 토큰을 저장하는 메서드
    public void saveTokens(String accessToken, String refreshToken) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("access_token", accessToken);
        editor.putString("refresh_token", refreshToken);
        editor.apply();
    }

    public String getAccessToken() {
        return sharedPreferences.getString(ACCESS_TOKEN_KEY, null);
    }

    public String getRefreshToken() {
        return sharedPreferences.getString(REFRESH_TOKEN_KEY, null);
    }

    public void clearTokens() {
        sharedPreferences.edit().clear().apply();
    }

    public void saveAccessToken(String newAccessToken) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ACCESS_TOKEN_KEY, newAccessToken);
    }
}