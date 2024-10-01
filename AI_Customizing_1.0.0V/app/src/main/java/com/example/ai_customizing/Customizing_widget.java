package com.example.ai_customizing;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.RemoteViews;
import android.content.Intent;
import android.app.PendingIntent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Customizing_widget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // 리모트 설
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.customizing_widget);

        // 1. PackageManager를 사용하여 설치된 앱 목록을 가져옴
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        // 1-2. 사용자 사용 앱만 필터링
        List<ApplicationInfo> filteredApps = new ArrayList<>();
        for (ApplicationInfo appInfo : installedApps) {
            // 사용자 설치 앱 포함
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                filteredApps.add(appInfo);
            } else {
                // 시스템 앱 중 사용자가 자주 사용하는 앱만 포함 (예: 전화, 캘린더 등)
                String packageName = appInfo.packageName;

                // 사용자가 자주 사용하는 시스템 앱 목록
                if (packageName.equals("com.samsung.android.dialer") ||     // 전화 앱
                        packageName.equals("com.google.android.calendar") ||  // 구글 캘린더
                        packageName.equals("com.google.android.youtube") ||   // 유튜브
                        packageName.equals("com.android.contacts") ||   // 주소록
                        packageName.equals("com.android.chrome") ||     // 크롬
                        packageName.equals("com.kakao.talk") ||         // 카카오톡 등
                        packageName.equals("com.naver.line")) {         // 네이버 앱
                    filteredApps.add(appInfo);
                }
            }
        }

        // 2. 랜덤으로 하나의 앱을 선택
        Random random1 = new Random(); Random random2 = new Random(); Random random3 = new Random(); Random random4 = new Random();
        int randomIndex1 = random1.nextInt(filteredApps.size()); int randomIndex2 = random2.nextInt(filteredApps.size()); int randomIndex3 = random3.nextInt(filteredApps.size());int randomIndex4 = random4.nextInt(filteredApps.size());
        ApplicationInfo randomApp1 = filteredApps.get(randomIndex1);  ApplicationInfo randomApp2 = filteredApps.get(randomIndex2); ApplicationInfo randomApp3 = filteredApps.get(randomIndex3); ApplicationInfo randomApp4 = filteredApps.get(randomIndex4);

        // 3. 선택한 앱의 아이콘 가져오기
        Drawable appIcon1 = packageManager.getApplicationIcon(randomApp1); Drawable appIcon2 = packageManager.getApplicationIcon(randomApp2); Drawable appIcon3 = packageManager.getApplicationIcon(randomApp3); Drawable appIcon4 = packageManager.getApplicationIcon(randomApp4);
        String string1 = packageManager.getApplicationLabel(randomApp1).toString(); String string2 = packageManager.getApplicationLabel(randomApp2).toString();String string3 = packageManager.getApplicationLabel(randomApp3).toString();String string4 = packageManager.getApplicationLabel(randomApp4).toString();
        Bitmap bitmap1 = drawableToBitmap(appIcon1);Bitmap bitmap2 = drawableToBitmap(appIcon2);Bitmap bitmap3 = drawableToBitmap(appIcon3);Bitmap bitmap4 = drawableToBitmap(appIcon4);
        String packageName1 = randomApp1.packageName;
        String packageName2 = randomApp2.packageName;
        String packageName3 = randomApp3.packageName;
        String packageName4 = randomApp4.packageName;

        //4. 앱 실행을 위한 인텐스 설정
        Intent launchIntent1 = packageManager.getLaunchIntentForPackage(packageName1);
        Intent launchIntent2 = packageManager.getLaunchIntentForPackage(packageName2);
        Intent launchIntent3 = packageManager.getLaunchIntentForPackage(packageName3);
        Intent launchIntent4 = packageManager.getLaunchIntentForPackage(packageName4);

        //5. 앱 실행 인텐트를 PendingIntent로 감싸기 or launchIntent1가 NULL이 아닌지 확인 필요하지만 생략함
        PendingIntent pendingIntent1 = PendingIntent.getActivity(context, 0, launchIntent1, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 0, launchIntent2, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent pendingIntent3 = PendingIntent.getActivity(context, 0, launchIntent3, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent pendingIntent4 = PendingIntent.getActivity(context, 0, launchIntent4, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        // ImageView에 아이콘 설정 or 이미지 뷰에 대한 클릭 이벤트 설정
        views.setImageViewBitmap(R.id.appwidget_imageView1, bitmap1);
        views.setImageViewBitmap(R.id.appwidget_imageView2, bitmap2);
        views.setImageViewBitmap(R.id.appwidget_imageView3, bitmap3);
        views.setImageViewBitmap(R.id.appwidget_imageView4, bitmap4);
        views.setTextViewText(R.id.appwidget_text1, string1);
        views.setTextViewText(R.id.appwidget_text2, string2);
        views.setTextViewText(R.id.appwidget_text3, string3);
        views.setTextViewText(R.id.appwidget_text4, string4);

        views.setOnClickPendingIntent(R.id.appwidget_imageView1, pendingIntent1);
        views.setOnClickPendingIntent(R.id.appwidget_imageView2, pendingIntent2);
        views.setOnClickPendingIntent(R.id.appwidget_imageView3, pendingIntent3);
        views.setOnClickPendingIntent(R.id.appwidget_imageView4, pendingIntent4);

        // 위젯 업데이트
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        // drawable이 BitmapDrawable인 경우, BitmapDrawable로 캐스팅하여 비트맵을 추출
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }


}