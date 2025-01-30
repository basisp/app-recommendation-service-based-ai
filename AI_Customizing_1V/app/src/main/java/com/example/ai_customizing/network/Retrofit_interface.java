package com.example.ai_customizing.network;

import com.example.ai_customizing.model.Member;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface Retrofit_interface {
    @POST("join")
    Call<Member> joinMember(@Body JsonObject memberJson);

    @POST("login")
    Call<Member> loginMember(@Body JsonObject memberJson);
}
