package it.polito.mad.booksharing;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

public class AddNewRequest extends AppCompatActivity {

    private User userLogged, userOwner;
    private TextView tvTitle, tvUser, tvLocation;
    private TextInputEditText edtMessage;
    private Button send;
    private Book book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_request);

        userLogged = getIntent().getExtras().getParcelable("userLogged");
        userOwner = getIntent().getExtras().getParcelable("userOwner");
        book = getIntent().getExtras().getParcelable("book");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        tvTitle = (TextView) findViewById(R.id.title);
        tvUser = (TextView) findViewById(R.id.user);
        tvLocation = (TextView) findViewById(R.id.location);
        edtMessage = (TextInputEditText) findViewById(R.id.inputMessage);
        send = (Button) findViewById(R.id.buttonSend);

        tvTitle.setText(book.getTitle());
        tvUser.setText(userOwner.getName().getValue() + " " + userOwner.getSurname().getValue());
        tvLocation.setText(book.getStreet() + ", " + book.getCity());
        edtMessage.setText(getString(R.string.message_request_book).replace("*user_name*", userOwner.getName().getValue()).replace("*book_name*", "\"" + book.getTitle() + "\""));

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!edtMessage.getText().toString().isEmpty()) {
                    final User sender = userLogged;
                    final User receiver = userOwner;
                    //User1: the owner of the phone (sender)
                    //User2: the owner of the book (receiver)
                    final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    final DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(sender.getKey()).child("chats");
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String keyChat = null;
                            if (dataSnapshot.exists()) {
                                //Chat list already exist
                                //Find previous chat between peer
                                boolean found = false;
                                for (DataSnapshot dataPeer : dataSnapshot.getChildren()) {
                                    Peer peer = dataPeer.getValue(Peer.class);
                                    if (peer.getReceiverInformation().getKey().equals(receiver.getKey())) {
                                        //Chat already exist
                                        found = true;
                                        keyChat = dataPeer.getKey();
                                    }
                                }
                                if (!found) {
                                    keyChat = createInstanceOfChat(sender, receiver);
                                }
                            } else {
                                keyChat = createInstanceOfChat(sender, receiver);
                            }

                            //Here i can insert the new message
                            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                            DatabaseReference databaseReference = firebaseDatabase.getReference("chats").child(keyChat).push();
                            String key = databaseReference.getKey();
                            databaseReference.setValue(new ChatMessage(sender.getKey(), receiver.getKey(), edtMessage.getText().toString(), key));

                            //Set the last message
                            DatabaseReference databaseReference1 = firebaseDatabase.getReference("users").child(sender.getKey()).child("chats").child(keyChat);
                            databaseReference1.child("lastMessage").setValue(edtMessage.getText().toString());
                            databaseReference1.child("lastTimestamp").setValue(-1 * new Date().getTime());

                            DatabaseReference databaseReference2 = firebaseDatabase.getReference("users").child(receiver.getKey()).child("chats").child(keyChat);
                            databaseReference2.child("lastMessage").setValue(edtMessage.getText().toString());
                            databaseReference2.child("lastTimestamp").setValue(-1 * new Date().getTime());

                            //add new request
                            FirebaseDatabase firebaseDatabaseLogged = FirebaseDatabase.getInstance();
                            DatabaseReference databaseReferenceLogged = firebaseDatabaseLogged.getReference("users").child(userLogged.getKey()).child("requests").child("outcoming").push();
                            String keyRequest = databaseReferenceLogged.getKey();
                            Request request = new Request(userOwner, userLogged, book, keyRequest);
                            databaseReferenceLogged.setValue(request);

                            FirebaseDatabase firebaseDatabaseOwner = FirebaseDatabase.getInstance();
                            DatabaseReference databaseReferenceOwner = firebaseDatabaseOwner.getReference("users").child(userOwner.getKey()).child("requests").child("incoming").child(keyRequest);
                            databaseReferenceOwner.setValue(request);

                            finish();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }
        });

    }

    private String createInstanceOfChat(User sender, User receiver) {
        //Create chat list
        //For the user1
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(sender.getKey()).child("chats");
        DatabaseReference instanceReference1 = databaseReference.push();
        String key = instanceReference1.getKey();
        instanceReference1.setValue(new Peer(receiver, key));

        //For the receiver: i put the sender
        DatabaseReference instanceReference2 = firebaseDatabase.getReference("users").child(receiver.getKey()).child("chats").child(key);
        instanceReference2.setValue(new Peer(sender, key));

        return key;
    }
}
