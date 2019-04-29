package com.mavencube.joyetadey.myapplication1;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {

    TextView tvWifiState;
    TextView tvScanning, tvResult;

    ArrayList<InetAddress> inetAddresses;
Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvWifiState = (TextView)findViewById(R.id.WifiState);
        tvScanning = (TextView)findViewById(R.id.Scanning);
        tvResult = (TextView)findViewById(R.id.Result);
btn = (Button) findViewById(R.id.button);
btn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        sendDataToUdpServer();
    }
});
        //To prevent memory leaks on devices prior to Android N,
        //retrieve WifiManager with
        //getApplicationContext().getSystemService(Context.WIFI_SERVICE),
        //instead of getSystemService(Context.WIFI_SERVICE)
     /*   WifiManager wifiManager =
                (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        tvWifiState.setText(readtvWifiState(wifiManager));

        new ScanTask(tvScanning, tvResult).execute();*/
        runUdpServer();

     /*   OkHttpClient client = new OkHttpClient();
        String url = "http://192.168.43.138/on";
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    final String myresponse = response.body().string();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                           // peripheralTextView.append("response from server "+myresponse);
                        }
                    });
                }
            }
        });*/
    }

    // "android.permission.ACCESS_WIFI_STATE" is needed
    private String readtvWifiState(WifiManager wm){
        String result = "";
        switch (wm.getWifiState()){
            case WifiManager.WIFI_STATE_DISABLED:
                result = "WIFI_STATE_DISABLED";
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                result = "WIFI_STATE_DISABLING";
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                result = "WIFI_STATE_ENABLED";
                break;
            case WifiManager.WIFI_STATE_ENABLING:
                result = "WIFI_STATE_ENABLING";
                break;
            case WifiManager.WIFI_STATE_UNKNOWN:
                result = "WIFI_STATE_UNKNOWN";
                break;
            default:
        }
        return result;
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
                    lText = str.substring(1,str.length());//new String(dp.getData());
                    Log.i("UDP packet received", ""+lText);

                    IP = lText;
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
    private class ScanTask extends AsyncTask<Void, String, Void> {

        TextView tvCurrentScanning, tvScanResullt;
        ArrayList<String> canonicalHostNames;

        public ScanTask(TextView tvCurrentScanning, TextView tvScanResullt) {
            this.tvCurrentScanning = tvCurrentScanning;
            this.tvScanResullt = tvScanResullt;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            tvCurrentScanning.setText("Finished.");
            tvScanResullt.setText("");
            for (int i = 0; i < inetAddresses.size(); i++) {
                tvScanResullt.append(canonicalHostNames.get(i) + "\n");
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            scanInetAddresses();
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            tvCurrentScanning.setText(values[0]);
        }

        private void scanInetAddresses() {
            //May be you have to adjust the timeout
            final int timeout = 500;

            if (inetAddresses == null) {
                inetAddresses = new ArrayList<>();
            }
            inetAddresses.clear();

            if (canonicalHostNames == null) {
                canonicalHostNames = new ArrayList<>();
            }
            canonicalHostNames.clear();

            //For demonstration, scan 192.168.1.xxx only
            byte[] ip = {(byte) 192, (byte) 168, (byte) 43, 0};
            for (int j = 0; j < 255; j++) {
                ip[3] = (byte) j;
                try {
                    InetAddress checkAddress = InetAddress.getByAddress(ip);
                    publishProgress(checkAddress.getCanonicalHostName());
                    if (checkAddress.isReachable(timeout)) {
                        inetAddresses.add(checkAddress);
                        canonicalHostNames.add(checkAddress.getCanonicalHostName());
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    publishProgress(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    publishProgress(e.getMessage());
                }
            }
        }


    }

}
