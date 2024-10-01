package com.example.ai_customizing;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface Retrofit_interface {
    @POST("join")
    Call<Member> joinMember(@Body JsonObject memberJson);

    @POST("login")
    Call<Member> loginMember(@Body JsonObject memberJson);
}
