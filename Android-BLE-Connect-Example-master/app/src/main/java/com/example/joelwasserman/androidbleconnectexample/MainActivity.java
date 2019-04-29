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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import me.anwarshahriar.calligrapher.Calligrapher;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    Button startScanningButton;
    Button stopScanningButton;
    TextView peripheralTextView;
    TextView oldDevice_name;

    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    Boolean btScanning = false;
    int deviceIndex = 0;
    ArrayList<BluetoothDevice> devicesDiscovered = new ArrayList<BluetoothDevice>();
    ArrayList<BluetoothDevice> devicesDiscovered_old = new ArrayList<BluetoothDevice>();
    ArrayList<BluetoothDevice> devicesDiscovered_connected = new ArrayList<BluetoothDevice>();
    ArrayList<ScanResult> devicesDiscovered_result = new ArrayList<ScanResult>();
    ArrayList<BluetoothDevice> devicesDiscovered_timer = new ArrayList<BluetoothDevice>();
    EditText deviceIndexInput;
    Button connectToDevice;
    Button disconnectDevice;
    BluetoothGatt bluetoothGatt;

    private BluetoothGattCharacteristic tx;
    private BluetoothGattCharacteristic rx;
    public Button sendBtn;
    public Button offBtn;
    ListView lvNewDevices;
    ListView lvConDevices;
    RelativeLayout layout1;

    public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    public static String BLE_RX = "0000ffe1-0000-1000-8000-00805f9b34fb";

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

    public Map<String, String> uuids = new HashMap<String, String>();

    // Stops scanning after 5 seconds.
    private Handler mHandler = new Handler();
    private static final long SCAN_PERIOD = 90*1000;
    private static final long SCAN_PERIOD_timer = 90*1000;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
public TextView simpleText;
    public int after=5*1000;
    public int interval = 120*1000;
    public  Timer timer=new Timer();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Calligrapher calli = new Calligrapher(this);
        calli.setFont(this, "fonts/HELR45W.ttf",true);
        peripheralTextView = (TextView) findViewById(R.id.PeripheralTextView);
        peripheralTextView.setMovementMethod(new ScrollingMovementMethod());
        deviceIndexInput = (EditText) findViewById(R.id.InputIndex);
        oldDevice_name = (TextView) findViewById(R.id.tvDeviceName12);
        deviceIndexInput.setText("0");
        simpleText = (TextView) findViewById(R.id.editText);
        connectToDevice = (Button) findViewById(R.id.ConnectButton);
        connectToDevice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connectToDeviceSelected();
            }
        });

        disconnectDevice = (Button) findViewById(R.id.DisconnectButton);
        disconnectDevice.setVisibility(View.INVISIBLE);
        disconnectDevice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                disconnectDeviceSelected();
            }
        });

        startScanningButton = (Button) findViewById(R.id.StartScanButton);
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //startScanning();
openDeviceInfo("found");
            }
        });

        stopScanningButton = (Button) findViewById(R.id.StopScanButton);
        stopScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopScanning();
            }
        });
       // stopScanningButton.setVisibility(View.INVISIBLE);

        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();
        sendBtn = (Button) findViewById(R.id.send);
        offBtn = (Button) findViewById(R.id.offBuzzer);
        lvNewDevices = (ListView) findViewById(R.id.deviceList);
        lvConDevices = (ListView) findViewById(R.id._connectedList);
        layout1 = (RelativeLayout) findViewById(R.id.old_device);
        layout1.setVisibility(View.INVISIBLE);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendClick();
                //startDisplayActivity();
            }
        });

        offBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                offClick();
                //startDisplayActivity();
            }
        });
        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        getApplicationContext().registerReceiver(mBluetoothStateBroadcastReceiver,
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        lvNewDevices.setOnItemClickListener(MainActivity.this);
        lvConDevices.setOnItemClickListener(MainActivity.this);
        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
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
        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               /* final Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.Settings$WifiApSettingsActivity");
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity( intent);*/


        }
        });
        lvConDevices.setVisibility(View.INVISIBLE);
        String str = readFromFile(getApplicationContext());
