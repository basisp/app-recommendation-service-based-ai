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
import com.example.ai_customizing.utils.Utils;


public class Customizing_widget extends AppWidgetProvider {
    public static int categoryNumber;
    static String TAG = "Customizing_widget";
    public static String packageName;

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.customizing_widget);
        SharedPreferences preferences = context.getSharedPreferences("AppData", Context.MODE_PRIVATE);


        // 패키지 이름을 가져오기 (우선순위에 따라 1, 2, 3, 4번 앱)
        String packageName1 = preferences.getString("packageName0", null);
        String packageName2 = preferences.getString("packageName1", null);
        String packageName3 = preferences.getString("packageName2", null);
        String packageName4 = preferences.getString("packageName3", null);

        // 카테고리를 가져오기
        String category1 = preferences.getString("category0", null);
        String category2 = preferences.getString("category1", null);
        String category3 = preferences.getString("category2", null);
        String category4 = preferences.getString("category3", null);

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

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            Log.d(TAG,"위젯을 업데이트 합니다.");
            categoryNumber = 0;
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

        try {
            // 앱 실행 인텐트 설정
            Intent launchIntent = getLaunchIntent(packageManager, packageName, category, context);
            if (launchIntent != null) {
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                views.setOnClickPendingIntent(imageViewId, pendingIntent);
            }


            // 앱의 아이콘과 이름을 가져옴
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