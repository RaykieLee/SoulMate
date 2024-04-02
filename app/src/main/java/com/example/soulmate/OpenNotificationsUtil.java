package com.example.soulmate;


import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Set;

public class OpenNotificationsUtil {

    public static final int OPEN_APP_NOTIFICATION = 200;
    public static final int OPEN_LISTENER_SETTINGS = 201;

    public static final int OPEN_SERVICE_NOTIFICATION_ID = 1;
    public static final int OPEN_MESSAGE_NOTIFICATION_ID = 2;

    /**
     * 是否开启APP通知
     *
     * @param context
     * @return
     */
    public static boolean isNotificationEnabledForApp(Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    /**
     * 开启APP通知
     *
     * @param context
     */
    public static void openNotificationSettingsForApp(Activity context) {
        try {
            // 锤子不能单个打开自己APP的 通知设置 界面
            if (android.os.Build.BRAND.equalsIgnoreCase("SMARTISAN")) {
                Toast.makeText(context, "锤子手机请手动打开 设置--通知中心--开启IMFitPro通知开关", Toast.LENGTH_LONG).show();
                return;
            }
            // Links to this app's notification settings.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra("android.provider.extra.APP_PACKAGE", context.getPackageName());
                intent.putExtra("app_package", context.getPackageName());
                intent.putExtra("app_uid", context.getApplicationInfo().uid);
                context.startActivityForResult(intent, OPEN_APP_NOTIFICATION);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否开启通知使用权限
     *
     * @param context
     * @return
     */
    public static boolean isNotificationListenerEnabled(Context context) {
        try {
            Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(context);
            if (packageNames.contains(context.getPackageName())) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 开启通知使用权限
     */
    public static void openNotificationListenSettings(Activity context) {
        try {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            context.startActivityForResult(intent, OPEN_LISTENER_SETTINGS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示通知栏通知
     *
     * @param context
     * @param title
     * @param text
     * @param type    0服务常驻通知， 1普通消息通知
     * @return
     */
    public static Notification createNotification(Context context, String title, String text, int type) {
        boolean isSilent = true;//是否静音
        boolean isOngoing = true;//是否持续(为不消失的常驻通知)
        String channelName = "服务常驻通知";
        String channelId = "Service_Id";
        String category = Notification.CATEGORY_SERVICE;
        if (type == 1) {
            isSilent = false;
            isOngoing = false;
            channelName = "普通消息通知";
            channelId = "Message_Id";
            category = Notification.CATEGORY_STATUS;
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent nfIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, nfIntent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setContentIntent(pendingIntent) //设置PendingIntent
                .setSmallIcon(R.mipmap.ic_launcher) //设置状态栏内的小图标
                .setContentTitle(title) //设置标题
                .setContentText(text) //设置内容
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)//设置通知公开可见
                .setOngoing(isOngoing)//设置持续(不消失的常驻通知)
                .setCategory(category)//设置类别
                .setPriority(NotificationCompat.PRIORITY_MAX);//优先级为：重要通知
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//安卓8.0以上系统要求通知设置Channel,否则会报错
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);//锁屏显示通知
            notificationManager.createNotificationChannel(notificationChannel);
            builder.setChannelId(channelId);
        }

        Notification notification = builder.build(); //获取构建好的Notification
        if (type == 1) {//普通消息通知，更新通知
            notificationManager.notify(OPEN_MESSAGE_NOTIFICATION_ID, notification);
        }
        return notification;
    }
}