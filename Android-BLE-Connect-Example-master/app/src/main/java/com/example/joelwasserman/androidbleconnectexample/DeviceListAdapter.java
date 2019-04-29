package com.example.joelwasserman.androidbleconnectexample;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import me.anwarshahriar.calligrapher.Calligrapher;


public class DeviceListAdapter extends  ArrayAdapter<BluetoothDevice> {

    private LayoutInflater mLayoutInflater;
    private ArrayList<BluetoothDevice> mDevices;
    private int  mViewResourceId;
    public int rssi;
    public boolean isOldDevice=false;

    public DeviceListAdapter(Context context, int tvResourceId, ArrayList<BluetoothDevice> devices,boolean isConnected){
        super(context, tvResourceId,devices);
        this.mDevices = devices;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = tvResourceId;
        isOldDevice = isConnected;

    }


    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mLayoutInflater.inflate(mViewResourceId, null);
        Calligrapher calli = new Calligrapher(getContext());
        calli.setFont(convertView, "fonts/HELR45W.ttf");

        BluetoothDevice device = mDevices.get(position);

        if (device != null) {
            TextView deviceName = (TextView) convertView.findViewById(R.id.tvDeviceName);
            TextView deviceAdress = (TextView) convertView.findViewById(R.id.tvDeviceAddress);
          LinearLayout ly = (LinearLayout)convertView.findViewById(R.id.connectL);
            ly.setVisibility(View.INVISIBLE);
            if (deviceName != null) {
                deviceName.setText(device.getName());
            }
            if (deviceAdress != null) {
                deviceAdress.setText(device.getAddress());
            }
            if(isOldDevice){
                ly.setVisibility(View.VISIBLE);
            }

        }

        return convertView;
    }

}
