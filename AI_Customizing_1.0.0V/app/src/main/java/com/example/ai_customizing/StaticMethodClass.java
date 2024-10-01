package com.example.ai_customizing;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class StaticMethodClass {
    private static final String PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=";
    private static final String TAG = "StaticMethodClass";

    public static void getCategoryFromPlayStore(String packageName, CategoryCallback callback) {
        new FetchCategoryTask(packageName, callback).execute();
    }

    private static class FetchCategoryTask extends AsyncTask<Void, Void, String> {
        private final String packageName;
        private final CategoryCallback callback;

        FetchCategoryTask(String packageName, CategoryCallback callback) {
            this.packageName = packageName;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                Document doc = Jsoup.connect(PLAY_STORE_URL + packageName)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .get();

                String scriptTag = doc.select("script[type=application/ld+json]").first().data();
                JSONObject jsonData = new JSONObject(scriptTag);
                String category = jsonData.optString("applicationCategory", "Unknown");

                // "GAME"이 포함된 경우 "GAME"으로 저장
                if (category.toUpperCase().contains("GAME")) {
                    Log.d(TAG, "게임의 경우 전부 \"GAME\"카테고리로 통일한다. : " + category);
                    return "GAME";
                }
                return category; // 다른 카테고리 반환
            } catch (IOException | JSONException e) {
                Log.d(TAG, "에러 다음 앱의 카테고리를 찾지 못함 : " + packageName); // 필요한 경우 Log.e 이용
            }
            return "Not Found"; // 오류 발생 시 NOT FOUND 반환
        }

        @Override
        protected void onPostExecute(String category) {
            // 비동기 작업이 완료된 후 콜백을 통해 결과 전달
            callback.onCategoryFetched(category);
        }
    }

    public static boolean isExceptionSystemApp(String packageName) {
        // 예외 시스템 앱 필터링 예시 (여기에 원하는 패키지 이름을 추가)
        return packageName.equals("com.google.android.youtube")
                || packageName.equals("com.android.phone")
                || packageName.equals("com.android.contacts")
                || packageName.equals("com.skt.skaf.A000Z00040")
                || packageName.equals("com.skt.tdatacoupon")
                || packageName.equals("com.tms")
                || packageName.equals("com.sktelecom.tguard")
                || packageName.equals("com.skplanet.tmaptaxi.android.passenger")
                || packageName.equals("com.android.vending")
                || packageName.equals("com.sktelecom.tsmartpay")
                || packageName.equals("com.samsung.android.app.contacts")
                || packageName.equals("com.sec.android.app.samsungapps")
                || packageName.equals("com.sec.android.app.myfiles")
                || packageName.equals("com.samsung.android.app.reminder")
                || packageName.equals("com.samsung.android.arzone")
                || packageName.equals("com.skt.prod.dialer")
                || packageName.equals("com.samsung.android.dialer")
                || packageName.equals("com.samsung.android.messaging")
                || packageName.equals("com.samsung.android.calendar")
                || packageName.equals("com.sec.android.app.camera")
                || packageName.equals("com.android.settings") // 설정 앱
                || packageName.equals("com.sec.android.app.fm")
                || packageName.equals("com.samsung.android.visionintelligence빅")
                || packageName.equals("com.samsung.android.app.dtv.dmb")
                || packageName.equals("com.sec.android.app.clockpackage")
                || packageName.equals("com.samsung.android.game.gamehome")
                || packageName.equals("com.sec.android.gallery3de")
                || packageName.equals("com.google.android.googlequicksearchbox")
                || packageName.equals("com.android.chrome")
                || packageName.equals("com.google.android.gm")
                || packageName.equals("com.google.android.apps.maps")
                || packageName.equals("com.google.android.apps.tachyon")
                || packageName.equals("com.samsung.android.app.notes")
                || packageName.equals("com.samsung.android.app.spage")
                || packageName.equals("com.sktelecom.minit")
                || packageName.equals("com.samsung.android.samsungpass");
    }

    // 구글 플레이 스토어에서 해당 앱의 카테고리 정보를 가져옴
    public static String getCategoryFromPlayStore(String packageName) {
        try {
            Document doc = Jsoup.connect(PLAY_STORE_URL + packageName)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .get();

            Element scriptTag = doc.select("script[type=application/ld+json]").first();
            if (scriptTag != null) {
                JSONObject jsonData = new JSONObject(scriptTag.data());
                String category = jsonData.optString("applicationCategory", "Unknown"); // 카테고리 정보

                // "GAME"이 포함된 경우 "GAME"으로 저장
                if (category.toUpperCase().contains("GAME")) {
                    Log.d(TAG,"게임의 경우 전부 \"GAME\"카테고리로 통일한다. : " + category);
                    return "GAME";
                }
                return category; // 다른 카테고리 반환
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "에러 다음 앱의 카테고리를 찾지 못함 : " + packageName, e);
        }
        return "Not Found"; // 오류 발생 시 NOTFOUND 반환
    }
}
