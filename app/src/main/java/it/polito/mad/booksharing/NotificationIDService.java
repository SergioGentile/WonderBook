package it.polito.mad.booksharing;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class NotificationIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        Log.d("FCM", "Refreshed token: " + FirebaseInstanceId.getInstance().getToken());

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            sendRegistrationToServer();
        }
    }

    public static void sendRegistrationToServer(){
        Log.d("FCM", "Sending token: " + FirebaseInstanceId.getInstance().getToken());
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/notificationToken");
        databaseReference.setValue(FirebaseInstanceId.getInstance().getToken());
    }

}
