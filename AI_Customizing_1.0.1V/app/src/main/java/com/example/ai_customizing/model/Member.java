package com.example.ai_customizing.model;

import android.content.Context;
import android.content.SharedPreferences;


public class Member {
    private String username;
    private String password;
    private SharedPreferences sharedPreferences = null;

    public Member(Context context) {
        this.sharedPreferences = context.getSharedPreferences("Member", Context.MODE_PRIVATE);
    }

    public String getUsername() { return sharedPreferences.getString("username", null); }
    public String getPassword() {
        return password;
    }


    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void saveUsername(String username) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.apply();
    }
}

