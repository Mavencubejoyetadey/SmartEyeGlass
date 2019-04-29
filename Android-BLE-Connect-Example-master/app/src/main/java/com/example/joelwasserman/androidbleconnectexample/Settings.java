package com.example.joelwasserman.androidbleconnectexample;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import me.anwarshahriar.calligrapher.Calligrapher;

public class Settings extends AppCompatActivity {
    EditText name_w;
    EditText pass;
    EditText duration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Calligrapher calli = new Calligrapher(this);
        calli.setFont(this, "fonts/HELR45W.ttf",true);
        name_w = (EditText) findViewById(R.id.editText2);
        pass = (EditText) findViewById(R.id.editText3);
        duration = (EditText) findViewById(R.id.editText4);
    }
    public void onClickSave(View view) {
        Intent incomingMessageIntent = new Intent("incomingMessage_settings");
        incomingMessageIntent.putExtra("wifiname",name_w.getText());
        incomingMessageIntent.putExtra("password",pass.getText());
        incomingMessageIntent.putExtra("duration",duration.getText());
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(incomingMessageIntent);
        Toast.makeText(getApplicationContext(),"Saved Successfully",Toast.LENGTH_LONG).show();
    }
}
