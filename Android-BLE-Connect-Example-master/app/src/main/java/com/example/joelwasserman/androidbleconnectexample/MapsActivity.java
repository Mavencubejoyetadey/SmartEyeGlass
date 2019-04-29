package com.example.joelwasserman.androidbleconnectexample;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import me.anwarshahriar.calligrapher.Calligrapher;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

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

    public String rssi;
    public String lati;
    public String longi;
    public String d_name;
    public String d_add;
    public String d_bat="50";
    public ImageView batImg;
    public TextView add_battery;
    public Button reconnect;
    ImageView btn;
    CanvasView canvas;
   public String wifi_name="joyeta_wifi";
    public String wifi_pass="12345678";
    public String ring_duration="1";

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    ArrayList<BluetoothDevice> devicesDiscovered = new ArrayList<BluetoothDevice>();
    BluetoothGatt bluetoothGatt;

    private BluetoothGattCharacteristic tx;
    private BluetoothGattCharacteristic rx;
    public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");

    public DeviceListAdapter adapter;
    public DeviceListAdapter adapter_connected;
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    private Handler mHandler = new Handler();
    private Handler mHandler_timer = new Handler();
    private static final long SCAN_PERIOD = 90*1000;
    private static final long SCAN_PERIOD1 = 50*1000;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    TextView peripheralTextView;
    Boolean btScanning = false;
    int deviceIndex = 0;
    public int after=60*1000;
    public int interval = 120*1000;
    public  Timer timer = new Timer();
    SupportMapFragment mapFragment;
