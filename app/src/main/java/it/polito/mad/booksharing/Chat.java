package it.polito.mad.booksharing;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Chat extends AppCompatActivity {
    private ImageButton btnBack;
    private FloatingActionButton fab;
    private TextInputEditText messageText;
    private List<String> Messages;
    private ListView lv ;
    private LayoutInflater inflater;
    private Context c;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Messages = new ArrayList<>();
        Messages.add("ciao");
        Messages.add("Come stai?hyhyhyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy");
    Messages.add("nief");
    Messages.add("weufhofihew");
        btnBack = (ImageButton) findViewById(R.id.chatToolbarBack);
        messageText = (TextInputEditText) findViewById(R.id.edittext_chatbox);
        fab = (FloatingActionButton) findViewById(R.id.fabAdd);
        lv = (ListView) findViewById(R.id.reyclerview_message_list);

        c = getApplicationContext();
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageText.setText("");
                getMessages();

            }
        });

        getMessages();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState); // the UI component values are saved here.
        outState.putString("text", messageText.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        messageText.setText(inState.getString("text"));
    }


    protected void getMessages(){
        lv.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return Messages.size();
            }

            @Override
            public Object getItem(int position) {
                return Messages.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (inflater== null)
                {
                    inflater=(LayoutInflater)c.getSystemService(getApplicationContext().LAYOUT_INFLATER_SERVICE);
                }
              /*  if (convertView == null) {
                    if(position%2==0) {
                        convertView = getLayoutInflater().inflate(R.layout.item_message_sent, parent, false);
                    }else{

                        convertView = getLayoutInflater().inflate(R.layout.item_message_received, parent, false);
                    }
                }
*/

                if(position%2==0) {
                    convertView = getLayoutInflater().inflate(R.layout.item_message_sent, parent, false);
                }else{

                    convertView = getLayoutInflater().inflate(R.layout.item_message_received, parent, false);
                }

                /*

                if(MessageSent){
                convertView = getLayoutInflater().inflate(R.layout.item_message_sent, parent, false);
                }else if(MessageRecv){
                convertView = getLayoutInflater().inflate(R.layout.item_message_received, parent, false);
                }
                 */

                TextView message = (TextView) convertView.findViewById(R.id.text_message_body);
                TextView time = (TextView) convertView.findViewById(R.id.text_message_time);


                message.setText(Messages.get(position));
                return convertView;
            }

        });
    }





}
