package it.polito.mad.booksharing;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompatBase;
import android.util.Log;



import java.util.Date;

public class MyNotificationManager {
    private Context mCtx;
    private static MyNotificationManager mInstance;
    private final String GROUP_KEY = "it.polito.mad.BookSharing.CHAT";
    private final int SUMMARY_ID = 0;
    private final String CHANNEL_ID = "channel_chat";
    private int notificationCounter = 0;
    private int messageCounter = 0;

    private MyNotificationManager(Context context) {
        mCtx = context;
    }

    public static synchronized MyNotificationManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MyNotificationManager(context);
        }
        return mInstance;
    }

    private void buildSimpleNotification(String messageBody,User sender,User receiver, String keyChat, NotificationManager notificationManager) {
        Intent intent = new Intent(mCtx, ActivityDispatcher.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Bundle bundle = new Bundle();
        bundle.putParcelable("sender", receiver);
        bundle.putParcelable("receiver", sender);
        intent.putExtra("key_chat", keyChat);
        intent.putExtras(bundle);
        intent.putExtra("notificationCounter", notificationCounter);

        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mCtx);
        stackBuilder.addNextIntentWithParentStack(intent);

        int id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        //FLAG_UPDATE_CURRENT necessary otherwise the extras are lost
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT);

        String title = sender.getName().getValue() + " " + sender.getSurname().getValue();

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mCtx, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setGroup(GROUP_KEY)
                        .setContentIntent(resultPendingIntent);

        notificationManager.notify(id, notificationBuilder.build());

    }

    private  void  buildSummaryNotification(String title, User user, int notificationCounter, NotificationManager notificationManager){
        Intent intent = new Intent(mCtx, ActivityDispatcher.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Bundle bundle = new Bundle();
        bundle.putParcelable("user", user);
        intent.putExtras(bundle);
        intent.putExtra("notificationCounter", notificationCounter);


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
                        .setContentText(String.valueOf(notificationCounter)+" new messages")
                        .setAutoCancel(true)
                        .setContentIntent(resultPendingIntent)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setGroup(GROUP_KEY)
                        .setGroupSummary(true);

        notificationManager.notify(SUMMARY_ID, summaryNotification.build());
    }

    public void displayNotification(String messageBody,User sender,User receiver, String keyChat){

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

            buildSummaryNotification("Incoming Message", receiver, notificationCounter+1,notificationManager);
            buildSimpleNotification(messageBody, sender, receiver, keyChat,notificationManager);

            notificationCounter++;
            messageCounter++;

        } else {
            Log.d("NOTIFICATION_MANAGER", "Notification Manager Null Pointer");
        }
    }

    public void clearNotification(){
        android.app.NotificationManager notificationManager =
                (android.app.NotificationManager) mCtx.getSystemService(Context.NOTIFICATION_SERVICE);

        if(notificationManager != null && notificationCounter > 0){
            notificationCounter = 0;
            notificationManager.cancelAll();
        }

    }

    public int getMessageCounter() {
        return messageCounter;
    }

    public void subtractMessageCounter(int value){
        if(messageCounter > 0){
            messageCounter = messageCounter - value;
            if(messageCounter < 0){
                messageCounter = 0;
            }
        }
    }
}
