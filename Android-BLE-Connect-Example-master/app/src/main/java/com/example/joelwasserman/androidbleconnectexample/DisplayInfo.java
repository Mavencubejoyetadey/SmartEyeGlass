package com.example.joelwasserman.androidbleconnectexample;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DisplayInfo extends AppCompatActivity {

    public TextView name_text;
    public TextView add_text;
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_info);
        Bundle extras = getIntent().getExtras();
        btn = (Button)findViewById(R.id.button2);
        btn.setText("Ring");
         name_text = (TextView) findViewById(R.id.name_dev);
         add_text = (TextView) findViewById(R.id.add_dev);
         if (extras != null) {
             name_text.setText("" + extras.getString("Dev_name"));
           add_text.setText("" + extras.getString("Dev_add"));
        }
    }

   /* public void onClickRing(View view) {
        if(btn.getText() == "Ring"){
            btn.setText("Off");
            Intent incomingMessageIntent = new Intent("incomingMessage");
            incomingMessageIntent.putExtra("Incoming_Message","ring");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(incomingMessageIntent);

        }
        else{
            btn.setText("Ring");
            Intent incomingMessageIntent = new Intent("incomingMessage");
            incomingMessageIntent.putExtra("Incoming_Message","off");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(incomingMessageIntent);

        }

    }*/

}
