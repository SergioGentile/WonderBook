package it.polito.mad.booksharing;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowMessageThread extends AppCompatActivity {

    private ListAdapter adapter;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_message_thread);

        user = getIntent().getExtras().getParcelable("user");

        showAllChat();

    }

    private void showAllChat(){
        final ListView listOfMessage = (ListView) findViewById(R.id.list_of_message_thread);

        adapter = new FirebaseListAdapter<Peer>(this, Peer.class, R.layout.adapter_message_thread, FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("chats").orderByPriority()) {
            @Override
            protected void populateView(View v, final Peer peer, int position) {
                //Get references to the views of list_item.xml

                CircleImageView profileImage;
                TextView name, lastMessage;



                profileImage = v.findViewById(R.id.profile);
                name = v.findViewById(R.id.user);
                lastMessage = v.findViewById(R.id.last_mess);
                final PeerInformation peerInformation = peer.getPeerInformationReceiver(user.getKey());
                Picasso.with(ShowMessageThread.this).load(peer.getPeerInformationReceiver(user.getKey()).getPathImage()).into(profileImage);
                name.setText(peerInformation.getName() + " " + peerInformation.getSurname());

                LinearLayout ll = (LinearLayout) v.findViewById(R.id.adapter_message_thread);
                if(peer.getLastMessage().isEmpty()){
                    ll.setVisibility(View.GONE);
                    profileImage.setVisibility(View.GONE);
                    name.setVisibility(View.GONE);
                    lastMessage.setVisibility(View.GONE);
                    v.setVisibility(View.GONE);
                    View line = (View) v.findViewById(R.id.line);
                    line.setVisibility(View.GONE);
                    return;
                }


                lastMessage.setText(peer.getLastMessage());

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Here i have a chat with key keyChat
                        Intent intent = new Intent(ShowMessageThread.this, ChatPage.class);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("sender", user);
                        bundle.putParcelable("receiver", new User(peerInformation.getName(), peerInformation.getSurname(), peerInformation.getPathImage(), peerInformation.getKey()));
                        intent.putExtra("key_chat", peer.getKeyChat());
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                });

                //DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(peer.getPeerInformationReceiver(user.getKey()).getKey()).child("chats");
            }
        };

        listOfMessage.setAdapter(adapter);
    }
}
