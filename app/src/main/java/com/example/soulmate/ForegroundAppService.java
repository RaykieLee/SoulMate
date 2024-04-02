package com.example.soulmate;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

public class ForegroundAppService extends Service {

    private static final String TAG = "ForegroundAppService";
    private static final int INTERVAL = 1000; // 监听间隔时间，单位：毫秒

    private Handler mHandler;
    private Runnable mRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        Notification notification = OpenNotificationsUtil.createNotification(this, "服务常驻通知", "APP正在运行中...", 0);
        startForeground(OpenNotificationsUtil.OPEN_SERVICE_NOTIFICATION_ID, notification);//显示常驻通知
        Log.d(TAG, "Foreground App: onCreate");
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                String foregroundApp = getForegroundPackageName(getApplicationContext());
                Log.d(TAG, "Foreground App: " + foregroundApp);
//                    Toast.makeText(getApplicationContext(), foregroundApp, Toast.LENGTH_LONG).show();
                mHandler.postDelayed(this, INTERVAL);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Foreground App: onStartCommand");
        mHandler.postDelayed(mRunnable, INTERVAL);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Foreground App: onDestroy");
        mHandler.removeCallbacks(mRunnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public static String getForegroundPackageName(Context context) {
        //Get the app record in the last month
        Calendar calendar = Calendar.getInstance();
        final long end = calendar.getTimeInMillis();
        calendar.add(Calendar.MONTH, -1);
        final long start = calendar.getTimeInMillis();

        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        UsageEvents usageEvents = usageStatsManager.queryEvents(start, end);
        UsageEvents.Event event = new UsageEvents.Event();
        String packageName = null;
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                packageName = event.getPackageName();
            }
        }
        return packageName;
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private String getForegroundApp() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long ts = System.currentTimeMillis();
        //第一个参数： 按照时间间隔来查询  第二个：开始时间 第三个：截止时间
        //通过给定的开始与结束时间  INTERVAL_BEST是按照最合适的时间间隔类型
        //还可以有：INTERVAL_DAILY  WEEKLY MONTHLY YEARLY
        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, 0, ts);
        //返回结果中的UsageStats的官方解释是：包含特定时间范围内应用程序包的使用统计资料。
        if (queryUsageStats == null || queryUsageStats.isEmpty()) {
            return null;
        }

        UsageStats recentStats = null;
        for (UsageStats usageStats : queryUsageStats) {
            if (recentStats == null ||
                    recentStats.getLastTimeUsed() < usageStats.getLastTimeUsed()) {
                recentStats = usageStats;
            }
        }

        Log.d(TAG, "getForegroundApp: " + recentStats.getPackageName());
        return recentStats.getPackageName();
    }
    public void startForegroundChannel() {
        Log.d("MrDouYa","MyForegroundService startForegroundChannel");
        String channelId = "startForeground";
        String name = "Foreground chanel";
        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(channelId,name, NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

            Notification notification = new Notification.Builder(ForegroundAppService.this,channelId)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Foreground Service")
                    .setContentText("this is a Foreground Service")
                    .setWhen(System.currentTimeMillis())
                    .build();
            startForeground(1, notification);
        }


    }

}