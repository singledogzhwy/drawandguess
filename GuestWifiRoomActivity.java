package com.lilwulin.team.drawguess;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class GuestWifiRoomActivity extends Activity {

    private String host_ip;
    ProgressDialog progressDialog = null;
    GuestReceiveBroadcastThread grbThread = null;
    ListView room_list = null;
    WifiRoomAdapter mAdapter = null;
    HashMap<String, String> roomMap = new HashMap<>();
    ArrayList<Map.Entry<String, String>> roomArrayList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_wifi_room);
        Bundle bundle = this.getIntent().getExtras();

        grbThread = new GuestReceiveBroadcastThread();
        grbThread.start();
        room_list = (ListView) findViewById(R.id.guest_wifi_room_list);
        roomArrayList = new ArrayList();
        roomArrayList.addAll(roomMap.entrySet());
        mAdapter = new WifiRoomAdapter(roomArrayList);
        room_list.setAdapter(mAdapter);
        room_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                host_ip = ((TextView)findViewById(R.id.room_ip)).getText().toString();
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(GuestWifiRoomActivity.this, "点击取消", "连接中", true, true);
                Log.e("wifi", host_ip);
                new Thread(new ClientRunnable()).start();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_guest_wifi_room, menu);
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

    private class ClientRunnable implements Runnable {

        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(host_ip);
                SharedVar.GuestSocket = new Socket(serverAddr, SharedVar.PORT);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.cancel();
                        }
                    }
                });
                try {
                    SharedVar.guestDIStream = new DataInputStream(SharedVar.GuestSocket.getInputStream());
                    SharedVar.guestOStream=new DataOutputStream(SharedVar.GuestSocket.getOutputStream());
                    final String val = SharedVar.guestDIStream.readUTF();
                    SharedVar.host_width = SharedVar.guestDIStream.readDouble();
                    SharedVar.host_height= SharedVar.guestDIStream.readDouble();
                    SharedVar.answer=SharedVar.guestDIStream.readUTF();
                    Log.v("yqf","width:"+SharedVar.host_width+";"+SharedVar.host_height);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), val, Toast.LENGTH_LONG).show();
                            startActivity(new Intent(GuestWifiRoomActivity.this, GuestWatchActivity.class));
                            finish();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (UnknownHostException e1) {
                Log.e("Error", e1.toString());
            } catch (IOException e1) {
                Log.e("Error", e1.toString());
            }
        }
    }

//    public void guestJoinRoomClick(View view) {
//        host_ip = ip_et.getText().toString();
//        if (progressDialog != null && progressDialog.isShowing()) {
//            progressDialog.dismiss();
//        }
//        progressDialog = ProgressDialog.show(this, "点击取消", "连接中", true, true);
//        Log.e("wifi", host_ip);
//        new Thread(new ClientRunnable()).start();
//
//    }

    private class GuestReceiveBroadcastThread extends Thread {
        @Override
        public void run() {
            DatagramSocket socket;
            try {
                socket = new DatagramSocket(SharedVar.BroadcastPort);
                byte data[] = new byte[4 * 1024];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                while (true) {
                    socket.receive(packet);
                    String result = new String(packet.getData(), packet.getOffset(), packet.getLength());
                    Log.e("receive data", result);
                    JSONObject roomJson = null;
                    roomJson = new JSONObject(result);
                    roomMap.put(roomJson.getString("ip"), roomJson.getString("room_name"));
                    roomArrayList.clear();
                    roomArrayList.addAll(roomMap.entrySet());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }
            } catch (SocketException e) {
                Log.e("receive broadcast", e.toString());
            } catch (IOException e) {
                Log.e("receive broadcast", e.toString());
            } catch (JSONException e) {
                Log.e("receive broadcast", e.toString());
            }
        }

    }

    private class WifiRoomAdapter extends BaseAdapter {
        private final ArrayList mData;

        private WifiRoomAdapter(ArrayList<Map.Entry<String, String>> mapArray) {
            mData = mapArray;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Map.Entry<String, String> getItem(int position) {
            return (Map.Entry) mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View result;

            if (convertView == null) {
                result = LayoutInflater.from(parent.getContext()).inflate(R.layout.guest_wifi_room_item, parent, false);
            } else {
                result = convertView;
            }

            Map.Entry<String, String> item = getItem(position);

            ((TextView) result.findViewById(R.id.room_ip)).setText(item.getKey());
            ((TextView) result.findViewById(R.id.room_name)).setText(item.getValue());

            return result;
        }
    }

}
