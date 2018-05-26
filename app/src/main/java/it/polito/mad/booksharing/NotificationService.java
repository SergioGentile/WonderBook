package it.polito.mad.booksharing;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
    private final String RECEIVER_CONSTANT = "receiver";
    private final String CHAT_KEY_CONSTANT = "chatID";
    private final String CHAT_NOTIFICATION = "CHAT";
    private final String LENDING_NOTIFICATION = "LENDING";
    private final String LENDING_STATUS = "status";
    private User user, sender;
    private MyNotificationManager mNotificationManager;

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
            String notificationType = metaData.get("notificationType");
            if(notificationType.equals(CHAT_NOTIFICATION)){
                showChatMessage(metaData);
            }
            else if(notificationType.equals(LENDING_NOTIFICATION)){
                showLendingNotification(metaData);
            }

        }
    }

    private void showLendingNotification(HashMap<String, String> metaData) {
        final String status = metaData.get(LENDING_STATUS);
        final String sender_key = metaData.get("sender");
        final String receiver = metaData.get("receiver");

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(sender_key);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //retrieve Sender user
                    Gson json = new Gson();
                    user = json.fromJson(receiver, User.class);
                    sender = dataSnapshot.getValue(User.class);

                    mNotificationManager.displayStatusLendingNotification(status, user, sender);
                    Intent intent = new Intent("UpdateView");
                    broadcaster.sendBroadcast(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void showChatMessage(HashMap<String,String> metaData){
        final String senderKey = metaData.get(SENDER_CONSTANT);
        final String keyChat = metaData.get(CHAT_KEY_CONSTANT);
        final String receiver = metaData.get(RECEIVER_CONSTANT);
        final String body = metaData.get("body");

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

                    mNotificationManager.displayChatNotification(body, sender, user, keyChat);
                    Intent intent = new Intent("UpdateView");
                    Bundle bundle  = new Bundle();
                    bundle.putParcelable("sender", sender);
                    intent.putExtras(bundle);
                    broadcaster.sendBroadcast(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}