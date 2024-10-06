package com.example.ai_customizing;


import org.json.JSONException;

public interface CategoryCallback {
    void onCategoryFetched(String category) throws JSONException;
}