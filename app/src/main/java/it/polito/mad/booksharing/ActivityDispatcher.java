package it.polito.mad.booksharing;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ActivityDispatcher extends AppCompatActivity {

    private static  int dispatcherCode = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dispatcherCode = getIntent().getIntExtra("dispatcherCode",0);

        if(dispatcherCode == 0){
            //start ChatPage
            User owner = getIntent().getParcelableExtra("sender");
            User receiver = getIntent().getParcelableExtra("receiver");
            String chat_key = getIntent().getStringExtra("key_chat");

            Intent intent = new Intent(this, ChatPage.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("sender",owner);
            bundle.putParcelable("receiver",receiver);
            bundle.putString("key_chat", chat_key);
            intent.putExtras(bundle);

            startActivity(intent);
            finish();
        }
        else{
            //start showThread
            //start ChatPage
            User owner = getIntent().getParcelableExtra("user");

            Intent intent = new Intent(this, ShowMessageThread.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("user",owner);
            intent.putExtras(bundle);

            startActivity(intent);
            finish();
        }
    }
}
