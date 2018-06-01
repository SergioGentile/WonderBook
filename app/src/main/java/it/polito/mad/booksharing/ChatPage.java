package it.polito.mad.booksharing;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatPage extends AppCompatActivity {

    private User sender, receiver;
    static boolean isRunning;
    private String chatKey;
    private FirebaseListAdapter<ChatMessage> adapter;
    private FloatingActionButton fab;
    private CircleImageView profileImage;
    private TextView tvName, tvStatus;
    private ImageButton backButton;
    private ListView listOfMessage;
    private Toolbar toolbar;
    private TextInputEditText input;

    private ArrayList<String> messagesRead = new ArrayList<>();
    private FirebaseDatabase firebaseDatabaseAccess;
    private DatabaseReference databaseReferenceAccess;
    private boolean backPressed;
    private List<String> keysMessageSelected;
    private MyNotificationManager notificationManager;
    List<String> messageToNot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        notificationManager = MyNotificationManager.getInstance(this);
        setContentView(R.layout.activity_chat_page);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        messageToNot = new ArrayList<>();

        isRunning = true;
        keysMessageSelected = new ArrayList<>();

        toolbar = findViewById(R.id.chat_toolbar);

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
        edit.apply();

        setStatus();

        tvName = findViewById(R.id.toolbarName);
        tvName.setText(receiver.getName().getValue() + " " + receiver.getSurname().getValue());

        profileImage = findViewById(R.id.toolbarPhoto);
        Picasso.with(ChatPage.this).load(receiver.getUser_image_url()).into(profileImage);

        backButton = findViewById(R.id.chatToolbarBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performBack();
            }
        });

        fab = findViewById(R.id.fab);
        input = findViewById(R.id.input);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!input.getText().toString().isEmpty()) {

                    messageToNot.clear();

                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = firebaseDatabase.getReference("chats").child(chatKey).push();
                    String key = databaseReference.getKey();
                    databaseReference.setValue(new ChatMessage(sender.getKey(), receiver.getKey(), input.getText().toString(), key));

                    //Set the last message
                    DatabaseReference databaseReference1 = firebaseDatabase.getReference("users").child(sender.getKey()).child("chats").child(chatKey);
                    databaseReference1.child("lastMessage").setValue(input.getText().toString());
                    databaseReference1.child("lastTimestamp").setValue(-1 * new Date().getTime());

                    DatabaseReference databaseReference2 = firebaseDatabase.getReference("users").child(receiver.getKey()).child("chats").child(chatKey);
                    databaseReference2.child("lastMessage").setValue(input.getText().toString());
                    databaseReference2.child("lastTimestamp").setValue(-1 * new Date().getTime());
                    input.setText("");
                }

            }
        });

        firebaseDatabaseAccess = FirebaseDatabase.getInstance();
        databaseReferenceAccess = firebaseDatabaseAccess.getReference("users").child(sender.getKey()).child("status");

        notificationManager.clearNotificationUser(receiver.getKey());

        displayChatMessage();
    }

    private String getDate(long timestamp) {
        return DateFormat.format("dd/MM/yyyy", timestamp).toString();
    }

    private void displayChatMessage() {
        listOfMessage = findViewById(R.id.list_of_message);

        messageToNot.clear();
        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class, R.layout.item_message, FirebaseDatabase.getInstance().getReference("chats").child(chatKey).orderByPriority()) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                //Get references to the views of list_item.xml
                TextView messageText, messageTime;
                ImageView read;
                Drawable d = null;
                LinearLayout clSend = v.findViewById(R.id.send_container);
                LinearLayout clRec = v.findViewById(R.id.received_container);

                TextView tvDate = v.findViewById(R.id.date);
                if (dateToPut(position)) {
                    tvDate.setVisibility(View.VISIBLE);
                    tvDate.setText(getDate(adapter.getItem(position).getTime()));
                } else {
                    tvDate.setVisibility(View.GONE);
                }

                if (model.getDeleteFor().contains(sender.getKey())) {
                    clRec.setVisibility(View.GONE);
                    clSend.setVisibility(View.GONE);
                    return;
                }

                if (sender.getKey().equals(model.getSender())) {
                    //Case sender
                    messageText = v.findViewById(R.id.text_message_body_send);
                    messageTime = v.findViewById(R.id.text_message_time_send);
                    read = v.findViewById(R.id.message_read);
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
                    messageText = v.findViewById(R.id.text_message_body_rec);
                    messageTime = v.findViewById(R.id.text_message_time_rec);
                    clSend.setVisibility(View.GONE);
                    clRec.setVisibility(View.VISIBLE);
                }

                messageText.setText(model.getMessage());
                messageTime.setText(DateFormat.format("HH:mm", model.getTime()));

                ImageView messageRecNot = (ImageView) v.findViewById(R.id.message_rec_notification);
                if (!model.isStatus_read() && model.getReceiver().equals(sender.getKey()) && !messagesRead.contains(model.getKey())) {
                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = firebaseDatabase.getReference("chats").child(chatKey).child(model.getKey()).child("status_read");
                    databaseReference.setValue(true);
                    model.setStatus_read(true);
                    messageRecNot.setVisibility(View.VISIBLE);
                    messageToNot.add(model.getKey());
                }
                if (!messageToNot.contains(model.getKey())) {
                    messageRecNot.setVisibility(View.GONE);
                } else {
                    messageRecNot.setVisibility(View.VISIBLE);
                }

            }
        };

        listOfMessage.setAdapter(adapter);

        listOfMessage.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL); //Scelgo il modo
        listOfMessage.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {


                final int checkedCount = listOfMessage.getCheckedItemCount();
                if (checkedCount == 1) {
                    mode.setTitle(checkedCount + " " + getString(R.string.message_to_delete));
                } else {
                    mode.setTitle(checkedCount + " " + getString(R.string.messages_to_delete));
                }

                if (checked == true) {
                    keysMessageSelected.add(adapter.getItem(position).getKey());
                } else {
                    keysMessageSelected.remove(adapter.getItem(position).getKey());
                }


            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.delete_message_menu_option, menu);
                toolbar.setVisibility(View.INVISIBLE);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                listOfMessage.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        deleteMessages(keysMessageSelected);
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                toolbar.setVisibility(View.VISIBLE);
                keysMessageSelected.clear();
                listOfMessage.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            }
        });
    }

    private boolean dateToPut(int position) {
        if (adapter.getItem(position).getDeleteFor().contains(sender.getKey())) {
            return false;
        }
        for (int i = position - 1; i >= 0; i--) {
            if (!adapter.getItem(i).getDeleteFor().contains(sender.getKey()) && getDate(adapter.getItem(i).getTime()).equals(getDate(adapter.getItem(position).getTime()))) {
                return false;
            }
        }

        return true;
    }


    private void deleteMessages(List<String> keysMessages) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("chats").child(chatKey);
        for (String keyMessage : keysMessages) {
            ChatMessage cmToDelete = takeMessageFromAdapter(keyMessage);
            if (cmToDelete != null) {
                cmToDelete.addUserDelete(sender.getKey());
                databaseReference.child(keyMessage).setValue(cmToDelete);
            }
        }
    }

    private ChatMessage takeMessageFromAdapter(String keyMessage) {
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).getKey() != null && adapter.getItem(i).getKey().equals(keyMessage)) {
                return adapter.getItem(i);
            }
        }
        return null;
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
        performBack();
    }

    private void performBack() {
        String time = new Date().getTime() + "";
        isRunning = false;
        backPressed = true;
        if (!getIntent().getBooleanExtra("fromShowMessageThread", false)) {
            databaseReferenceAccess.setValue(time);
        }
        databaseReferenceAccess.onDisconnect().setValue(time);

        //Get the last message
        String lastMessage = new String("");
        Long lastTime = new Long(-1);
        for (int i = adapter.getCount() - 1; i >= 0; i--) {
            System.out.println("Message " + adapter.getItem(i).getMessage() + " delete for " + adapter.getItem(i).getDeleteFor().size() + " people");
            if (!adapter.getItem(i).getDeleteFor().contains(sender.getKey())) {
                lastMessage = adapter.getItem(i).getMessage();
                lastTime = adapter.getItem(i).getTime();
                break;
            }
        }
        //Set the last message
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(sender.getKey()).child("chats").child(chatKey);
        databaseReference.child("lastMessage").setValue(lastMessage);
        databaseReference.child("lastTimestamp").setValue(-1 * lastTime);

        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        String time = new Date().getTime() + "";
        isRunning = false;
        if (!backPressed) {
            databaseReferenceAccess.setValue(time);
        }
        databaseReferenceAccess.onDisconnect().setValue(time);

        //Get the last message
        String lastMessage = new String("");
        Long lastTime = new Long(-1);
        for (int i = adapter.getCount() - 1; i >= 0; i--) {
            System.out.println("Message " + adapter.getItem(i).getMessage() + " delete for " + adapter.getItem(i).getDeleteFor().size() + " people");
            if (!adapter.getItem(i).getDeleteFor().contains(sender.getKey())) {
                lastMessage = adapter.getItem(i).getMessage();
                lastTime = adapter.getItem(i).getTime();
                break;
            }
        }
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(sender.getKey()).child("chats").child(chatKey);
        databaseReference.child("lastMessage").setValue(lastMessage);
        databaseReference.child("lastTimestamp").setValue(-1 * lastTime);


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
        outState.putStringArrayList("messageReadList", messagesRead);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isRunning = savedInstanceState.getBoolean("isRunning");
        backPressed = savedInstanceState.getBoolean("backPressed");
        messagesRead = savedInstanceState.getStringArrayList("messageReadList");


    }


    private void setStatus() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(receiver.getKey()).child("status");
        tvStatus = findViewById(R.id.status);
        tvStatus.setVisibility(View.GONE);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //Here the status exist
                    String status = dataSnapshot.getValue(String.class);
                    if (status.toLowerCase().equals("online")) {
                        tvStatus.setText("Online");
                    } else {
                        if (DateFormat.format("dd:MM:yyyy", new Long(status)).equals(DateFormat.format("dd:MM:yyyy", new Date().getTime()))) {
                            tvStatus.setText(getString(R.string.last_seen) + " " + DateFormat.format("HH:mm", new Long(status)));
                        } else {
                            tvStatus.setText(getString(R.string.last_seen) + " " + DateFormat.format("HH:mm", new Long(status)) + " " + getString(R.string.of) + " " + DateFormat.format("dd/MM/yyyy", new Long(status)));
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