if(!str.equalsIgnoreCase("")){
    String[] arr = str.split("\\|");
    if(arr.length>0){
        deviceFromFile = arr[0];
        device_longi = arr[1];
        device_lati = arr[2];
    }
}



if(!deviceFromFile.equalsIgnoreCase("")){
    layout1.setVisibility(View.VISIBLE);
    oldDevice_name.setText(deviceFromFile);
}


        peripheralTextView.append("Old device:"+deviceFromFile);
        startScanning();
      //  client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).buil/d();

        LocalBroadcastManager.getInstance(this).registerReceiver(mreceiver_wifi,new IntentFilter("incomingMessage_wifi"));




    }
public String deviceFromFile="";
    public String device_longi="";
    public String device_lati="";
    private void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
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
public void openWifiSetting(){
    final Intent intent = new Intent(Intent.ACTION_MAIN, null);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);
    final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.Settings$WifiApSettingsActivity");
    intent.setComponent(cn);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity( intent);
    runUdpServer();


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

    Button btn;
    private final BroadcastReceiver mreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("Incoming_Message");

            peripheralTextView.append("in local broadcast");
            if(action == "ring"){
                sendClick();
            }
            else{
                offClick();
            }

        }
    };



    private final BroadcastReceiver mreceiver_wifi = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("Incoming_Message");

            peripheralTextView.append("in wifi broadcast");
            if(action == "ring"){
                sendDataToUdpServer();
            }


        }
    };

    String wifi_name="joyeta_wifi";


    String wifi_pass="12345678";
    String ring_duration=null;

    private final BroadcastReceiver mreceiver_settings = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            wifi_name = intent.getStringExtra("wifiname");
            wifi_pass = intent.getStringExtra("password");
            ring_duration = intent.getStringExtra("duration");
            peripheralTextView.append("wifi settings saved. ");
        }
    };
    private final BroadcastReceiver mreceiver_fromText = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String str = readFromFile(getApplicationContext());
            if(!str.equalsIgnoreCase("")){
                String[] arr = str.split("\\|");
                if(arr.length>0){
                    deviceFromFile = arr[0];
                    device_longi = arr[1];
                    device_lati = arr[2];
                }
            }
            startScanning();
        }
    };
    public  void sendClick(){
        String message = "A";
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
    }
    public void offClick(){
        String message = "B";
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
    }
    public void startDisplayActivity(){
    /*Intent intent = new Intent(this, DisplayInfo.class);

    String name = deviceName;
    String add = deviceAddress;
    intent.putExtra(EXTRA_MESSAGE, name);
    //intent.putExtra(EXTRA_MESSAGE, add);
    startActivity(intent);*/
    }

    public boolean isOldDeviceConnected = false;
    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result.getDevice().getName() != null && result.getDevice().getName().toLowerCase().contains("harrell")) {


                if ( isDuplicateDevice(result) == false) {

                    if (deviceFromFile.equalsIgnoreCase(result.getDevice().getName())) {
                        autoConnectToPairedDevice(result);
                        stopScanning();

                    }
                    devicesDiscovered.add(result.getDevice());

                    devicesDiscovered_result.add(result);
                    peripheralTextView.append("rssi " + result.getRssi() + "\n");

                    adapter = new DeviceListAdapter(getApplicationContext(), R.layout.device_adapter_view, devicesDiscovered, false);
                    lvNewDevices.setAdapter(adapter);

                    adapter.setNotifyOnChange(true);
                    deviceIndex++;

                    // auto scroll for text view
                    final int scrollAmount = peripheralTextView.getLayout().getLineTop(peripheralTextView.getLineCount()) - peripheralTextView.getHeight();
                    // if there is no need to scroll, scrollAmount will be <=0
                    if (scrollAmount > 0) {
                        peripheralTextView.scrollTo(0, scrollAmount);
                    }
                }
            }
        }
    };
