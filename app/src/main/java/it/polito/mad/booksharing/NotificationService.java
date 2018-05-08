package it.polito.mad.booksharing;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
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

import static com.google.android.gms.common.util.WorkSourceUtil.TAG;

public class NotificationService extends FirebaseMessagingService {

    private final String SENDER_CONSTANT = "sender_key";
    private final String CHAT_KEY_CONSTANT = "chat_key";
    private User user,sender;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            if(remoteMessage.getData().size() > 0){
                HashMap<String,String> metaData = new HashMap<>(remoteMessage.getData());
                final String senderKey = metaData.get(SENDER_CONSTANT);
                final String keyChat = metaData.get(CHAT_KEY_CONSTANT);
                final String body = remoteMessage.getNotification().getBody();

                getUserInfoFromSharedPref();
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(senderKey);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            //retrieve Sender user
                            sender = dataSnapshot.getValue(User.class);
                            displayNotification(body,sender,user,keyChat);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }



    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void displayNotification(String messageBody,User sender,User receiver, String keyChat) {
        Intent intent = new Intent(this, ChatPage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Bundle bundle = new Bundle();
        bundle.putParcelable("sender", receiver);
        bundle.putParcelable("receiver", sender);
        intent.putExtra("key_chat", keyChat);
        intent.putExtras(bundle);

        String title = sender.getName().getValue() + " " + sender.getSurname().getValue();

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = "channel_chat";
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "BookSharing",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        if(notificationManager != null){
            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        }
        else{
            Log.d("NOTIFICATION_MANAGER","Notification Manager Null Pointer");
        }
    }

    protected void getUserInfoFromSharedPref() {

        SharedPreferences sharedPref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        String defaultString = "";
        String userName = sharedPref.getString("user", defaultString);
        if (userName.equals(defaultString)) {
            user = new User();
            return;
        }
        Gson json = new Gson();
        user = json.fromJson(userName, User.class);
        if (user.getDescription().getValue().equals("")) {

            user.setDescription(new User.MyPair(getString(R.string.description_value), "public"));
        }
    }
}
