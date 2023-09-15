package com.example.cardview;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle the notification here
        createNotification(remoteMessage);
    }

    private void createNotification(RemoteMessage remoteMessage) {
        NotificationManager notificationCheck = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationCheck != null && !notificationCheck.areNotificationsEnabled()) {
            new AlertDialog.Builder(this)
                    .setMessage("Enable notifications to receive updates about your games.")
                    .setPositiveButton("Settings", (dialog, which) -> {
                        // Open app settings
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }

            NotificationChannel channel = new NotificationChannel("GameNotifications", "Game Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.createNotificationChannel(channel);

        String title = ""; // default title
        String body = ""; // default content

        if(remoteMessage.getData().containsKey("title")) {
            title = remoteMessage.getData().get("title");
        }

        if(remoteMessage.getData().containsKey("body")) {
            body = remoteMessage.getData().get("body");
        }
        // Create a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "GameNotifications")
                .setSmallIcon(R.drawable.ic_launcher_background) // Replace with your own notification icon
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Show the notification
        NotificationManagerCompat notification = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notification.notify(1, builder.build());
    }


    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        // Get current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            DatabaseReference db = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            db.child("fcmToken").setValue(token)
                    .addOnSuccessListener(aVoid -> Log.d("FCM", "Token updated successfully"))
                    .addOnFailureListener(e -> Log.e("FCM", "Failed to update token", e));
        }

    }
}
