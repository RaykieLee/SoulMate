package com.example.soulmate;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ForegroundApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissionStatus();
        // 检查悬浮窗权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivity(intent);
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
        if (!OpenNotificationsUtil.isNotificationEnabledForApp(this)) {//未开启通知，去开启
            OpenNotificationsUtil.openNotificationSettingsForApp(this);
        }


        // 开启前台应用监听服务
//        startService(new Intent(this, ForegroundAppService.class));
    }

    private void checkPermissionStatus() {
        XXPermissions.with(this)
                // 申请单个权限
                .permission(Permission.RECORD_AUDIO)
                // 申请多个权限
                .permission(Permission.Group.CALENDAR)
                .permission(Permission.WRITE_EXTERNAL_STORAGE)
//                .permission(Permission.VIBRATE)
                // 设置权限请求拦截器（局部设置）
                //.interceptor(new PermissionInterceptor())
                // 设置不触发错误检测机制（局部设置）
                //.unchecked()
                .request(new OnPermissionCallback() {

                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        if (!all) {
                            toast("获取部分权限成功，但部分权限未正常授予");
                            return;
                        }
                        toast("获取录音和日历权限成功");
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        if (never) {
                            toast("被永久拒绝授权，请手动授予录音和日历权限");
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(getApplicationContext(), permissions);
                        } else {
                            toast("获取录音和日历权限失败");
                        }
                    }
                });
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OpenNotificationsUtil.OPEN_APP_NOTIFICATION) {
//            1.创建普通消息通知
            OpenNotificationsUtil.createNotification(this, "普通消息通知", "欢迎来到APP！", 0);

            //2.启动前台服务，创建服务常驻通知
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                startForegroundService(new Intent(this, ForegroundAppService.class));
            } else {
                startService(new Intent(this, ForegroundAppService.class));
            }
        }
    }
    private String getForegroundApp() {
        // 获取UsageStatsManager对象
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        // 获取当前时间
        long endTime = System.currentTimeMillis();
        long startTime = endTime - 24 * 60 * 60 * 1000; // 获取过去一天的应用程序使用情况
        // 获取应用程序的使用情况
        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
        // 遍历应用程序的使用情况，获取正在运行的应用程序的信息
        for (UsageStats usageStats : usageStatsList) {
            // 获取应用程序的包名
            String packageName = usageStats.getPackageName();

            // 获取应用程序的使用时间
            long totalTimeInForeground = usageStats.getTotalTimeInForeground();

            // 打印应用程序的相关信息
            Log.d("RunningAppInfo", "Package Name: " + packageName);
            Log.d("RunningAppInfo", "Total Time In Foreground: " + totalTimeInForeground);
        }
        return null;
    }
//    public static boolean canUsageStats(Context context) {
//        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
//        int mode = 0;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            mode = appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.getPackageName());
//        } else {
//            mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.getPackageName());
//        }
//        if (mode == AppOpsManager.MODE_DEFAULT && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            return (context.checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
//        } else {
//            return (mode == AppOpsManager.MODE_ALLOWED);
//        }
//    }
}