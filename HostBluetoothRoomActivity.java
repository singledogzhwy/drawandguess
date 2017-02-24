package com.lilwulin.team.drawguess;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class HostBluetoothRoomActivity extends ActionBarActivity {

    public static final int REQUEST_ENABLE_BT=1;
    private static final String NAME = "Bluetooth";

    BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK)init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host_bluetooth_room);

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
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        }
        else init();
    }

    public void init(){
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, SharedVar.MY_UUID);
        } catch (IOException e) {
        }
        SharedVar.hbtssocket = tmp;
        service.start();
    }

    Thread service=new Thread(){
        public void run(){
            try {
                Log.v("yqf","thread running");
                SharedVar.hbtsocket=SharedVar.hbtssocket.accept();
                Log.v("yqf","accept");
                SharedVar.hostOStream=new DataOutputStream(SharedVar.hbtsocket.getOutputStream());
                SharedVar.hostIStream=new DataInputStream(SharedVar.hbtsocket.getInputStream());
                startActivity(new Intent(HostBluetoothRoomActivity.this, MainActivity.class));
                HostBluetoothRoomActivity.this.finish();


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_host_bluetooth_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
