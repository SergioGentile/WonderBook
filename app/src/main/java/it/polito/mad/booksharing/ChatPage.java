package it.polito.mad.booksharing;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatPage extends AppCompatActivity {

    private User sender, receiver;
    boolean isRunning;
    private String chatKey;
    private FirebaseListAdapter<ChatMessage> adapter;
    private FloatingActionButton fab;
    private CircleImageView profileImage;
    private TextView tvName, tvStatus;
    private ImageButton backButton;

    private FirebaseDatabase firebaseDatabaseAccess;
    private DatabaseReference databaseReferenceAccess;
    private boolean backPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_page);


        isRunning = true;

        sender = getIntent().getExtras().getParcelable("sender");
        receiver = getIntent().getExtras().getParcelable("receiver");
        chatKey = getIntent().getStringExtra("key_chat");

        /*
         *  write receiver key on sharedPreferences, the Notification will read it.
         *  If message is sent from receiver, notification won't be submitted
         **/
        SharedPreferences sharedPref = getSharedPreferences("chatReceiver", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.putString("receiver_key", receiver.getKey());
        edit.commit();

        setStatus();

        tvName = (TextView) findViewById(R.id.toolbarName);
        tvName.setText(receiver.getName().getValue() + " " + receiver.getSurname().getValue());

        profileImage = (CircleImageView) findViewById(R.id.toolbarPhoto);
        Picasso.with(ChatPage.this).load(receiver.getUser_image_url()).into(profileImage);

        backButton = (ImageButton) findViewById(R.id.chatToolbarBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Log.d("Chat " + chatKey, "Send from:" + sender.getName().getValue() + ", rec by:" + receiver.getName().getValue());
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextInputEditText input = (TextInputEditText) findViewById(R.id.input);
                if (!input.getText().toString().isEmpty()) {

                    //Michelangelo: Qui setto il messaggio
                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = firebaseDatabase.getReference("chats").child(chatKey);
                    //La riga successiva setta il messaggio nel server.
                    //aggiungi cose al costruttore per inserire nuovi valori (come il token che ti servirà)
                    //la classe a cui dovrai aggiungere le cose è ChatMessage, dovrai solo aggiungere i getter and setters
                    databaseReference.push().setValue(new ChatMessage(sender.getKey(), receiver.getKey(), input.getText().toString()));

                    //Set the last message
                    DatabaseReference databaseReference1 = firebaseDatabase.getReference("users").child(sender.getKey()).child("chats").child(chatKey);
                    databaseReference1.child("lastMessage").setValue(input.getText().toString());
                    databaseReference1.child("lastTimestamp").setValue(new Date().getTime());
                    databaseReference1.setPriority(-1 * new Date().getTime());

                    DatabaseReference databaseReference2 = firebaseDatabase.getReference("users").child(receiver.getKey()).child("chats").child(chatKey);
                    databaseReference2.child("lastMessage").setValue(input.getText().toString());
                    databaseReference2.child("lastTimestamp").setValue(new Date().getTime());
                    databaseReference2.setPriority(-1 * new Date().getTime());
                    input.setText("");

                }

            }
        });

        firebaseDatabaseAccess = FirebaseDatabase.getInstance();
        databaseReferenceAccess = firebaseDatabaseAccess.getReference("users").child(sender.getKey()).child("status");

        isReadUpdate();
        displayChatMessage();


    }

    private String getDate(long timestamp) {
        return DateFormat.format("dd/MM/yyyy", timestamp).toString();
    }

    private void displayChatMessage() {
        final ListView listOfMessage = (ListView) findViewById(R.id.list_of_message);

        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class, R.layout.item_message, FirebaseDatabase.getInstance().getReference("chats").child(chatKey).orderByPriority()) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                //Get references to the views of list_item.xml
                TextView messageText, messageTime;
                ImageView read;
                Drawable d = null;
                ConstraintLayout clSend = (ConstraintLayout) v.findViewById(R.id.send_container);
                ConstraintLayout clRec = (ConstraintLayout) v.findViewById(R.id.received_container);

                /*TextView tvDate = (TextView) v.findViewById(R.id.date);
                if(!lastDate.equals(DateFormat.format("dd/MM/yyyy", model.getTime()).toString())){
                    lastDate = DateFormat.format("dd/MM/yyyy", new Date().getTime()).toString();
                    tvDate.setVisibility(View.VISIBLE);
                    tvDate.setText(lastDate);
                }
                else{
                    tvDate.setVisibility(View.GONE);
                }*/
                TextView tvDate = (TextView) v.findViewById(R.id.date);
                if (position == 0) {
                    tvDate.setVisibility(View.VISIBLE);
                    tvDate.setText(getDate(adapter.getItem(0).getTime()));
                } else if (!getDate(adapter.getItem(position).getTime()).equals(getDate(adapter.getItem(position - 1).getTime()))) {
                    tvDate.setVisibility(View.VISIBLE);
                    tvDate.setText(getDate(adapter.getItem(position).getTime()));
                } else {
                    tvDate.setVisibility(View.GONE);
                }

                if (sender.getKey().equals(model.getSender())) {
                    //Case sender
                    // read.setVisibility(View.VISIBLE);
                    messageText = (TextView) v.findViewById(R.id.text_message_body_send);
                    messageTime = (TextView) v.findViewById(R.id.text_message_time_send);
                    read = (ImageView) v.findViewById(R.id.message_read);
                    clRec.setVisibility(View.GONE);
                    clSend.setVisibility(View.VISIBLE);

                    if (model.isStatus_read()) {
                        d = getDrawable(R.drawable.check_double);
                    } else {
                        d = getDrawable(R.drawable.ic_check_blue_24dp);
                    }
                    d.setTint(getColor(R.color.colorPrimary));
                    d.setTintMode(PorterDuff.Mode.SRC_IN);
                    read.setImageDrawable(d);
                } else {
                    messageText = (TextView) v.findViewById(R.id.text_message_body_rec);
                    messageTime = (TextView) v.findViewById(R.id.text_message_time_rec);
                    clSend.setVisibility(View.GONE);
                    clRec.setVisibility(View.VISIBLE);
                }

                messageText.setText(model.getMessage());
                messageTime.setText(DateFormat.format("HH:mm", model.getTime()));
            }
        };
        listOfMessage.setAdapter(adapter);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("chats").child(chatKey);
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("change:", "captured");
                if (isRunning) {
                    Log.d("change:", "enter on isrunning");
                    isReadUpdate();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void setNotification(ChatMessage cm) {


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ChatPage.this)
                        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                        .setContentTitle(cm.getSender())
                        .setContentText(cm.getMessage());
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, ShowMessageThread.class), PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, mBuilder.build());
        Log.d("Notification:", "Set not");

        /*int id = 0;
        if((id = searchUserNotifyId(cm.getMessageUser())) < 0){
            id = userNotify.size();
            userNotify.add(cm.getMessageUser());
            userNotify.add(cm.getMessageUser());
        }
        notificationManager.notify(id, mBuilder.build());*/
    }

   /* private void destroyNotification(){
        notificationManager.cancelAll();
        userNotify.clear();
    }

    private int searchUserNotifyId(String searched){
        int i;
        for(i=0; i<userNotify.size(); i++){
            if(userNotify.get(i).toLowerCase().equals(searched.toLowerCase())){
                return i;
            }
        }
        return -1;
    }*/

    private void isReadUpdate() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("chats").child(chatKey);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot message : dataSnapshot.getChildren()) {
                    ChatMessage cm = message.getValue(ChatMessage.class);
                    if (!cm.isStatus_read() && !cm.getSender().equals(sender.getKey())) {
                        //update status read
                        Log.d("UP", "Update the status for " + message.getKey());
                        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                        DatabaseReference databaseReference = firebaseDatabase.getReference("chats").child(chatKey).child(message.getKey());
                        cm.setStatus_read(true);
                        databaseReference.setValue(cm);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
        String time = new Date().getTime() + "";
        databaseReferenceAccess.setValue("online");
        databaseReferenceAccess.onDisconnect().setValue(time);
        MyNotificationManager notificationManager = MyNotificationManager.getInstance(this);
        notificationManager.clearNotification();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        String time = new Date().getTime()+"";
        isRunning = false;
        backPressed = true;
        if(!getIntent().getBooleanExtra("fromShowMessageThread", false)){
            databaseReferenceAccess.setValue(time);
        }
        databaseReferenceAccess.onDisconnect().setValue(time);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        String time = new Date().getTime()+"";
        isRunning = false;
        if(!backPressed){
            databaseReferenceAccess.setValue(time);
        }
        databaseReferenceAccess.onDisconnect().setValue(time);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
        String time = new Date().getTime() + "";
        if (!backPressed) {
            databaseReferenceAccess.setValue(time);
        }
        databaseReferenceAccess.onDisconnect().setValue(time);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isRunning", isRunning);
        outState.putBoolean("backPressed", backPressed);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isRunning = savedInstanceState.getBoolean("isRunning");
        backPressed = savedInstanceState.getBoolean("backPressed");
    }


    private void setStatus(){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(receiver.getKey()).child("status");
        tvStatus = (TextView) findViewById(R.id.status);
        tvStatus.setVisibility(View.GONE);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    //Here the status exist
                    String status = dataSnapshot.getValue(String.class);
                    if(status.toLowerCase().equals("online")){
                        tvStatus.setText("Online");
                    }
                    else{
                        if(DateFormat.format("dd:MM:yyyy", new Long(status)  ).equals(DateFormat.format("dd:MM:yyyy", new Date().getTime() )) ){
                            tvStatus.setText( getString(R.string.last_seen) + " " + DateFormat.format("HH:mm", new Long(status)));
                        }
                        else{
                            tvStatus.setText(getString(R.string.last_seen) + " " + DateFormat.format("HH:mm", new Long(status) )  + " " + getString(R.string.of) + " " + DateFormat.format("dd/MM/yyyy", new Date().getTime()) );
                        }


                    }
                    tvStatus.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