Typeface tf;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Calligrapher calli = new Calligrapher(this);
        calli.setFont(this, "fonts/HELR45W.ttf",true);

      //  tf.createFromAsset(getAssets(), "fonts/HELR45W.ttf");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
               .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        batImg = (ImageView) findViewById(R.id.batimg);
        reconnect = (Button)findViewById(R.id.recon);
        reconnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanning();

            }
        });
        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        circle_img = (ImageView)findViewById(R.id.circle1);
        btScanner = btAdapter.getBluetoothLeScanner();
        peripheralTextView = (TextView) findViewById(R.id.txt);
        peripheralTextView.setMovementMethod(new ScrollingMovementMethod());
        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        getApplicationContext().registerReceiver(mBluetoothStateBroadcastReceiver,
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }

        //----------------------------------
        Bundle extras = getIntent().getExtras();
       // btn = (ImageView)findViewById(R.id.button4);

        name_text = (TextView) findViewById(R.id.textView4);
        add_text = (TextView) findViewById(R.id.textView2);
        add_battery = (TextView)findViewById(R.id.textView2_bat);
        canvas = (CanvasView) findViewById(R.id.canvasView);
       /* name_text.setTypeface(tf);
        add_text.setTypeface(tf);
        add_battery.setTypeface(tf);*/
        if (extras != null) {
            lati = extras.getString("lati");
            longi  = extras.getString("longi");
            d_name = extras.getString("Dev_name");

            if(extras.getString("status").equalsIgnoreCase("found")) {
                d_add = extras.getString("Dev_add");
                rssi = extras.getString("Dev_rssi");
                System.out.print("Rssi:"+rssi);
                name_text.setText("Name: " + extras.getString("Dev_name"));
                add_text.setText("Address: " + extras.getString("Dev_add"));
                String str = extras.getString("Dev_bat");
                if(!str.equalsIgnoreCase("NA")){
                    int bat = Integer.parseInt(extras.getString("Dev_bat"));
                    calculateBattery(bat);
                }
                else{
                    add_battery.setText("NA");
                    d_bat="";
                }



       //         btn.setEnabled(true);
                getLocationPermission();

scale(-Integer.parseInt(rssi));
              /*  canvas.drawCircle(-Integer.parseInt(rssi));
                canvas.setBackgroundColor(0xf5f5f5);

                canvas.invalidate();*/
            }
            else if(extras.getString("status").equalsIgnoreCase("new device")){
                d_add = extras.getString("Dev_add");
                rssi = extras.getString("Dev_rssi");
                name_text.setText("Name: " + extras.getString("Dev_name"));
                add_text.setText("Address: " + extras.getString("Dev_add"));
                add_battery.setText("Updating device information.");


/*
                canvas.drawCircle(-Integer.parseInt(rssi));
                canvas.setBackgroundColor(0xf5f5f5);

                canvas.invalidate();*/
                getLocationPermission();
              //  scale(-Integer.parseInt(rssi));
                startScanning();
            }
            else{
                rssi = "-100";//extras.getString("");
                //  btn.setEnabled(false);
                scale(-Integer.parseInt(rssi));
                startScanning();
                name_text.setText("Name: " + extras.getString("Dev_name"));
                add_text.setText("Status: Searching Device" );
                add_battery.setText("NA");

            }
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mreceiver1,new IntentFilter("incomingMessage"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mreceiver_settings,new IntentFilter("incomingMessage_settings"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mreceiver,new IntentFilter("BleDeviceBattery"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mreceiver,new IntentFilter("BleDeviceRSSI"));
    }
    private final BroadcastReceiver mreceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("Incoming_Message");

           // peripheralTextView.append("in local broadcast");
            if(action == "ring"){
                String message = "A";
               // peripheralTextView.append("Sentee: " + message);
                if (tx == null || message == null || message.isEmpty()) {
                    // Do nothing if there is no device or message to send.
                    return;
                }
                // Update TX characteristic value.  Note the setValue overload that takes a byte array must be used.
                tx.setValue(message.getBytes(Charset.forName("UTF-8")));
                if (bluetoothGatt.writeCharacteristic(tx)) {
                   // peripheralTextView.append("Sent: " + message);
                }
                else {
                   // peripheralTextView.append("Couldn't write TX characteristic!");
                }
            }
            else{

            }

        }
    };

    public void startScanning() {
        System.out.println("start scanning");



        btScanning = true;
        deviceIndex = 0;
        devicesDiscovered.clear();

        peripheralTextView.setText("");
        peripheralTextView.append("Started Scanning\n");

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScanning();
            }
        }, SCAN_PERIOD);
    }
    public void startTimeScan(){


        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //here you can write the code for send the message
                peripheralTextView.append("timer start\n");
                startScanning_timer();
            }
        }, after, interval);
    }
    public void startScanning_timer() {
        System.out.println("start scanning timer");

       // devicesDiscovered_timer.clear();
        peripheralTextView.setText("");
        peripheralTextView.append("Started Scanning timer\n");
        isDeviceOutOfRange=true;
        btScanner.startScan(leScanCallback_time);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("start scanning timer run");

            }
        });

        mHandler_timer.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScanning_timer();
            }
        }, SCAN_PERIOD1);
    }
    public boolean isDeviceOutOfRange = true;
    public boolean isOldDeviceConnected = false;
    public int count=0;
    private ScanCallback leScanCallback_time = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            System.out.println("start scanning timer onScanResult");
            isDeviceOutOfRange = true;
            if(result.getDevice().getName() != null ) {
                if(d_name.equalsIgnoreCase(result.getDevice().getName())){
                    peripheralTextView.append("connected device Rssi: "+result.getRssi() +"\n");
                    String rssi1 = ""+result.getRssi();
                    scale(-Integer.parseInt(rssi1));
                   /* canvas.drawCircle(-Integer.parseInt(rssi1));
                    canvas.setBackgroundColor(0xf5f5f5);

                    canvas.invalidate();*/
                    isDeviceOutOfRange = false;

                }

             //   devicesDiscovered_timer.add(result.getDevice());



            }

        }
    };
    public void stopScanning_timer() {
        System.out.println("stopping scanning timer");
        peripheralTextView.append("Stopped Scanningtimer\n");
        btScanning = false;
        if(isDeviceOutOfRange){
          //  timer.cancel();
            openWifiSetting();

        }
        btScanner.stopScan(leScanCallback_time);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
             /*   if(isDeviceOutOfRange){
                    timer.cancel();
                    openWifiSetting();

                }
                btScanner.stopScan(leScanCallback_time);*/
            }
        });
    }
    public  void sendSettings(){
        String message = "C"+wifi_name+":"+wifi_pass;
        if (tx == null || message == null || message.isEmpty()) {
            // Do nothing if there is no device or message to send.
            return;
        }
        // Update TX characteristic value.  Note the setValue overload that takes a byte array must be used.
        tx.setValue(message.getBytes(Charset.forName("UTF-8")));
        if (bluetoothGatt.writeCharacteristic(tx)) {
            peripheralTextView.append("Sent: " + message);
        }
        else {
            peripheralTextView.append("Couldn't write TX characteristic!");
        }
        sendRingDuration();
    }
    public  void sendRingDuration(){
        String message = "B"+ring_duration;
        if (tx == null || message == null || message.isEmpty()) {
            // Do nothing if there is no device or message to send.
            return;
        }
        // Update TX characteristic value.  Note the setValue overload that takes a byte array must be used.
        tx.setValue(message.getBytes(Charset.forName("UTF-8")));
        if (bluetoothGatt.writeCharacteristic(tx)) {
            peripheralTextView.append("Sent: Ring " + message);
        }
        else {
            peripheralTextView.append("Couldn't write TX characteristic!");
        }
    }
    public void openWifiSetting(){
        openDeviceInfo_wifi();
        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.Settings$WifiApSettingsActivity");
        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity( intent);
       // runUdpServer();


    }

    private static final int UDP_SERVER_PORT = 44444;
    private static final int MAX_UDP_DATAGRAM_LEN = 1500;
    private static String IP;
    public String deviceName = "Ble";
    public String deviceAddress = "11230989302";
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
                        rssi = arr[0];
                        deviceAddress =IP = lText;
                        deviceName = arr[1];
                    }


                    openDeviceInfo_wifi();
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
    public void openDeviceInfo_wifi(){
     //   timer.cancel();
        Intent intent = new Intent(this, MapsActivity_wifi.class);
        intent.putExtra("lati",lati);
        intent.putExtra("longi",longi);
        intent.putExtra("Dev_name",d_name);
        intent.putExtra("Dev_add",d_add);
        intent.putExtra("Dev_bat",d_bat);
       // intent.putExtra("Dev_rssi",""+rssi);
        startActivity(intent);

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


    public void stopScanning() {
        System.out.println("stopping scanning"+isDeviceOutOfRange);
        peripheralTextView.append("Stopped Scanning\n");
        btScanning = false;
        //  startScanningButton.setVisibility(View.VISIBLE);
        //  stopScanningButton.setVisibility(View.INVISIBLE);
        if(isDeviceOutOfRange &&count==3 ){
            openDeviceNotFound();
        }
        btScanner.stopScan(leScanCallback);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
             /*   if(isDeviceOutOfRange ){

                    openDeviceNotFound();
                }
                btScanner.stopScan(leScanCallback);*/
            }
        });
    }
    private ScanCallback leScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            System.out.println("le scan call back");
            if(result.getDevice().getName() != null ) {

                if (d_name.equalsIgnoreCase(result.getDevice().getName())) {
                    autoConnectToPairedDevice(result);

                    devicesDiscovered.add(result.getDevice());


                    peripheralTextView.append("rssi " + result.getRssi() + "\n");

                    deviceIndex++;
                    isDeviceOutOfRange = false;
                    stopScanning();


                }


                // auto scroll for text view

            }
        }
    };

    public void autoConnectToPairedDevice(ScanResult result){
        peripheralTextView.append("Trying to connect to old device: \n");
        d_name = result.getDevice().getName();
        d_add = result.getDevice().getAddress();
        add_text.setText("Address: "+d_add);
        add_battery.setText("100%");
        rssi = ""+result.getRssi();
        scale(-Integer.parseInt(rssi));

/*
        canvas.drawCircle(-Integer.parseInt(rssi));
        canvas.setBackgroundColor(0xf5f5f5);

        canvas.invalidate();*/
        bluetoothGatt = result.getDevice().connectGatt(this, false, btleGattCallback);
        count++;
    }
    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation

            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            MapsActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                   // peripheralTextView.append("device read or wrote to\n");
                    ;
                }
            });
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
          //  peripheralTextView.append(""+newState);
            switch (newState) {
                case 0:
                    MapsActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            if(count<3){
                                System.out.println("on connect count: "+count);
                                count++;
                                bluetoothGatt = devicesDiscovered.get(0).connectGatt(getApplicationContext(), false, btleGattCallback);
                            }
                            else if(count == 4){

                            }
                            else{
                                count = 0;
                                openWifiSetting();
                            }



                           // peripheralTextView.append("device disconnected\n");
                           // stopScanning();
                            // connectToDevice.setVisibility(View.VISIBLE);
                            // disconnectDevice.setVisibility(View.INVISIBLE);
                        }
                    });
                    break;
                case 2:
                    MapsActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                         //   peripheralTextView.append("device connected\n");
                            count=4;//once connected

                            // connectToDevice.setVisibility(View.INVISIBLE);
                            // disconnectDevice.setVisibility(View.VISIBLE);
                        }
                    });

                    // discover services and characteristics for this device
                    bluetoothGatt.discoverServices();

                    break;
                default:
                    MapsActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                          //  peripheralTextView.append("we encounterned an unknown state, uh oh\n");
                        }
                    });
                    break;
            }
        }
        public final static String ACTION_GATT_SERVICES_DISCOVERED =
                "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
        final private UUID CCCD_ID = UUID.fromString("000002902-0000-1000-8000-00805f9b34fb");
        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            // this will get called after the client initiates a BluetoothGatt.discoverServices() call
            MapsActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    bluetoothGatt  = gatt;
                    peripheralTextView.append("device services have been discovered\n");
                    tx = gatt.getService(UART_UUID).getCharacteristic(TX_UUID);
                    rx = gatt.getService(UART_UUID).getCharacteristic(RX_UUID);
                    // BluetoothGattCharacteristic bleCharacteristic = gattCharacteristic;
                    bluetoothGatt.setCharacteristicNotification(rx,true);
                    bluetoothGatt.readCharacteristic(rx);

                    BluetoothGattDescriptor descriptor = rx.getDescriptor(
                            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    bluetoothGatt.writeDescriptor(descriptor);


                    getLocationPermission();
                   // stopScanning();
                    startTimeScan();
                    sendSettings();


                }
            });
            displayGattServices(bluetoothGatt.getServices());
        }




        private void displayGattServices(List<BluetoothGattService> gattServices) {
            if (gattServices == null) return;

            // Loops through available GATT Services.
            for (BluetoothGattService gattService : gattServices) {

                final String uuid = gattService.getUuid().toString();
                System.out.println("Service discovered: " + uuid);
                MapsActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        peripheralTextView.append("Service disovered: "+uuid+"\n");
                    }
                });
                new ArrayList<HashMap<String, String>>();
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();

                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic :
                        gattCharacteristics) {

                    final String charUuid = gattCharacteristic.getUuid().toString();
                    System.out.println("Characteristic discovered for service: " + gattCharacteristic.getValue());

                    MapsActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            peripheralTextView.append("Characteristic discovered for service: "+charUuid+"\n");
                        }
                    });

                }
            }
        }



        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                peripheralTextView.append("Receiving\n");
            }
        }
    };

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {


        final String heartRate = rx.getStringValue(0);
        System.out.println("Battery : "+heartRate);

        int bat = Integer.parseInt(heartRate);
        calculateBattery(bat);
        //openDeviceInfo();
     /*   peripheralTextView.append("heartRate : "+heartRate);
        peripheralTextView.append("Receiving\n");
        final byte[] data = characteristic.getValue();
        if(data !=null & data.length>0){
            final StringBuilder str = new StringBuilder(data.length);
            for(byte byteChar : data){
                str.append(String.format("%02X",byteChar));
            }
            peripheralTextView.append(""+new String(data)+"\n"+str.toString());
        }*/
    }


    private final BroadcastReceiver mBluetoothStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
            final int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.STATE_OFF);

            final String stateString = "[Broadcast] Action received: " + BluetoothAdapter.ACTION_STATE_CHANGED +
                    ", state changed to ";


            switch (state) {
                case BluetoothAdapter.STATE_TURNING_OFF:
                    peripheralTextView.append("Ble STATE_TURNING_OFF");
                case BluetoothAdapter.STATE_OFF:

                    openWifiSetting();
                    break;
            }
        }
    };
    float prev_scale=1f;
    ImageView circle_img;
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
    private void calculateBattery(int bat) {

        double d = (double)100/(double)4095;
        double d2 = d*(double)bat;
        d3 = (int)d2;
        d_bat = ""+d3;
        MapsActivity.this.runOnUiThread(new Runnable() {
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
                add_battery.setText("" + d_bat+"%");
            }
        });

      //  name_text.setText("Battery: " + d3+"%");

    }
    private final BroadcastReceiver mreceiver_settings = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            wifi_name = intent.getStringExtra("wifiname");
            wifi_pass = intent.getStringExtra("password");
            ring_duration = intent.getStringExtra("duration");
            peripheralTextView.append("wifi settings saved. ");



        }
    };
    private final BroadcastReceiver mreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equalsIgnoreCase("BleDeviceRSSI")){
                rssi = intent.getStringExtra("ble_rssi");
            }
            else{
                String str = intent.getStringExtra("ble_battery");


                if(!str.equalsIgnoreCase("NA")){
                    int bat = Integer.parseInt(str);
                    calculateBattery(bat);
                }
                else{
                    add_battery.setText("Battery: NA");
                }
            }



        }
    };
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
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            getDeviceLocation();
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
                            longi = ""+currentLocation.getLatitude();
                            lati = ""+currentLocation.getLongitude();
                            saveToFile = d_name+"|"+currentLocation.getLatitude()+"|"+currentLocation.getLongitude();
                            writeToFile(saveToFile,getApplicationContext());
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM);
                            if(rssi!="")
                            drawCircle(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));

                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }
    private void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            Toast.makeText(MapsActivity.this, "Device saved successfully", Toast.LENGTH_SHORT).show();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    private void moveCamera(LatLng latLng, float zoom){

        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapsActivity.this);
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
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }
    public void onClickChangeDevice(View view){
      //  if(timer.equals(new Timer()))
       // timer.cancel();
        Intent intent = new Intent(this, MainActivity.class);

        startActivity(intent);
    }
    public void onClickRing(View view) {
        String message = "A";
        peripheralTextView.append("Sentee: " + message);
        if (tx == null || message == null || message.isEmpty()) {
            // Do nothing if there is no device or message to send.
            return;
        }
        // Update TX characteristic value.  Note the setValue overload that takes a byte array must be used.
        tx.setValue(message.getBytes(Charset.forName("UTF-8")));
        if (bluetoothGatt.writeCharacteristic(tx)) {
            peripheralTextView.append("Sent: " + message);
        }
        else {
            peripheralTextView.append("Couldn't write TX characteristic!");
        }
        Toast.makeText(MapsActivity.this, "Ring", Toast.LENGTH_SHORT).show();
           /* Intent incomingMessageIntent = new Intent("incomingMessage");
            incomingMessageIntent.putExtra("Incoming_Message","ring");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(incomingMessageIntent);*/


    }
    public void onClickMapFound(View view){
        Intent intent = new Intent(this, MapsActivity_device_not_found.class);
        intent.putExtra("lati",lati);
        intent.putExtra("longi",longi);
        intent.putExtra("dvname",d_name);
        intent.putExtra("dvadd",d_add);
        intent.putExtra("dvbat",d_bat);
        if(isDeviceOutOfRange)
        intent.putExtra("status","not found");
        else
            intent.putExtra("status","true");
        startActivity(intent);
    }
    public void openDeviceNotFound(){
        Intent intent = new Intent(this, MapsActivity_device_not_found.class);
        intent.putExtra("lati",lati);
        intent.putExtra("longi",longi);
        intent.putExtra("dvname",d_name);
        intent.putExtra("dvadd",d_add);
        intent.putExtra("dvbat",d_bat);
        if(isDeviceOutOfRange)
            intent.putExtra("status","not found");
        else
            intent.putExtra("status","true");
        startActivity(intent);
    }
    private final BroadcastReceiver mreceiver_fromText = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            add_battery.setText("from local broadcast");
        }
    };
}
