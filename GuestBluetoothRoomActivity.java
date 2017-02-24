package com.lilwulin.team.drawguess;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;


public class GuestBluetoothRoomActivity extends ActionBarActivity {

    public static final int REQUEST_ENABLE_BT=1;
    public static final int REQUEST_CONNECT_DEVICE_SECURE=2;

    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    BluetoothAdapter mBluetoothAdapter;

    private ListView lv;
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_ENABLE_BT){
        switch(resultCode) {
            case RESULT_OK:
                init();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_bluetooth_room);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        //手机不支持蓝牙
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        init();


    }

    private void init() {
        lv = (ListView) findViewById(R.id.lv);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        lv.setAdapter(mPairedDevicesArrayAdapter);
        lv.setOnItemClickListener(mItemClickListener);

        // Get a set of currently paired devices
        //查找已经匹配的设备
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = "no paired device";
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);

            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

            try {
                SharedVar.gbtsocket = device.createRfcommSocketToServiceRecord(SharedVar.MY_UUID);
                SharedVar.gbtsocket.connect();
                Log.v("yqf", "connect success");

                SharedVar.guestDIStream = new DataInputStream(SharedVar.gbtsocket.getInputStream());
                SharedVar.guestOStream = new DataOutputStream(SharedVar.gbtsocket.getOutputStream());

                SharedVar.host_width = SharedVar.guestDIStream.readDouble();
                SharedVar.host_height = SharedVar.guestDIStream.readDouble();
                SharedVar.answer = SharedVar.guestDIStream.readUTF();
                Log.v("yqf", "width:" + SharedVar.host_width + ";" + SharedVar.host_height);
                startActivity(new Intent(GuestBluetoothRoomActivity.this, GuestWatchActivity.class));
                finish();

            }catch (IOException e) {
                e.printStackTrace();
            }

        }
    };


    /*
    public void join(View v){
        startActivityForResult(new Intent(this, BluetoothDeviceListActivity.class),
                REQUEST_CONNECT_DEVICE_SECURE);
    }
    */



}
