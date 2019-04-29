package com.example.joelwasserman.androidbleconnectexample;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import me.anwarshahriar.calligrapher.Calligrapher;

public class MapsActivity_wifi extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 17f;

    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    public TextView name_text;
    public TextView add_text;
    ImageView btn;
    public String rssi="-100";
    public String lati;
    public String longi;
    public String d_name;
    CanvasView canvas;
    ImageView circle_img;
    public ImageView batImg;
    public TextView add_battery;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_wifi);

        Calligrapher calli = new Calligrapher(this);
        calli.setFont(this, "fonts/HELR45W.ttf",true);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapw);
        mapFragment.getMapAsync(this);

        Bundle extras = getIntent().getExtras();
        btn = (ImageView)findViewById(R.id.button4w1);
        circle_img = (ImageView)findViewById(R.id.circle);
        name_text = (TextView) findViewById(R.id.textView4w);
        add_text = (TextView) findViewById(R.id.textView2w);
        canvas = (CanvasView) findViewById(R.id.canvasViewwifi);
        add_battery = (TextView)findViewById(R.id.textView2_batw);
        batImg = (ImageView)findViewById(R.id.batimgw);
        if (extras != null) {
            d_name = extras.getString("Dev_name");
            lati = extras.getString("lati");
            longi  = extras.getString("longi");
            name_text.setText("Name: " + extras.getString("Dev_name"));
            add_text.setText("Address: " + extras.getString("Dev_add"));
            d_bat = extras.getString("Dev_bat");
            calculateBattery(Integer.parseInt(d_bat));
           // rssi = ""+extras.getString("Dev_rssi");

scale(-Integer.parseInt(rssi));
           /* canvas.drawCircle(-Integer.parseInt(rssi));
            canvas.invalidate();*/
        }
        runUdpServer();
        timer= new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //here you can write the code for send the message
                runUdpServer();

            }
        }, 10, 1000);
        getLocationPermission();
    }

    public void openDeviceNotFound(){
        Intent intent = new Intent(this, MapsActivity_device_not_found.class);
        intent.putExtra("lati",lati);
        intent.putExtra("longi",longi);
        intent.putExtra("dvname",d_name);
        intent.putExtra("dvadd",IP);
        intent.putExtra("dvbat","");

            intent.putExtra("status","not found");

        startActivity(intent);
    }
    float prev_scale=1f;
    public void scale(int rssi) {
        double d = (double)rssi/(double)100;
        float to_scale = (float)d;
        ScaleAnimation scaleAnimation = new ScaleAnimation(prev_scale, to_scale, prev_scale, to_scale, circle_img.getWidth() / 2.0f, circle_img.getHeight() / 2.0f);
        scaleAnimation.setDuration(1000);

        circle_img.startAnimation(scaleAnimation);
        scaleAnimation.setFillAfter(true);
        prev_scale = to_scale;
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                Log.d("ScaleActivity", "Scale started...");
            }

            public void onAnimationRepeat(Animation animation) {

            }

            public void onAnimationStart(Animation animation) {
                Log.d("ScaleActivity", "Scale ended...");
            }
        });
    }

    int d3=0;
    public String d_bat="50";
    private void calculateBattery(int bat) {

        double d = (double) 100 / (double) 4095;
        double d2 = d * (double) bat;
        d3 = (int) d2;
        d_bat = "" + d3;
        MapsActivity_wifi.this.runOnUiThread(new Runnable() {
            public void run() {
                if (d3 >= 90 && d3 <= 100) {
                    batImg.setImageResource(R.drawable.b100);
                } else if (d3 >= 75 && d3 < 90) {
                    batImg.setImageResource(R.drawable.d75);
                } else if (d3 >= 50 && d3 < 75) {
                    batImg.setImageResource(R.drawable.d75);
                } else {
                    batImg.setImageResource(R.drawable.d0);
                }
                add_battery.setText("" + d_bat + "%");
            }
        });
    }
    private static final int UDP_SERVER_PORT = 44444;
    private static final int MAX_UDP_DATAGRAM_LEN = 1500;
    private static String IP;
    public Timer timer=new Timer();
    private void runUdpServer() {
        new Thread(new Runnable() {
            public void run() {
                String lText;
                byte[] lMsg = new byte[MAX_UDP_DATAGRAM_LEN];
                DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);
                DatagramSocket ds = null;
                try {
                    ds = new DatagramSocket(UDP_SERVER_PORT);
                    //disable timeout for testing
                    //ds.setSoTimeout(100000);

                    ds.receive(dp);
                    String str = ""+dp.getAddress();
                    String str1 = new String(dp.getData(), 0, dp.getLength());
                    lText = str.substring(1,str.length());//new String(dp.getData());
                    Log.i("UDP packet received", ""+str1);
                    if(!str1.equalsIgnoreCase("")){
                        String[] arr = str1.split("\\|");
                        rssi = (arr[0]);
                        IP = lText;
                        System.out.println("UDP packet received rssi: "+rssi);
                        calculateBattery(Integer.parseInt(arr[2]));
                        MapsActivity_wifi.this.runOnUiThread(new Runnable() {
                            public void run() {
                                add_text.setText(IP);
                               // canvas.drawCircle(-Integer.parseInt(rssi));
                              //  canvas.invalidate();
                               // scale(new Random().nextInt((100-10)+1)+10);
                                scale(-Integer.parseInt(rssi));
                                if(-Integer.parseInt(rssi)>90){
                                    openDeviceNotFound();
                                }
                                // peripheralTextView.append("device read or wrote to\n");
                                ;
                            }
                        });

                       // deviceName = arr[1];
                    }



                    // data.setText(lText);
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (ds != null) {
                        ds.close();
                    }
                }
            }
        }).start();

    }
    private void sendDataToUdpServer() {
        new Thread(new Runnable() {
            public void run() {
                String lText;
                byte[] lMsg = new byte[MAX_UDP_DATAGRAM_LEN];

                DatagramSocket ds = null;
                try {
                    String msg = "A";
                    Log.i("UDP packet send ip", ""+IP);
                    InetAddress local = InetAddress.getByName(IP);
                    int msg_len = msg.length();
                    byte[] message = msg.getBytes();
                    ds = new DatagramSocket();
                    DatagramPacket dp = new DatagramPacket(message, msg_len,local,UDP_SERVER_PORT);
                    ds.send(dp);
                    //disable timeout for testing
                    //ds.setSoTimeout(100000);

                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (ds != null) {
                        ds.close();
                    }
                }
            }
        }).start();

    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;



        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

        }
    }
    public String saveToFile;
    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            saveToFile = d_name+"|"+currentLocation.getLatitude()+"|"+currentLocation.getLongitude();
                            writeToFile(saveToFile,getApplicationContext());
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM);
                           // drawCircle(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));


                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapsActivity_wifi.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }
    private void moveCamera(LatLng latLng, float zoom){

        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapw);

        mapFragment.getMapAsync(MapsActivity_wifi.this);
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    private void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            Toast.makeText(MapsActivity_wifi.this, "Device saved successfully", Toast.LENGTH_SHORT).show();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    private void drawCircle(LatLng point){

        // Instantiating CircleOptions to draw a circle around the marker
        CircleOptions circleOptions = new CircleOptions();

        // Specifying the center of the circle
        circleOptions.center(point);

        // Radius of the circle
        circleOptions.radius(-Double.parseDouble(rssi));

        // Border color of the circle
        circleOptions.strokeColor(Color.BLACK);

        // Fill color of the circle
        circleOptions.fillColor(0x30ff0000);

        // Border width of the circle
        circleOptions.strokeWidth(2);

        // Adding the circle to the GoogleMap
        mMap.addCircle(circleOptions);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        mLocationPermissionsGranted = false;
        Log.d(TAG, "onRequestPermissionsResult: called.");
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }
    public void onClickRingwifi(View view) {
        Toast.makeText(MapsActivity_wifi.this, "Ring", Toast.LENGTH_SHORT).show();
        sendDataToUdpServer();
          /*  Intent incomingMessageIntent = new Intent("incomingMessage_wifi");
            incomingMessageIntent.putExtra("Incoming_Message","ring");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(incomingMessageIntent);
*/

    }
    public void onClickChangeDevicewifi(View view){
        timer.cancel();
        Intent intent = new Intent(this, MainActivity.class);

        startActivity(intent);
    }
    public void onClickMapFoundOnWifi(View view){
        if(!lati.equalsIgnoreCase("")){
            Intent intent = new Intent(this, MapsActivity_device_not_found.class);
            intent.putExtra("lati",lati);
            intent.putExtra("longi",longi);
            intent.putExtra("dvname",d_name);
            intent.putExtra("dvadd",IP);
           // intent.putExtra("dvbat",d_bat);
            intent.putExtra("status","wifi");
            startActivity(intent);
        }

    }
}
