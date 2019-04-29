package com.example.joelwasserman.androidbleconnectexample;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joelwasserman.androidbleconnectexample.directionhelpers.FetchURL;
import com.example.joelwasserman.androidbleconnectexample.directionhelpers.TaskLoadedCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.DecimalFormat;

import me.anwarshahriar.calligrapher.Calligrapher;

public class MapsActivity_device_not_found extends FragmentActivity implements OnMapReadyCallback,TaskLoadedCallback {

    private static final String TAG = "MapActivity_device_not_found";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 17f;

    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    public String lati;
    public String longi;
    public String dvname;
    public String dvAdd;
    public String battery;
    public TextView name_text;
    public TextView add_text;
    public TextView add_battery;
    public ImageView btn_map;
    public ImageView btn_ring;
    public ImageView batImg;
    public int d3;
    private Polyline currentPolyline;
    private MarkerOptions place1, place2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_device_not_found);


        Calligrapher calli = new Calligrapher(this);
        calli.setFont(this, "fonts/HELR45W.ttf",true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map12);
        mapFragment.getMapAsync(this);
        Bundle extras = getIntent().getExtras();
        name_text = (TextView) findViewById(R.id.textView4dv);
        add_text = (TextView) findViewById(R.id.textView2dv);
        add_battery = (TextView)findViewById(R.id.textView2_batdv);
        btn_map = (ImageView)findViewById(R.id.buttonMapdv);
        btn_ring = (ImageView)findViewById(R.id.button4dv);
        batImg = (ImageView) findViewById(R.id.batimg1);
        if (extras != null) {
            if(extras.getString("status").equalsIgnoreCase("true")){
                lati = extras.getString("longi");
                longi = extras.getString("lati");
                dvname = extras.getString("dvname");
                dvAdd = extras.getString("dvadd");
                battery = extras.getString("dvbat");
                name_text.setText("Name: " + dvname);
                add_text.setText("Address: " + dvAdd);
                add_battery.setText(""+battery+"%");
                d3 = Integer.parseInt(battery);

                MapsActivity_device_not_found.this.runOnUiThread(new Runnable() {
                    public void run() {
                        if(d3>=90 && d3<=100){
                            batImg.setImageResource(R.drawable.b100);
                        }
                        else if(d3>=75 && d3<90){
                            batImg.setImageResource(R.drawable.d75);
                        }
                        else if(d3>=50 && d3<75){
                            batImg.setImageResource(R.drawable.d75);
                        }
                        else{
                            batImg.setImageResource(R.drawable.d0);
                        }

                    }
                });

                btn_ring.setAlpha(1.0f);
                btn_map.setAlpha(0.5f);
            }
            else if(extras.getString("status").equalsIgnoreCase("wifi")){
                lati = extras.getString("longi");
                longi = extras.getString("lati");
                dvname = extras.getString("dvname");
                dvAdd = extras.getString("dvadd");
              ///  battery = extras.getString("dvbat");
                name_text.setText("Name: " + dvname);
                add_text.setText("Address: " + dvAdd);
                add_battery.setText("");
                batImg.setVisibility(View.INVISIBLE);
                btn_ring.setAlpha(1.0f);
                btn_map.setAlpha(0.5f);
            }
            else{
                lati = extras.getString("longi");
                longi = extras.getString("lati");
                dvname = extras.getString("dvname");
                dvAdd = extras.getString("dvadd");

                name_text.setText("Name: " + dvname);
                add_text.setText("Status: Device Not Found" );
                add_battery.setText("");
                batImg.setVisibility(View.INVISIBLE);
                btn_ring.setAlpha(0.5f);
                btn_map.setAlpha(0.5f);
                pushNotification();
            }

        }
        btn_ring.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               if(btn_ring.getAlpha() != 0.5f)
                onClickRing_Map();
            }
        });
        getLocationPermission();
    }

public void pushNotification(){
    String tittle="Smart Eye Glass";
    String subject="Status";
    String body="Device Not Found";
     Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    NotificationManager notif=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    Notification notify=new Notification.Builder
            (getApplicationContext()).setContentTitle(tittle).setContentText(body).
            setContentTitle(subject).setSound(sound).setSmallIcon(R.drawable.eyeglass_icon).build();

    notify.flags |= Notification.FLAG_AUTO_CANCEL;
    notif.notify(0, notify);

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

        // Add a marker in Sydney and move the camera
      /*  LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        if (mLocationPermissionsGranted) {


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
          //  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
          //  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(lati), Double.parseDouble(longi)),
                 //   DEFAULT_ZOOM));
            getDeviceLocation();

        }


    }
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
                            CalculationByDistance(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),
                                    new LatLng(Double.parseDouble(lati), Double.parseDouble(longi)));


                        }else{
                            moveCamera(new LatLng(Double.parseDouble(lati), Double.parseDouble(longi)),
                                    DEFAULT_ZOOM);
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapsActivity_device_not_found.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }
public double radius_dev=0.0;
    public double kmInDec=0;
    public double meterInDec=0;
    private void moveCamera(LatLng latLng, float zoom){

        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        mMap.addMarker(new MarkerOptions().position(latLng).title("Device is in "+kmInDec+"km and "+meterInDec+" meter.")).showInfoWindow();

        place1 = new MarkerOptions().position(latLng).title("device location");
        place2 = new MarkerOptions().position(endp).title("Your location");
        // if(kmInDec !=0.0 && meterInDec != 0.0){
        //  mMap.addMarker(place1);
        mMap.addMarker(place2).showInfoWindow();
        new FetchURL(MapsActivity_device_not_found.this).execute(getUrl(place1.getPosition(), place2.getPosition(), "driving"), "driving");

    }
    LatLng endp;
    public void  CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
       // StartP = new LatLng(22.523300,88.455750);
        endp = StartP;
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
         kmInDec = Double.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
         meterInDec = Double.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);
        moveCamera(new LatLng(Double.parseDouble(lati), Double.parseDouble(longi)),
                DEFAULT_ZOOM);
         // }



       // return Radius * c;
    }
    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map12);

        mapFragment.getMapAsync(MapsActivity_device_not_found.this);
    }
    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key1);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
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

    public void onClickRing_Map() {
        Toast.makeText(MapsActivity_device_not_found.this, "Ring", Toast.LENGTH_SHORT).show();
        Intent incomingMessageIntent = new Intent("incomingMessage");
        incomingMessageIntent.putExtra("Incoming_Message","ring");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(incomingMessageIntent);


    }
    public void onClickChangeDevice_not_found(View view){
        Intent intent = new Intent(this, MainActivity.class);

        startActivity(intent);
    }
}
