package com.example.freefood;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.freefood.activities.MainActivity;
import com.example.freefood.activities.PostDetailActivity;
import com.example.freefood.utils.Utils;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "mFCMService";
    private final String ADMIN_CHANNEL_ID ="admin_channel";
    private static final String SUBSCRIBE_TO = "all";
    private static double notificationsRadius = 5;

    @Override
    public void onNewToken (String token) {
        // Here you can include more complex logic for users to only be subscribed to certain topics
        FirebaseMessaging.getInstance().subscribeToTopic(SUBSCRIBE_TO);
        Log.i(TAG, "onTokenRefresh completed with token: " + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();

        // do not display notification if current user posted it
        String postUserId = data.get("userId");
        if (postUserId.equals(ParseUser.getCurrentUser().getObjectId())) {
            return;
        }

        // only displays notification if you are within notificationsRadius from the post location
        double latitude = Double.parseDouble(data.get("latitude"));
        double longitude = Double.parseDouble(data.get("longitude"));

        final Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra("post_id", data.get("postId"));
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationID = new Random().nextInt(3000);

        /*
          Apps targeting SDK 26 or above (Android O) must implement notification channels and add its notifications to at least one of them. Therefore, confirm if version is Oreo or higher, then setup notification channel
        */
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels(notificationManager);
        }


        ParseGeoPoint postLocation = new ParseGeoPoint(latitude, longitude);
        if (Utils.getRelativeDistance(postLocation) > notificationsRadius) {
            return;
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this , 0, intent, PendingIntent.FLAG_ONE_SHOT);
        // You can change the notification icon/picture here
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_baseline_fastfood_24);

        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_fastfood_24)
                .setLargeIcon(largeIcon)
                .setContentTitle(data.get("title"))
                .setContentText(data.get("message"))
                .setAutoCancel(true)
                .setSound(notificationSoundUri)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(data.get("message")));

        //Set notification color to match your app color template
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {notificationBuilder.setColor(getResources().getColor(R.color.primaryDarkColor));
        }
        notificationManager.notify(notificationID, notificationBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(NotificationManager notificationManager) {
        CharSequence adminChannelName = "New notification";
        String adminChannelDescription = "Device to device notification";

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

    public static double getNotificationsRadius() {
        return notificationsRadius;
    }

    public static void setNotificationsRadius(double radius) {
        notificationsRadius = radius;
    }

}
