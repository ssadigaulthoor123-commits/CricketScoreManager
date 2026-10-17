package com.example.cricketscorer.utils;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.cricketscorer.R;
import com.example.cricketscorer.activities.MatchSummaryActivity;

public class NotificationHelper {

    private static final String TAG = "NOTIF_DEBUG";

    public static final String CHANNEL_MATCH = "match_completion_channel";
    public static final String CHANNEL_SETTINGS = "settings_notification_channel";

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                NotificationChannel matchChannel = new NotificationChannel(
                        CHANNEL_MATCH,
                        "Cricket Score Manager",
                        NotificationManager.IMPORTANCE_HIGH
                );
                matchChannel.setDescription("Notifications when a cricket match finishes");

                NotificationChannel settingsChannel = new NotificationChannel(
                        CHANNEL_SETTINGS,
                        "Cricket Settings Updates",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                settingsChannel.setDescription("Notifications for theme changes and data resets");

                nm.createNotificationChannel(matchChannel);
                nm.createNotificationChannel(settingsChannel);
                Log.d(TAG, "Notification channels created successfully");
            }
        }
    }

    public static boolean checkAndRequestPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean granted = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
            Log.d(TAG, "POST_NOTIFICATIONS permission status: " + granted);
            if (!granted) {
                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
                return false;
            }
            return true;
        }
        return true;
    }

    public static void sendMatchCompletedNotification(Context context, long matchId, String result) {
        Log.d(TAG, "Notification trigger reached: sendMatchCompletedNotification for matchId: " + matchId + " | Result: " + result);
        createNotificationChannels(context);

        Intent intent = new Intent(context, MatchSummaryActivity.class);
        intent.putExtra("match_id", matchId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) matchId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_MATCH)
                .setSmallIcon(R.drawable.ic_trophy)
                .setContentTitle("Match Completed")
                .setContentText(result)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                        ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                                == PackageManager.PERMISSION_GRANTED) {
                    nm.notify((int) matchId, builder.build());
                    Log.d(TAG, "Match completion notification posted successfully for matchId: " + matchId);
                } else {
                    Log.e(TAG, "Cannot post match completion notification: POST_NOTIFICATIONS permission not granted");
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to post match completion notification: " + e.getMessage(), e);
            }
        }
    }

    public static void sendThemeChangedNotification(Context context, String themeName) {
        Log.d(TAG, "Notification trigger reached: sendThemeChangedNotification -> Theme: " + themeName);
        createNotificationChannels(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_SETTINGS)
                .setSmallIcon(R.drawable.ic_settings)
                .setContentTitle("Theme Changed")
                .setContentText("App theme has been updated to " + themeName + " successfully.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                        ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                                == PackageManager.PERMISSION_GRANTED) {
                    nm.notify(2001, builder.build());
                    Log.d(TAG, "Theme changed notification posted successfully");
                } else {
                    Log.e(TAG, "Cannot post theme notification: POST_NOTIFICATIONS permission not granted");
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to post theme notification: " + e.getMessage(), e);
            }
        }
    }

    public static void sendMatchHistoryResetNotification(Context context) {
        Log.d(TAG, "Notification trigger reached: sendMatchHistoryResetNotification");
        createNotificationChannels(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_SETTINGS)
                .setSmallIcon(R.drawable.ic_history)
                .setContentTitle("Match History Reset")
                .setContentText("Match history has been cleared successfully.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                        ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                                == PackageManager.PERMISSION_GRANTED) {
                    nm.notify(2002, builder.build());
                    Log.d(TAG, "Match history reset notification posted successfully");
                } else {
                    Log.e(TAG, "Cannot post reset notification: POST_NOTIFICATIONS permission not granted");
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to post reset notification: " + e.getMessage(), e);
            }
        }
    }
}