public void autoConnectToPairedDevice(ScanResult result){
    peripheralTextView.append("Trying to connect to old device: \n");
    deviceName = result.getDevice().getName();
    deviceAddress = result.getDevice().getAddress();
    rssi = result.getRssi();

  //  bluetoothGatt = result.getDevice().connectGatt(this, false, btleGattCallback);
    bleDevice = result.getDevice();
    isOldDeviceConnected = true;
    loadConnectedDevice(result.getDevice());
}
public void loadConnectedDevice(BluetoothDevice result){
    layout1.setVisibility(View.INVISIBLE);
    lvConDevices.setVisibility(View.VISIBLE);
    devicesDiscovered_connected.clear();
    lvConDevices.setAdapter(null);
    devicesDiscovered_connected.add(result);
    adapter_connected = new DeviceListAdapter(getApplicationContext(), R.layout.device_adapter_view, devicesDiscovered_connected,true);
    lvConDevices.setAdapter(adapter_connected);

    adapter_connected.setNotifyOnChange(true);
}
    private ScanCallback leScanCallback_time = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            System.out.println("start scanning timer onScanResult");
            if(result.getDevice().getName() != null ) {
                if(deviceName.equalsIgnoreCase(result.getDevice().getName())){
                    peripheralTextView.append("connected device Rssi: "+result.getRssi() +"\n");
                    broadCastToBleDeviceRssiIntent(""+result.getRssi());
                }

                devicesDiscovered_timer.add(result.getDevice());



            }

        }
    };

 public boolean isDuplicateDevice(ScanResult result){
     boolean flag = false;
    // peripheralTextView.append("duplicate in method "+devicesDiscovered.size() +"\n");
    for(int i=0;i<devicesDiscovered.size();i++) {

        if (result.getDevice().getName().equalsIgnoreCase(devicesDiscovered.get(i).getName()) ) {
            flag =  true;
            break;
        } else {
            flag = false;
            break;
        }
    }
    return flag;
 }
    // Device connect call back
    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation

            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
           MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    peripheralTextView.append("device read or wrote to\n");
                    ;
                }
            });
        }
        private void writeLine(final CharSequence text) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    peripheralTextView.append("battery: "+text);
                    peripheralTextView.append("\n");
                }
            });
        }
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            peripheralTextView.append(""+newState);
            switch (newState) {
                case 0:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            peripheralTextView.append("device disconnected\n");
                           // connectToDevice.setVisibility(View.VISIBLE);
                           // disconnectDevice.setVisibility(View.INVISIBLE);
                        }
                    });
                    break;
                case 2:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            peripheralTextView.append("device connected\n");
                           // connectToDevice.setVisibility(View.INVISIBLE);
                           // disconnectDevice.setVisibility(View.VISIBLE);
                        }
                    });

                    // discover services and characteristics for this device
                    bluetoothGatt.discoverServices();

                    break;
                default:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            peripheralTextView.append("we encounterned an unknown state, uh oh\n");
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
            MainActivity.this.runOnUiThread(new Runnable() {
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
                  //  rx = bleCharacteristic;
                //    BluetoothGattService gattService = gatt.getService(RX_UUID);
               //     rx =  gattService.getCharacteristic(RX_UUID);
               //     gatt.setCharacteristicNotification(rx,true);
               //     readCustomCharacteristic();
                  //  gatt.setCharacteristicNotification(rx,true);

                  //  readCustomCharacteristic();
                  //  broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                  /*  gatt.setCharacteristicNotification(rx,true);
//peripheralTextView.append(""+rx.getValue().length);
                    if (!gatt.setCharacteristicNotification(rx, true)) {
                        writeLine("Couldn't set notifications for RX characteristic!");
                    }*/
                    // Next update the RX characteristic's client descriptor to enable notifications.





                    openDeviceInfo("found");
                    sendSettings();
                    startTimeScan();
                   // timer.scheduleAtFixedRate(task, after, interval);
                }
            });
            displayGattServices(bluetoothGatt.getServices());
        }

        public void readCustomCharacteristic() {
            if (btAdapter == null || bluetoothGatt == null) {
               // Log.w(TAG, "BluetoothAdapter not initialized");
                return;
            }
            /*check if the service is available on the device*/
            BluetoothGattService mCustomService = bluetoothGatt.getService((RX_UUID));
            if(mCustomService == null){
               peripheralTextView.append("Custom BLE Service not found");
                return;
            }
            /*get the read characteristic from the service*/
            BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(RX_UUID);
            bluetoothGatt.readCharacteristic(mReadCharacteristic);
            if(bluetoothGatt.readCharacteristic(mReadCharacteristic) == false){
                peripheralTextView.append("Failed to read characteristic");
            }
        }

