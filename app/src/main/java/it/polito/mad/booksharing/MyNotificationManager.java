package it.polito.mad.booksharing;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;

public class MyNotificationManager {
    private final static int CHAT_PAGE = 0, MESSAGE_THREAD = 1, SHOW_MOVMENT = 2, MAIN_PAGE = 3;
    private final String LENDER = "LENDER", BORROWER = "BORROWER";
    private Context mCtx;
    private static MyNotificationManager mInstance;
    private final String GROUP_KEY_CHAT = "it.polito.mad.BookSharing.CHAT";
    private final String GROUP_KEY_LENDING_STATUS = "it.polito.mad.BookSharing.LENDING_STATUS";
    private final String GROUP_KEY_REJECTED = "it.polito.mad.BookSharing.REJECTED_LENDING";
    private final int SUMMARY_ID = 0, SUMMARY_ID_REJECTED = 1, SUMMARY_ID_STATUS = 2;
    private final String CHANNEL_ID = "channel_chat";
    private int notificationCounter = 0;
    private Integer messageCounter;
    private HashMap<String, Long> userKeyMessageCounter;
    private Integer pendingRequestCounter;
    private Integer lenderStatusNotificationCounter;
    private Integer borrowerStatusNotificationCounter;


    private MyNotificationManager(Context context) {
        mCtx = context;

        SharedPreferences sharedPreferences = mCtx.getSharedPreferences("notificationPref", Context.MODE_PRIVATE);
        messageCounter = sharedPreferences.getInt("messageCounter", 0);
        pendingRequestCounter = sharedPreferences.getInt("pendingRequestCounter", 0);
        lenderStatusNotificationCounter = sharedPreferences.getInt("lenderStatusNotificationCounter", 0);
        borrowerStatusNotificationCounter = sharedPreferences.getInt("borrowerStatusNotificationCounter", 0);
        Log.d("SetCounterSharePref", messageCounter.toString());

        userKeyMessageCounter = getMapFromSharedPref();
    }

