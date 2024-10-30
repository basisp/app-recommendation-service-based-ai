package com.example.ai_customizing;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.ai_customizing.data.AppUsageFetcher;
import com.example.ai_customizing.data.OnAppDataFetchedListener;
import com.example.ai_customizing.network.SendServer;


public class AppUsageFetchWorker extends Worker {
    private String TAG = "AppUsageFetchWorker";

    public AppUsageFetchWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Activity 또는 적절한 위치에서 호출하는 코드
            AppUsageFetcher appUsageFetcher = new AppUsageFetcher(getApplicationContext());
            appUsageFetcher.fetchAndSaveAppsByCategory(getApplicationContext(), new OnAppDataFetchedListener() {
                @Override
                public void onDataFetched() {
                    // 데이터 처리가 완료된 후 서버로 데이터 전송
                    SendServer sendServer = new SendServer(getApplicationContext());
                    sendServer.sendAllAppDataToServer();
                    Log.d(TAG, "doWork : 서버로 보내기 성공.");
                }
            });



            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "doWork : 작업 실패");
            return Result.failure();
        }
    }
}
