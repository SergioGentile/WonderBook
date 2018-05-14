package it.polito.mad.booksharing;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

public class MyNotificationManager {
    private Context mCtx;
    private static MyNotificationManager mInstance;
    private final String GROUP_KEY = "it.polito.mad.BookSharing.CHAT";
    private final int SUMMARY_ID = 0;
    private final String CHANNEL_ID = "channel_chat";
    private int notificationCounter = 0;
    private Integer messageCounter = 0;
    private LocalBroadcastManager broadcaster;

    private MyNotificationManager(Context context) {
        mCtx = context;
        broadcaster = LocalBroadcastManager.getInstance(context);

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences("messageCounter",Context.MODE_PRIVATE);
        messageCounter = sharedPreferences.getInt("messageCounter",0);
    }

    public static synchronized MyNotificationManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MyNotificationManager(context);
        }
        return mInstance;
    }

    private void buildSimpleNotification(String messageBody, User sender, User receiver, String keyChat, NotificationManager notificationManager) {
        Intent intent = new Intent(mCtx, ActivityDispatcher.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Bundle bundle = new Bundle();
        bundle.putParcelable("sender", receiver);
        bundle.putParcelable("receiver", sender);
        intent.putExtra("key_chat", keyChat);
        intent.putExtras(bundle);
        intent.putExtra("dispatcherCode", 0);

        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mCtx);
        stackBuilder.addNextIntentWithParentStack(intent);

        int id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        //FLAG_UPDATE_CURRENT necessary otherwise the extras are lost
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT);

        String title = sender.getName().getValue() + " " + sender.getSurname().getValue();

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mCtx, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_logo_black_24dp)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setGroup(GROUP_KEY)
                        .setContentIntent(resultPendingIntent);

        notificationManager.notify(id, notificationBuilder.build());

    }

    private void buildSummaryNotification(String title, User user, int notificationCounter, NotificationManager notificationManager) {
        Intent intent = new Intent(mCtx, ActivityDispatcher.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Bundle bundle = new Bundle();
        bundle.putParcelable("user", user);
        intent.putExtras(bundle);
        intent.putExtra("dispatcherCode", notificationCounter);


        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mCtx);
        stackBuilder.addNextIntentWithParentStack(intent);

        //FLAG_UPDATE_CURRENT necessary otherwise the extras are lost
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(SUMMARY_ID, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder summaryNotification =
                new NotificationCompat.Builder(mCtx, CHANNEL_ID)
                        .setContentTitle(title)
                        //set content text to support devices running API level < 24
                        .setContentText(String.valueOf(notificationCounter) + " " + mCtx.getString(R.string.msg_notification))
                        .setAutoCancel(true)
                        .setContentIntent(resultPendingIntent)
                        .setSmallIcon(R.drawable.ic_logo_black_24dp)
                        .setGroup(GROUP_KEY)
                        .setGroupSummary(true);

        notificationManager.notify(SUMMARY_ID, summaryNotification.build());
    }

    public void displayNotification(String messageBody, User sender, User receiver, String keyChat) {

        android.app.NotificationManager notificationManager =
                (android.app.NotificationManager) mCtx.getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (notificationManager != null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                        "BookSharing",
                        android.app.NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            buildSimpleNotification(messageBody, sender, receiver, keyChat, notificationManager);
            buildSummaryNotification(mCtx.getString(R.string.incoming_msg), receiver, notificationCounter + 1, notificationManager);

            notificationCounter++;
            messageCounter++;

            SharedPreferences sharedPref = mCtx.getSharedPreferences("messageCounter",Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPref.edit();
            edit.putInt("messageCounter",messageCounter).commit();


        } else {
            Log.d("NOTIFICATION_MANAGER", "Notification Manager Null Pointer");
        }
    }

    public void clearNotification() {
        android.app.NotificationManager notificationManager =
                (android.app.NotificationManager) mCtx.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null && notificationCounter > 0) {
            notificationCounter = 0;
            notificationManager.cancelAll();
        }

    }

    public void setMessageCounter(int messageCounter){
        this.messageCounter = messageCounter;
    }

    public int getMessageCounter() {
        return messageCounter;
    }

    public void subtractMessageCounter(int value,String Uid) {
        if (messageCounter > 0) {
            messageCounter = messageCounter - value;
            if (messageCounter < 0) {
                messageCounter = 0;
            }
            SharedPreferences sharedPref = mCtx.getSharedPreferences("messageCounter",Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPref.edit();
            edit.putInt("messageCounter",messageCounter).commit();

        }
    }


}
