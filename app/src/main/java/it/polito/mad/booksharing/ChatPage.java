package it.polito.mad.booksharing;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
    private ListView listOfMessage;
    private Toolbar toolbar;
    private TextInputEditText input;

    private FirebaseDatabase firebaseDatabaseAccess;
    private DatabaseReference databaseReferenceAccess;
    private boolean backPressed;
    private List<String> keysMessageSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_page);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        isRunning = true;
        keysMessageSelected = new ArrayList<>();

        toolbar = (Toolbar) findViewById(R.id.chat_toolbar);

        sender = getIntent().getExtras().getParcelable("sender");
        receiver = getIntent().getExtras().getParcelable("receiver");
        chatKey = getIntent().getStringExtra("key_chat");

        setStatus();

        tvName = (TextView) findViewById(R.id.toolbarName);
        tvName.setText(receiver.getName().getValue() + " " + receiver.getSurname().getValue());

        profileImage = (CircleImageView) findViewById(R.id.toolbarPhoto);
        Picasso.with(ChatPage.this).load(receiver.getUser_image_url()).into(profileImage);

        backButton = (ImageButton) findViewById(R.id.chatToolbarBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performBack();
            }
        });

        //Log.d("Chat " + chatKey, "Send from:" + sender.getName().getValue() + ", rec by:" + receiver.getName().getValue());
        fab = (FloatingActionButton) findViewById(R.id.fab);
        input = (TextInputEditText) findViewById(R.id.input);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!input.getText().toString().isEmpty()) {

                    //Michelangelo: Qui setto il messaggio
                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = firebaseDatabase.getReference("chats").child(chatKey).push();
                    //La riga successiva setta il messaggio nel server.
                    //aggiungi cose al costruttore per inserire nuovi valori (come il token che ti servirà)
                    //la classe a cui dovrai aggiungere le cose è ChatMessage, dovrai solo aggiungere i getter and setters
                    String key = databaseReference.getKey();
                    databaseReference.setValue(new ChatMessage(sender.getKey(), receiver.getKey(), input.getText().toString(), key));

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

        displayChatMessage();
        isReadUpdate();


    }

    private String getDate(long timestamp) {
        return DateFormat.format("dd/MM/yyyy", timestamp).toString();
    }

    private void displayChatMessage() {
        listOfMessage = (ListView) findViewById(R.id.list_of_message);

        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class, R.layout.item_message, FirebaseDatabase.getInstance().getReference("chats").child(chatKey).orderByPriority()) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                //Get references to the views of list_item.xml
                TextView messageText, messageTime;
                ImageView read;
                Drawable d = null;
                LinearLayout clSend = (LinearLayout) v.findViewById(R.id.send_container);
                LinearLayout clRec = (LinearLayout) v.findViewById(R.id.received_container);

                TextView tvDate = (TextView) v.findViewById(R.id.date);
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

        listOfMessage.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL); //Scelgo il modo
        listOfMessage.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

                if (adapter.getItem(position).getKey() != null && adapter.getItem(position).getMessage() != null) {
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
                    //Cosa succede se schiaccio l'id
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

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("chats").child(chatKey);
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (isRunning) {
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

    private boolean dateToPut(int position){
        //Se non è visibile, sicuramente non visualizzerò la data
        if(adapter.getItem(position).getDeleteFor().contains(sender.getKey())){
            return false;
        }
        int counter=0;
        //Conto quanti visibili ci sono con la stessa data
        for(int i=position-1; i>=0; i--){
            if(!adapter.getItem(i).getDeleteFor().contains(sender.getKey()) && getDate(adapter.getItem(i).getTime()).equals(getDate(adapter.getItem(position).getTime()))){
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

        //Select the right last message on the thread
        /*DatabaseReference databaseReference1 = firebaseDatabase.getReference("users").child(sender.getKey()).child("chats").child(chatKey);
        databaseReference1.child("lastMessage").setValue(adapter.getCount()-1);
        databaseReference1.child("lastTimestamp").setValue(new Date().getTime());
        databaseReference1.setPriority(-1 * new Date().getTime());

        DatabaseReference databaseReference2 = firebaseDatabase.getReference("users").child(receiver.getKey()).child("chats").child(chatKey);
        databaseReference2.child("lastMessage").setValue(input.getText().toString());
        databaseReference2.child("lastTimestamp").setValue(new Date().getTime());
        databaseReference2.setPriority(-1 * new Date().getTime());
        input.setText("");*/
    }

    private ChatMessage takeMessageFromAdapter(String keyMessage) {
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).getKey() != null && adapter.getItem(i).getKey().equals(keyMessage)) {
                return adapter.getItem(i);
            }
        }
        return null;
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
                        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                        DatabaseReference databaseReference = firebaseDatabase.getReference("chats").child(chatKey).child(message.getKey()).child("status_read");
                        databaseReference.setValue(true);
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
        isReadUpdate();
        String time = new Date().getTime() + "";
        databaseReferenceAccess.setValue("online");
        databaseReferenceAccess.onDisconnect().setValue(time);
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
        Long lastTime = new Long(0);
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
        databaseReference.child("lastTimestamp").setValue(lastTime);
        databaseReference.setPriority(-1 * lastTime);

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


    private void setStatus() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(receiver.getKey()).child("status");
        tvStatus = (TextView) findViewById(R.id.status);
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
