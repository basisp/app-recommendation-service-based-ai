package com.example.ai_customizing.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.RemoteViews;
import android.content.Intent;
import android.app.PendingIntent;

import com.example.ai_customizing.R;
import com.example.ai_customizing.utils.StaticMethodClass;
import com.example.ai_customizing.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Customizing_widget extends AppWidgetProvider {
    public static int categoryNumber;
    static String TAG = "Customizing_widget";
    public static String packageName;

    static void FirstUpdateAppWidget(Context context, AppWidgetManager appWidgetManager,
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
                if (StaticMethodClass.isExceptionSystemApp(packageName)) {
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


    public static void updateAppWidgetClick(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.customizing_widget);
        SharedPreferences preferences = context.getSharedPreferences("AppData", Context.MODE_PRIVATE);



        // 패키지 이름을 가져오기 (우선순위에 따라 1, 2, 3, 4번 앱)
        String packageName1 = preferences.getString("package_name0", null);
        String packageName2 = preferences.getString("package_name1", null);
        String packageName3 = preferences.getString("package_name2", null);
        String packageName4 = preferences.getString("package_name3", null);
        Log.d(TAG, "package_name0 : " + packageName1);
        Log.d(TAG, "package_name1 : " + packageName2);
        Log.d(TAG, "package_name2 : " + packageName3);
        Log.d(TAG, "package_name3 : " + packageName4);
        // 카테고리를 가져오기
        String category1 = preferences.getString("category0", null);
        String category2 = preferences.getString("category1", null);
        String category3 = preferences.getString("category2", null);
        String category4 = preferences.getString("category3", null);
        Log.d(TAG, "category1 : " + category1);
        Log.d(TAG, "category2 : " + category2);
        Log.d(TAG, "category3 : " + category3);
        Log.d(TAG, "category4 : " + category4);
        // 앱 아이콘 및 텍스트 설정 및 클릭 이벤트 설정

        updateWidgetViews(context, views, packageName1, category1, R.id.appwidget_imageView1, R.id.appwidget_text1);

        updateWidgetViews(context, views, packageName2, category2, R.id.appwidget_imageView2, R.id.appwidget_text2);

        updateWidgetViews(context, views, packageName3, category3, R.id.appwidget_imageView3, R.id.appwidget_text3);

        updateWidgetViews(context, views, packageName4, category4, R.id.appwidget_imageView4, R.id.appwidget_text4);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    static Intent getLaunchIntent(PackageManager packageManager, String packageName, String category, Context context) {
        Intent launchIntent = packageManager.getLaunchIntentForPackage(packageName);

        if (category == null){
            launchIntent = Utils.getMostUsedAppIntent(context, "All");
            Log.d(TAG,"카테고리 정보도 없음 All_app.json에서 가장 많이 사용한 앱을 가져옴");
        }
        else if(launchIntent == null) {
            // 해당 패키지가 없을 경우 카테고리에서 가장 많이 사용된 앱으로 대체
            launchIntent = Utils.getMostUsedAppIntent(context, category);
            Log.d(TAG,"해당하는 앱의 정보를 확인할 수 없음" + category+ "_app.json에서 가장 많이 사용한 앱을 가져옴");
        }
        categoryNumber++;
        return launchIntent;
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences preferences = context.getSharedPreferences("MyWidgetPrefs", Context.MODE_PRIVATE);
        boolean isFirstRun = preferences.getBoolean("isFirstRun", true);

        if (isFirstRun) {
            // 첫 실행일 때만 FirstUpdateAppWidget 호출, 랜덤으로 위젯에 앱을 보여줌
            for (int appWidgetId : appWidgetIds) {
                FirstUpdateAppWidget(context, appWidgetManager, appWidgetId);
            }

            // 첫 실행 플래그를 false로 설정
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isFirstRun", false);
            editor.apply();
        }


        // There may be multiple widgets active, so update all of them
//        for (int appWidgetId : appWidgetIds) {
//            Log.d(TAG,"위젯을 업데이트 합니다.");
//            categoryNumber = 0;
//            updateAppWidget(context, appWidgetManager, appWidgetId);
//        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        // 브로드캐스트를 통해 전달된 메시지 처리
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
            // 업데이트 요청이 왔을 때 처리
            int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            if (appWidgetIds != null && appWidgetIds.length > 0) {
                this.onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds);
            }
        }
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

    //함수의 아이콘과 이름을 설정
    private static void updateWidgetViews(Context context, RemoteViews views, String packageName, String category, int imageViewId, int textViewId) {
        PackageManager packageManager = context.getPackageManager();
        Customizing_widget.packageName = null;
        try {
            // 앱 실행 인텐트 설정
            Intent launchIntent = getLaunchIntent(packageManager, packageName, category, context);
            if (launchIntent != null) {
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                views.setOnClickPendingIntent(imageViewId, pendingIntent);
            }


            // 앱의 아이콘과 이름을 가져옴
            if (Customizing_widget.packageName == null)
                Customizing_widget.packageName = packageName;

            ApplicationInfo appInfo = packageManager.getApplicationInfo(Customizing_widget.packageName, PackageManager.GET_META_DATA);
            Drawable appIcon = packageManager.getApplicationIcon(appInfo);
            String appName = packageManager.getApplicationLabel(appInfo).toString();

            // 아이콘을 비트맵으로 변환하여 설정
            Bitmap bitmap = drawableToBitmap(appIcon);
            views.setImageViewBitmap(imageViewId, bitmap);

            // 앱 이름 설정
            views.setTextViewText(textViewId, appName);



        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


}