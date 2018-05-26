package it.polito.mad.booksharing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ActivityDispatcher extends AppCompatActivity {

    private final int CHAT_PAGE = 0, MESSAGE_THREAD = 1, SHOW_MOVMENT = 2, MAIN_PAGE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int dispatcherCode = getIntent().getIntExtra("dispatcherCode", -1);

        if (dispatcherCode == CHAT_PAGE) {
            //start ChatPage
            User owner = getIntent().getParcelableExtra("sender");
            User receiver = getIntent().getParcelableExtra("receiver");
            String chat_key = getIntent().getStringExtra("key_chat");

            Intent intent = new Intent(this, ChatPage.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("sender", owner);
            bundle.putParcelable("receiver", receiver);
            bundle.putString("key_chat", chat_key);
            intent.putExtras(bundle);

            startActivity(intent);
            finish();
        }
        else if(dispatcherCode == MESSAGE_THREAD){
            //start showThread
            //start ChatPage
            User owner = getIntent().getParcelableExtra("user");

            Intent intent = new Intent(this, ShowMessageThread.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", owner);
            intent.putExtras(bundle);

            startActivity(intent);
            finish();
        }
        else if(dispatcherCode == MAIN_PAGE){
            User user = getIntent().getParcelableExtra("user");
            Intent intent = new Intent(this, MainPage.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", user);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
        else if(dispatcherCode == SHOW_MOVMENT){
            User user = getIntent().getParcelableExtra("user");
            Intent intent = new Intent(this, ShowMovment.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", user);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    }
}