public void startTimeScan(){
    timer= new Timer();

    timer.scheduleAtFixedRate(new TimerTask() {
        @Override
        public void run() {
            //here you can write the code for send the message
            peripheralTextView.append("timer start\n");
             startScanning_timer();
        }
    }, after, interval);
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

        System.out.println(characteristic.getUuid());
        final String heartRate = rx.getStringValue(0);

        devicebattery = heartRate;
        broadCastToBleDeviceInfoIntent(devicebattery);
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
public void broadCastToBleDeviceInfoIntent(String str){
    Intent incomingMessageIntent = new Intent("BleDeviceBattery");
    incomingMessageIntent.putExtra("ble_battery",str);
    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(incomingMessageIntent);


}
    public void broadCastToBleDeviceRssiIntent(String str){
        Intent incomingMessageIntent = new Intent("BleDeviceRSSI");
        incomingMessageIntent.putExtra("ble_rssi",str);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(incomingMessageIntent);


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
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

    public void startScanning() {
        System.out.println("start scanning");
        timer.cancel();

        simpleText.setVisibility(View.VISIBLE);
        btScanning = true;
        deviceIndex = 0;
        devicesDiscovered.clear();
        devicesDiscovered_result.clear();
        lvNewDevices.setAdapter(null);
        peripheralTextView.setText("");
        peripheralTextView.append("Started Scanning\n");
      //  startScanningButton.setVisibility(View.INVISIBLE);
     //   stopScanningButton.setVisibility(View.VISIBLE);
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

    public void startScanning_timer() {
        System.out.println("start scanning timer");

        devicesDiscovered_timer.clear();
        peripheralTextView.setText("");
        peripheralTextView.append("Started Scanning timer\n");

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("start scanning timer run");
                btScanner.startScan(leScanCallback_time);
            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScanning_timer();
            }
        }, SCAN_PERIOD);
    }

    public void stopScanning() {
        System.out.println("stopping scanning");
        peripheralTextView.append("Stopped Scanning\n");
        btScanning = false;
      //  startScanningButton.setVisibility(View.VISIBLE);
      //  stopScanningButton.setVisibility(View.INVISIBLE);
        simpleText.setVisibility(View.INVISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if(!isOldDeviceConnected && deviceFromFile!=""){
                    deviceName = deviceFromFile;
//openDeviceInfo("not found");
                    openDeviceInfoNotFound();
                }
                btScanner.stopScan(leScanCallback);
            }
        });
    }
    public void stopScanning_timer() {
        System.out.println("stopping scanning");
        peripheralTextView.append("Stopped Scanningtimer\n");
        btScanning = false;
        if(idDeviceOutofRange()){
            timer.cancel();
            openWifiSetting();
        }
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback_time);
            }
        });
    }
