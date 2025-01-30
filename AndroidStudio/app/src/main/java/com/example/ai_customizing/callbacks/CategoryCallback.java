package com.example.ai_customizing.callbacks;


import org.json.JSONException;

public interface CategoryCallback {
    void onCategoryFetched(String category) throws JSONException;
}