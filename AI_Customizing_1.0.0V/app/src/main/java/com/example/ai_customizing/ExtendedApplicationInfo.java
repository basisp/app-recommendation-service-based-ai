package com.example.ai_customizing;

import android.content.pm.ApplicationInfo;

public class ExtendedApplicationInfo extends ApplicationInfo {
    public static final int CATEGORY_ART_AND_DESIGN = 100;
    public static final int CATEGORY_AUTO_AND_VEHICLES = 101;
    public static final int CATEGORY_BEAUTY = 102;
    public static final int CATEGORY_BOOKS_AND_REFERENCE = 103;
    public static final int CATEGORY_BUSINESS = 104;
    public static final int CATEGORY_COMICS = 105;
    public static final int CATEGORY_COMMUNICATION = 106;
    public static final int CATEGORY_DATING = 107;
    public static final int CATEGORY_EDUCATION = 108;
    public static final int CATEGORY_ENTERTAINMENT = 109;
    public static final int CATEGORY_EVENTS = 110;
    public static final int CATEGORY_FINANCE = 111;
    public static final int CATEGORY_FOOD_AND_DRINK = 112;
    public static final int CATEGORY_HEALTH_AND_FITNESS = 113;
    public static final int CATEGORY_HOUSE_AND_HOME = 114;
    public static final int CATEGORY_LIBRARIES_AND_DEMO = 115;
    public static final int CATEGORY_LIFESTYLE = 116;
    public static final int CATEGORY_MAPS_NAVIGATION = 117;
    public static final int CATEGORY_MEDICAL = 118;
    public static final int CATEGORY_MUSIC_AUDIO = 119;
    public static final int CATEGORY_NEWS_MAGAZINES = 120;
    public static final int CATEGORY_PARENTING = 121;
    public static final int CATEGORY_PERSONALIZATION = 122;
    public static final int CATEGORY_PHOTOGRAPHY = 123;
    public static final int CATEGORY_PRODUCTIVITY = 124;
    public static final int CATEGORY_SHOPPING = 125;
    public static final int CATEGORY_SOCIAL = 126;
    public static final int CATEGORY_SPORTS = 127;
    public static final int CATEGORY_TOOLS = 128;
    public static final int CATEGORY_TRAVEL_LOCAL = 129;
    public static final int CATEGORY_VIDEO_PLAYERS = 130;
    public static final int CATEGORY_WEATHER = 131;
    public static final int CATEGORY_NOTFOUND = 132;
    public static final int CATEGORY_GAME = 133;

    public String packageName;
    public String categoryName;
    public long usageTime; // 사용 시간 추가


    // 기본 생성자
    public ExtendedApplicationInfo() {}

    // 생성자
    public ExtendedApplicationInfo(String packageName, String categoryName, long usageTime) {
        this.packageName = packageName;
        this.categoryName = categoryName;
        this.usageTime = usageTime;
    }

    public static int mapCategory(String category) {
        switch (category) {
            case "ART_AND_DESIGN": // 아트/디자인
                return CATEGORY_ART_AND_DESIGN;
            case "AUTO_AND_VEHICLES": // 자동자/교통수단
                return CATEGORY_AUTO_AND_VEHICLES;
            case "BEAUTY": // 뷰티
                return CATEGORY_BEAUTY;
            case "BOOKS_AND_REFERENCE": // 도서/참고자료
                return CATEGORY_BOOKS_AND_REFERENCE;
            case "BUSINESS": // 비지니스
                return CATEGORY_BUSINESS;
            case "COMICS":
                return CATEGORY_COMICS;
            case "COMMUNICATION":
                return CATEGORY_COMMUNICATION;
            case "DATING":
                return CATEGORY_DATING;
            case "EDUCATION":
                return CATEGORY_EDUCATION;
            case "ENTERTAINMENT":
                return CATEGORY_ENTERTAINMENT;
            case "EVENTS":
                return CATEGORY_EVENTS;
            case "FINANCE":
                return CATEGORY_FINANCE;
            case "FOOD_AND_DRINK":
                return CATEGORY_FOOD_AND_DRINK;
            case "HEALTH_AND_FITNESS":
                return CATEGORY_HEALTH_AND_FITNESS;
            case "HOUSE_AND_HOME":
                return CATEGORY_HOUSE_AND_HOME;
            case "LIBRARIES_AND_DEMO":
                return CATEGORY_LIBRARIES_AND_DEMO;
            case "LIFESTYLE":
                return CATEGORY_LIFESTYLE;
            case "MAPS_AND_NAVIGATION":
                return CATEGORY_MAPS_NAVIGATION;
            case "MEDICAL":
                return CATEGORY_MEDICAL;
            case "MUSIC_AND_AUDIO":
                return CATEGORY_MUSIC_AUDIO;
            case "NEWS_AND_MAGAZINES":
                return CATEGORY_NEWS_MAGAZINES;
            case "PARENTING":
                return CATEGORY_PARENTING;
            case "PERSONALIZATION":
                return CATEGORY_PERSONALIZATION;
            case "PHOTOGRAPHY":
                return CATEGORY_PHOTOGRAPHY;
            case "PRODUCTIVITY":
                return CATEGORY_PRODUCTIVITY;
            case "SHOPPING":
                return CATEGORY_SHOPPING;
            case "SOCIAL":
                return CATEGORY_SOCIAL;
            case "SPORTS":
                return CATEGORY_SPORTS;
            case "TOOLS":
                return CATEGORY_TOOLS;
            case "TRAVEL_AND_LOCAL":
                return CATEGORY_TRAVEL_LOCAL;
            case "VIDEO_PLAYERS":
                return CATEGORY_VIDEO_PLAYERS;
            case "WEATHER":
                return CATEGORY_WEATHER;
            case "Not Found":
                return CATEGORY_NOTFOUND;
            case "GAME":
                return CATEGORY_GAME;
            default:
                return CATEGORY_UNDEFINED; // 정의되지 않은 카테고리
        }
    }

    public String getCategoryName() {
        return categoryName;
    }

    public long getUsageTime() {
        return usageTime;
    }

    // getPackageName 메서드 추가
    public String getPackageName() {
        return packageName;
    }
}
