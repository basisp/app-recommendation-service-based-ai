package com.example.ai_customizing.model;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private final Drawable iconResId;
    private final String name;
    private String info;
    private final String packageName;

    public AppInfo(Drawable iconResId, String name, String info, String packageName) {
        this.iconResId = iconResId;
        this.name = name;
        this.info = info;
        this.packageName = packageName;
    }

    public Drawable getIconResId() {
        return iconResId;
    }

    public String getName() {
        return name;
    }

    public String getInfo() {
        return info;
    }

    public String getPackageName() { return packageName; }

    public void setInfo(String info) {
        this.info = info;
    }
}
