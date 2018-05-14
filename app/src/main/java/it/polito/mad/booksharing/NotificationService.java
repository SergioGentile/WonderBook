package it.polito.mad.booksharing;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;

public class NotificationService extends FirebaseMessagingService {

    private LocalBroadcastManager broadcaster;
    private final String SENDER_CONSTANT = "sender";
    private final String CHAT_KEY_CONSTANT = "chatID";
    private User user, sender;
    private MyNotificationManager mNotificationManager;
    private String lastChatReceiver;

    @Override
    public void onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this);
        mNotificationManager = MyNotificationManager.getInstance(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        // Check if message contains a notification payload.
        Log.d("MessageReceived", remoteMessage.getData().toString());
        if (remoteMessage.getData().size() > 0) {
            HashMap<String, String> metaData = new HashMap<>(remoteMessage.getData());
            final String senderKey = metaData.get(SENDER_CONSTANT);
            final String keyChat = metaData.get(CHAT_KEY_CONSTANT);
            final String body = metaData.get("body");
            final String receiver = metaData.get("receiver");

            SharedPreferences sharedPref = getSharedPreferences("chatReceiver", Context.MODE_PRIVATE);
            lastChatReceiver = sharedPref.getString("receiver_key", "");

            Log.d("MessageReceived", "Querying Firebase");

            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(senderKey);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        //retrieve Sender user
                        Gson json = new Gson();
                        user = json.fromJson(receiver, User.class);
                        sender = dataSnapshot.getValue(User.class);

                        Log.d("MessageReceived", "Deserialized data");

                        mNotificationManager.setMessageCounter(user.getMessageToRead());

                        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                        List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();
                        Log.d("MessageReceived", "Got app tasks");
                        if (tasks != null && !tasks.isEmpty()) {
                            Log.d("MessageReceived", "Tasks empty");
                            String className = tasks.get(0).getTaskInfo().topActivity.getClassName();
                            if (!className.contains("ChatPage")) {
                                //if current activity is not chatPage notify the user
                                mNotificationManager.displayNotification(body, sender, user, keyChat);
                                Intent intent = new Intent("UpdateView");
                                broadcaster.sendBroadcast(intent);
                            } else if (!lastChatReceiver.equals(sender.getKey()) || !ChatPage.isRunning) {
                                //if is chatPage but messageThread is different notify the user
                                mNotificationManager.displayNotification(body, sender, user, keyChat);
                                Intent intent = new Intent("UpdateView");
                                broadcaster.sendBroadcast(intent);
                            }
                        } else {
                            Log.d("MessageReceived", "Tasks not empty");
                            mNotificationManager.displayNotification(body, sender, user, keyChat);
                            Intent intent = new Intent("UpdateView");
                            broadcaster.sendBroadcast(intent);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}