    public static synchronized MyNotificationManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MyNotificationManager(context);
        }
        return mInstance;
    }

    public void displayChatNotification(String messageBody, User sender, User receiver, String keyChat) {


        SharedPreferences sharedPreferences = mCtx.getSharedPreferences("chatReceiver", Context.MODE_PRIVATE);
        String lastChatReceiver = sharedPreferences.getString("receiver_key", "");

        android.app.NotificationManager notificationManager =
                (android.app.NotificationManager) mCtx.getSystemService(Context.NOTIFICATION_SERVICE);

        ActivityManager activityManager = (ActivityManager) mCtx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();

        if (notificationManager != null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                        "BookSharing",
                        android.app.NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            if (tasks != null && !tasks.isEmpty()) {
                ComponentName componentName = tasks.get(0).getTaskInfo().topActivity;
                String className = "";
                if(componentName != null){
                    className = componentName.getClassName();
                }
                if (!className.contains("ChatPage") ) {
                    buildChatSimpleNotification(messageBody, sender, receiver, keyChat, notificationManager);
                    buildChatSummaryNotification(mCtx.getString(R.string.incoming_msg), receiver, notificationCounter + 1, notificationManager);
                    notificationCounter++;
                    messageCounter++;

                    if (userKeyMessageCounter.containsKey(sender.getKey())) {
                        Long currentCounter = userKeyMessageCounter.get(sender.getKey());
                        currentCounter++;
                        userKeyMessageCounter.put(sender.getKey(), new Long(currentCounter));
                    } else {
                        userKeyMessageCounter.put(sender.getKey(), new Long(1));
                    }
                    Log.d("NotificationManager", "User " + userKeyMessageCounter.get(sender.getKey()) + " notification");

                } else if (!lastChatReceiver.equals(sender.getKey()) || !ChatPage.isRunning) {
                    buildChatSimpleNotification(messageBody, sender, receiver, keyChat, notificationManager);
                    buildChatSummaryNotification(mCtx.getString(R.string.incoming_msg), receiver, notificationCounter + 1, notificationManager);
                    notificationCounter++;
                    messageCounter++;

                    if (userKeyMessageCounter.containsKey(sender.getKey())) {
                        Long currentCounter = userKeyMessageCounter.get(sender.getKey());
                        currentCounter++;
                        userKeyMessageCounter.put(sender.getKey(), new Long(currentCounter));
                    } else {
                        userKeyMessageCounter.put(sender.getKey(), new Long(1));
                    }
                    Log.d("NotificationManager", "User " + userKeyMessageCounter.get(sender.getKey()) + " notification");
                }
            } else {
                buildChatSimpleNotification(messageBody, sender, receiver, keyChat, notificationManager);
                buildChatSummaryNotification(mCtx.getString(R.string.incoming_msg), receiver, notificationCounter + 1, notificationManager);
                notificationCounter++;
                messageCounter++;


                if (userKeyMessageCounter.containsKey(sender.getKey())) {
                    Long currentCounter = userKeyMessageCounter.get(sender.getKey());
                    currentCounter++;
                    userKeyMessageCounter.put(sender.getKey(), new Long(currentCounter));
                } else {
                    userKeyMessageCounter.put(sender.getKey(), new Long(1));
                }
                Log.d("NotificationManager", "User " + userKeyMessageCounter.get(sender.getKey()) + " notification");
            }
            ShortcutBadger.applyCount(mCtx, messageCounter + pendingRequestCounter + lenderStatusNotificationCounter);

            SharedPreferences sharedPref = mCtx.getSharedPreferences("notificationPref", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPref.edit();
            edit.putInt("messageCounter", messageCounter).commit();
            saveMap();
        }

    }

    public void displayStatusLendingNotification(HashMap<String, String> statusMap, User user, User sender) {
        int intent = SHOW_MOVMENT;
        SharedPreferences sharedPref = mCtx.getSharedPreferences("notificationPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();

        android.app.NotificationManager notificationManager =
                (android.app.NotificationManager) mCtx.getSystemService(Context.NOTIFICATION_SERVICE);

        ActivityManager activityManager = (ActivityManager) mCtx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();

        if (notificationManager != null) {

            //switching according status value
            String status = statusMap.get("status");
            if (status.equals("sended")) {
                pendingRequestCounter++;
                edit.putInt("pendingRequestCounter", pendingRequestCounter).commit();
                ShortcutBadger.applyCount(mCtx, getTotalNumberOfNotifications());
                return;
            } else if (status.equals("canceled")) {
                pendingRequestCounter--;
                edit.putInt("pendingRequestCounter", pendingRequestCounter).commit();
                ShortcutBadger.applyCount(mCtx, getTotalNumberOfNotifications());
                return;
            } else if (status.equals("rejected")) {
                intent = MAIN_PAGE;
            } else if (status.equals("accepted")) {
                lenderStatusNotificationCounter++;
                edit.putInt("lenderStatusNotificationCounter", lenderStatusNotificationCounter).commit();
            } else if (status.equals("wait")) {
                if (statusMap.get("endRequestBy").equals(LENDER)) {
                    lenderStatusNotificationCounter++;
                    edit.putInt("lenderStatusNotificationCounter", lenderStatusNotificationCounter).commit();
                } else if (statusMap.get("endRequestBy").equals(BORROWER)) {
                    borrowerStatusNotificationCounter++;
                    edit.putInt("borrowerStatusNotificationCounter", borrowerStatusNotificationCounter).commit();
                }
            }

            //Now are goiing to display notifications
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                        "BookSharing",
                        android.app.NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            if (intent == SHOW_MOVMENT) {
                //display notification
                int showMovmentTotalNotifications = lenderStatusNotificationCounter + borrowerStatusNotificationCounter;
                buildChangeStatusSimpleNotification(status, user, sender, notificationManager);
                buildChangeStatusSummaryNotification(user, showMovmentTotalNotifications, notificationManager);
                notificationCounter++;
            } else if (intent == MAIN_PAGE) {
                buildRejectedSimpleNotification(user, sender, notificationManager);
                buildRejectedSummaryNotification(user, notificationManager);
                notificationCounter++;
            }

            ShortcutBadger.applyCount(mCtx, getTotalNumberOfNotifications());
        }
    }

    private void buildRejectedSummaryNotification(User user, NotificationManager notificationManager) {
        Intent intent = new Intent(mCtx, ActivityDispatcher.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //Show Chat Page, Chat message is arrived
        Bundle bundle = new Bundle();
        bundle.putParcelable("user", user);
        intent.putExtras(bundle);
        intent.putExtra("dispatcherCode", MAIN_PAGE);

        String title = mCtx.getString(R.string.title_rejected_request);
        String messageBody = mCtx.getString(R.string.message_body_rejected_summary);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mCtx);
        stackBuilder.addNextIntentWithParentStack(intent);

        int id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        //FLAG_UPDATE_CURRENT necessary otherwise the extras are lost
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder summaryNotification =
                new NotificationCompat.Builder(mCtx, CHANNEL_ID)
                        .setContentTitle(title)
                        //set content text to support devices running API level < 24
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setContentIntent(resultPendingIntent)
                        .setSmallIcon(R.drawable.ic_logo_black_24dp)
                        .setGroup(GROUP_KEY_REJECTED)
                        .setGroupSummary(true);

        notificationManager.notify(SUMMARY_ID_REJECTED, summaryNotification.build());
    }

    private void buildRejectedSimpleNotification(User user, User sender, NotificationManager notificationManager) {
        //Sent from Borrower to Lender
        Intent intent = new Intent(mCtx, ActivityDispatcher.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //Show Chat Page, Chat message is arrived
        Bundle bundle = new Bundle();
        bundle.putParcelable("user", user);
        intent.putExtras(bundle);
        intent.putExtra("dispatcherCode", MAIN_PAGE);

        String messageBody = mCtx.getString(R.string.message_body_rejected_simple) + " " + sender.getName().getValue() + " " + sender.getSurname().getValue();

        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mCtx);
        stackBuilder.addNextIntentWithParentStack(intent);

        int id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        //FLAG_UPDATE_CURRENT necessary otherwise the extras are lost
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT);

        String title = mCtx.getString(R.string.title_rejected_request);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mCtx, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_logo_black_24dp)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setGroup(GROUP_KEY_REJECTED)
                        .setContentIntent(resultPendingIntent);

        notificationManager.notify(id, notificationBuilder.build());

    }

    private void buildChatSimpleNotification(String messageBody, User sender, User receiver, String keyChat, NotificationManager notificationManager) {
        Intent intent = new Intent(mCtx, ActivityDispatcher.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //Show Chat Page, Chat message is arrived
        Bundle bundle = new Bundle();
        bundle.putParcelable("sender", receiver);
        bundle.putParcelable("receiver", sender);
        intent.putExtra("key_chat", keyChat);
        intent.putExtras(bundle);
        intent.putExtra("dispatcherCode", CHAT_PAGE);


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
                        .setGroup(GROUP_KEY_CHAT)
                        .setContentIntent(resultPendingIntent);

        notificationManager.notify(id, notificationBuilder.build());

    }

    private void buildChatSummaryNotification(String title, User user, int notificationCounter, NotificationManager notificationManager) {
        Intent intent = new Intent(mCtx, ActivityDispatcher.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


        Bundle bundle = new Bundle();
        bundle.putParcelable("user", user);
        intent.putExtras(bundle);
        intent.putExtra("dispatcherCode", MESSAGE_THREAD);

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
                        .setGroup(GROUP_KEY_CHAT)
                        .setGroupSummary(true);

        notificationManager.notify(SUMMARY_ID, summaryNotification.build());
    }

    private void buildChangeStatusSummaryNotification(User user, int changeLendingStatusCounter, NotificationManager notificationManager) {
        Intent intent = new Intent(mCtx, ActivityDispatcher.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //Show Chat Page, Chat message is arrived
        Bundle bundle = new Bundle();
        bundle.putParcelable("user", user);
        intent.putExtras(bundle);
        intent.putExtra("dispatcherCode", SHOW_MOVMENT);

        String title = mCtx.getString(R.string.title_status_lending);
        String messageBody = String.valueOf(changeLendingStatusCounter) + " " + mCtx.getString(R.string.message_body_status_lending);

        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mCtx);
        stackBuilder.addNextIntentWithParentStack(intent);

        int id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        //FLAG_UPDATE_CURRENT necessary otherwise the extras are lost
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder summaryNotification =
                new NotificationCompat.Builder(mCtx, CHANNEL_ID)
                        .setContentTitle(title)
                        //set content text to support devices running API level < 24
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setContentIntent(resultPendingIntent)
                        .setSmallIcon(R.drawable.ic_logo_black_24dp)
                        .setGroup(GROUP_KEY_LENDING_STATUS)
                        .setGroupSummary(true);

        notificationManager.notify(SUMMARY_ID_REJECTED, summaryNotification.build());

    }

    private void buildChangeStatusSimpleNotification(String status, User user, User sender, NotificationManager notificationManager) {
        //Sent from Lender to Borrower
        Intent intent = new Intent(mCtx, ActivityDispatcher.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //Show Chat Page, Chat message is arrived
        Bundle bundle = new Bundle();
        bundle.putParcelable("user", user);
        intent.putExtras(bundle);
        intent.putExtra("dispatcherCode", SHOW_MOVMENT);

        String title = "";
        String messageBody = "";
        if (status.equals("accepted")) {
            title = mCtx.getString(R.string.title_accepted_loan);
            messageBody = sender.getName().getValue() + " " + sender.getSurname().getValue() + " " + mCtx.getString(R.string.message_body_accepted);
        } else if (status.equals("wait")) {
            title = mCtx.getString(R.string.loan_wait_title);
            messageBody = sender.getName().getValue() + " " + sender.getSurname().getValue() + " " + mCtx.getString(R.string.message_body_wait);
        }

        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mCtx);
        stackBuilder.addNextIntentWithParentStack(intent);

        int id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        //FLAG_UPDATE_CURRENT necessary otherwise the extras are lost
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT);


        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mCtx, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_logo_black_24dp)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setGroup(GROUP_KEY_LENDING_STATUS)
                        .setContentIntent(resultPendingIntent);

        notificationManager.notify(id, notificationBuilder.build());

    }

    public void clearNotificationUser(String key) {
        Long currentNotification = new Long(0);
        if (userKeyMessageCounter.containsKey(key)) {
            currentNotification = userKeyMessageCounter.get(key);
        }
        userKeyMessageCounter.put(key, new Long(0));
        subtractMessageCounter(currentNotification.intValue());

    }

    public void clearNotification() {
        android.app.NotificationManager notificationManager =
                (android.app.NotificationManager) mCtx.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null && notificationCounter > 0) {
            notificationCounter = 0;
            notificationManager.cancelAll();
        }
    }

    public void setMessageCounter(Integer messageCounter) {
        this.messageCounter = messageCounter;
        if (this.messageCounter <= 0) {
            ShortcutBadger.removeCount(mCtx);
            this.messageCounter = 0;
        } else {
            ShortcutBadger.applyCount(mCtx, getTotalNumberOfNotifications());
        }
        SharedPreferences sharedPref = mCtx.getSharedPreferences("notificationPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.putInt("messageCounter", this.messageCounter).commit();
        Log.d("SetCounter", messageCounter.toString());

    }

    public int getMessageCounter() {
        return messageCounter;
    }

    public void subtractMessageCounter(int value) {
        if (messageCounter > 0) {
            messageCounter = messageCounter - value;
            if (messageCounter <= 0) {
                ShortcutBadger.removeCount(mCtx);
                messageCounter = 0;
            } else {
                ShortcutBadger.applyCount(mCtx, getTotalNumberOfNotifications());
            }

            SharedPreferences sharedPref = mCtx.getSharedPreferences("notificationPref", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPref.edit();
            edit.putInt("messageCounter", messageCounter).commit();
            saveMap();

        }
    }

    public int getPendingRequestCounter() {
        return pendingRequestCounter;
    }

    public void setPendingRequestCounter(Integer pendingRequestCounter) {
        this.pendingRequestCounter = pendingRequestCounter;
        if (this.pendingRequestCounter <= 0) {
            ShortcutBadger.removeCount(mCtx);
            this.pendingRequestCounter = 0;
        } else {
            ShortcutBadger.applyCount(mCtx, getTotalNumberOfNotifications());
        }
        SharedPreferences sharedPref = mCtx.getSharedPreferences("notificationPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.putInt("pendingRequestCounter", this.pendingRequestCounter).commit();
    }

    public void subtractPendingRequestCounter(int value) {
        if (pendingRequestCounter > 0) {
            pendingRequestCounter = pendingRequestCounter - value;
            if (pendingRequestCounter <= 0) {
                pendingRequestCounter = 0;
                ShortcutBadger.removeCount(mCtx);
            } else {
                ShortcutBadger.applyCount(mCtx, getTotalNumberOfNotifications());
            }

            SharedPreferences sharedPref = mCtx.getSharedPreferences("notificationPref", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPref.edit();
            edit.putInt("pendingRequestCounter", pendingRequestCounter).commit();
        }
    }

    public void setLenderStatusNotificationCounter(Integer lenderStatusNotificationCounter) {
        this.lenderStatusNotificationCounter = lenderStatusNotificationCounter;

        if (this.lenderStatusNotificationCounter <= 0) {
            ShortcutBadger.removeCount(mCtx);
            this.lenderStatusNotificationCounter = 0;
        } else {
            ShortcutBadger.applyCount(mCtx, getTotalNumberOfNotifications());
        }
        SharedPreferences sharedPref = mCtx.getSharedPreferences("notificationPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.putInt("lenderStatusNotificationCounter", this.lenderStatusNotificationCounter).commit();
    }

    public int getLenderStatusNotificationCounter() {
        return lenderStatusNotificationCounter;
    }

    public void subtractLenderStatusNotificationCounter(int value) {
        if (lenderStatusNotificationCounter > 0) {
            lenderStatusNotificationCounter = lenderStatusNotificationCounter - value;
            if (lenderStatusNotificationCounter <= 0) {
                lenderStatusNotificationCounter = 0;
                ShortcutBadger.removeCount(mCtx);
            } else {
                ShortcutBadger.applyCount(mCtx, getTotalNumberOfNotifications());
            }

            SharedPreferences sharedPref = mCtx.getSharedPreferences("notificationPref", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedPref.edit();
            edit.putInt("lenderStatusNotificationCounter", lenderStatusNotificationCounter).commit();
        }
    }

    public int getBorrowerStatusNotificationCounter() {
        return this.borrowerStatusNotificationCounter;
    }

    public void setBorrowerStatusNotificationCounter(Integer value) {
        this.borrowerStatusNotificationCounter = value;

        if (this.borrowerStatusNotificationCounter <= 0) {
            ShortcutBadger.removeCount(mCtx);
            this.borrowerStatusNotificationCounter = 0;
        } else {
            ShortcutBadger.applyCount(mCtx, getTotalNumberOfNotifications());
        }
        SharedPreferences sharedPref = mCtx.getSharedPreferences("notificationPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.putInt("borrowerStatusNotificationCounter", this.borrowerStatusNotificationCounter).commit();
    }

    public void subtractBorrowerStatusNotificationCounter(int value) {
        this.borrowerStatusNotificationCounter = this.borrowerStatusNotificationCounter - value;

        if (this.borrowerStatusNotificationCounter <= 0) {
            ShortcutBadger.removeCount(mCtx);
            this.borrowerStatusNotificationCounter = 0;
        } else {
            ShortcutBadger.applyCount(mCtx, getTotalNumberOfNotifications());
        }
        SharedPreferences sharedPref = mCtx.getSharedPreferences("notificationPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.putInt("borrowerStatusNotificationCounter", this.borrowerStatusNotificationCounter).commit();
    }

    public int getTotalNumberOfNotifications() {
        return messageCounter + pendingRequestCounter + lenderStatusNotificationCounter + borrowerStatusNotificationCounter;
    }

    public int getChangeStatusNotifications() {
        return lenderStatusNotificationCounter + borrowerStatusNotificationCounter;
    }

    public Long getReceiverCount(String key) {
        if (userKeyMessageCounter.containsKey(key)) {
            return userKeyMessageCounter.get(key);
        }
        return new Long(0);
    }

    private void saveMap() {

        SharedPreferences sharedPref = mCtx.getSharedPreferences("notificationMap", Context.MODE_PRIVATE);
        if (sharedPref != null) {
            SharedPreferences.Editor edit = sharedPref.edit();
            Gson json = new GsonBuilder().create();
            String toStore = json.toJson(userKeyMessageCounter);
            edit.putString("notificationMap", toStore).apply();
            edit.commit();

        }
    }

    private HashMap<String, Long> getMapFromSharedPref() {
        HashMap<String, Long> outputMap = new HashMap<>();
        SharedPreferences SharedPref = mCtx.getSharedPreferences("notificationMap", Context.MODE_PRIVATE);
        String map = SharedPref.getString("notificationMap", "noMap");
        if (map.equals("noMap")) {
            return outputMap;
        }
        Gson json = new Gson();
        Type typeOfHashmap = new TypeToken<HashMap<String, Long>>() {
        }.getType();
        outputMap = json.fromJson(map, typeOfHashmap);
        return outputMap;
    }

    public HashMap<String, Long> getMap() {
        if (userKeyMessageCounter == null) {
            userKeyMessageCounter = new HashMap<>();
        }

        return new HashMap<String, Long>(userKeyMessageCounter);
    }

    public void setMap(HashMap value) {
        this.userKeyMessageCounter = new HashMap<>(value);
        saveMap();
    }
}
