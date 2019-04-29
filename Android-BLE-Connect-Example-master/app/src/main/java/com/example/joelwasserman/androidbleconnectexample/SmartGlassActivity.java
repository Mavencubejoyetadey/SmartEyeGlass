package com.example.joelwasserman.androidbleconnectexample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SmartGlassActivity extends Activity {
    public static int SPLASH_TIME=4000;
    public String device_longi="";
    public String device_lati="";
    public String deviceFromFile="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_glass);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String str = readFromFile(getApplicationContext());
                if(!str.equalsIgnoreCase("")){
                    String[] arr = str.split("\\|");
                    if(arr.length>0){
                        deviceFromFile = arr[0];
                        device_longi = arr[1];
                        device_lati = arr[2];

                        openDeviceInfo();


                    }
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                    startActivity(intent);
                }
                finish();
            }
        },SPLASH_TIME
        );


    }
    private String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("config.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
                System.out.println("From file: "+ret);
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    public void openDeviceInfo(){
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("lati",device_lati);
        intent.putExtra("longi",device_longi);
        intent.putExtra("status","hold");
        intent.putExtra("Dev_name",deviceFromFile);

        startActivity(intent);
    }
}