public boolean idDeviceOutofRange(){
    boolean flag = false;
    // peripheralTextView.append("duplicate in method "+devicesDiscovered.size() +"\n");
    for(int i=0;i<devicesDiscovered_timer.size();i++) {

        if (deviceName.equalsIgnoreCase(devicesDiscovered_timer.get(i).getName())) {
            flag =  false;
            break;
        } else {
            flag = true;
            break;
        }
    }
    return flag;
}
    public void connectToDeviceSelected() {
        peripheralTextView.append("Trying to connect to device at index: " + deviceIndexInput.getText() + "\n");
        int deviceSelected = Integer.parseInt(deviceIndexInput.getText().toString());
        bluetoothGatt = devicesDiscovered.get(deviceSelected).connectGatt(this, false, btleGattCallback);
    }
    public String deviceName = "Ble";
    public String deviceAddress = "11230989302";
    public String devicebattery = "NA";
    public int rssi=-48;

    public BluetoothDevice bleDevice;
    public void connectToDeviceSelectedFromList(int i){
        peripheralTextView.append("Trying to connect to device at index: " + i + "\n");
        deviceName = devicesDiscovered.get(i).getName();
        deviceAddress = devicesDiscovered.get(i).getAddress();
        rssi = devicesDiscovered_result.get(i).getRssi();

        openDeviceInfo("new device");
      /*  bluetoothGatt = devicesDiscovered.get(i).connectGatt(this, false, btleGattCallback);
        bleDevice = devicesDiscovered.get(i);*/
        loadConnectedDevice(devicesDiscovered.get(i));
    }
    public void connectToDeviceSelectedFromConnetedList(int i){
        peripheralTextView.append("Trying to connect to device from: " + i + "\n");
        deviceName = devicesDiscovered_connected.get(i).getName();
        deviceAddress = devicesDiscovered_connected.get(i).getAddress();
        for(int j=0;j<devicesDiscovered_result.size();j++){
            if(devicesDiscovered_result.get(j).getDevice().getName().equalsIgnoreCase(deviceName) ){
                rssi = devicesDiscovered_result.get(j).getRssi();
                break;
            }
        }
        openDeviceInfo("new device");
        //writeToFile(deviceName,getApplicationContext());
      /*  bluetoothGatt = devicesDiscovered_connected.get(i).connectGatt(this, false, btleGattCallback);
        bleDevice = devicesDiscovered_connected.get(i);*/
       // loadConnectedDevice(devicesDiscovered.get(i));
    }
    public void disconnectDeviceSelected() {
        peripheralTextView.append("Disconnecting from device\n");
        bluetoothGatt.disconnect();
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            final String uuid = gattService.getUuid().toString();
            System.out.println("Service discovered: " + uuid);
            MainActivity.this.runOnUiThread(new Runnable() {
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

                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        peripheralTextView.append("Characteristic discovered for service: "+charUuid+"\n");
                    }
                });

            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

       /* client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.joelwasserman.androidbleconnectexample/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);*/
    }

    @Override
    public void onStop() {
        super.onStop();

      /*  Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.joelwasserman.androidbleconnectexample/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();*/
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(view.getParent().toString().contains("deviceList")){

            connectToDeviceSelectedFromList(i);
        }
        else{
            connectToDeviceSelectedFromConnetedList(i);
        }
        //connectToDeviceSelectedFromList(i);

    }

    public void onClickScan(View view) {
        peripheralTextView.append("timer stop\n");

startScanning();

    }
    public void openDeviceInfo(String status){
      Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("lati",device_lati);
        intent.putExtra("longi",device_longi);
        intent.putExtra("status",status);
        intent.putExtra("Dev_name",deviceName);
        intent.putExtra("Dev_add",deviceAddress);
        intent.putExtra("Dev_rssi",""+rssi);
        intent.putExtra("Dev_bat",""+devicebattery);
        startActivity(intent);
    }

    public void openDeviceInfoNotFound(){
        Intent intent = new Intent(this, MapsActivity_device_not_found.class);
        intent.putExtra("lati",device_lati);
        intent.putExtra("longi",device_longi);
        intent.putExtra("status","false");
        intent.putExtra("dvname",deviceName);
        intent.putExtra("dvadd",deviceAddress);
       // intent.putExtra("Dev_rssi",""+rssi);
        intent.putExtra("dvbat",""+devicebattery);
        startActivity(intent);
    }
    public void openDeviceInfo_wifi(){
        Intent intent = new Intent(this, MapsActivity_wifi.class);
        intent.putExtra("lati",device_lati);
        intent.putExtra("longi",device_longi);
        intent.putExtra("Dev_name",deviceName);
       intent.putExtra("Dev_add",deviceAddress);
        intent.putExtra("Dev_rssi",""+rssi);
        startActivity(intent);

    }
    public void openSettings(View view){
        Intent intent = new Intent(this, Settings.class);

        startActivity(intent);

    }
    private static final int UDP_SERVER_PORT = 44444;
    private static final int MAX_UDP_DATAGRAM_LEN = 1500;
    private static String IP;

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
                        rssi = Integer.valueOf(arr[0]);
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







}
