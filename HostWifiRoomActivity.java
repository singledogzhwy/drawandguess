package com.lilwulin.team.drawguess;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import java.net.ServerSocket;
import java.net.SocketException;


public class HostWifiRoomActivity extends Activity {

    Thread serverThread = null;
    HostBroadcastAddressThread bcThread = null;
    TextView ip_tv;
    TextView roomname_tv;
    Button btnStart;
    String ip;
    boolean is_waiting_guess = true;
    String roomName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host_wifi_room);
        this.serverThread = new Thread(new ServerRunnable());
        this.serverThread.start();
        ip_tv = (TextView) findViewById(R.id.host_ip_tv);
        roomname_tv = (TextView)findViewById(R.id.host_roomname_tv);
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        ip_tv.setText("你的IP地址: " + ip);
        btnStart = (Button) findViewById(R.id.host_start_game_btn);
        btnStart.setBackgroundDrawable(getResources().getDrawable(R.drawable.mybuttonstyle_selected));
        btnStart.setClickable(false);
        try {
            SharedVar.HostBroadCastSocket = new DatagramSocket(SharedVar.BroadcastPort);
            SharedVar.HostBroadCastSocket.setBroadcast(true);
        } catch (SocketException e) {
            Log.e("set broadcast", e.toString());
        }
        // 获取Host填入的房间名字
        Bundle bundle = this.getIntent().getExtras();
        roomName = bundle.getString("roomName");
        roomname_tv.setText("房间名：" + roomName);
        this.bcThread = new HostBroadcastAddressThread();
        this.bcThread.start();
        Toast.makeText(getApplicationContext(), "房间已建立",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_host_wifi_room, menu);
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

    class ServerRunnable implements Runnable {
        public void run() {
            SharedVar.HostSocket = null;
            try {
                SharedVar.serverSocket = new ServerSocket(SharedVar.PORT);
            } catch (IOException e) {
                Log.e("ServerSocket", e.toString());
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SharedVar.HostSocket = SharedVar.serverSocket.accept();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"有人加入了",Toast.LENGTH_LONG).show();
                            btnStart.setBackgroundDrawable(getResources().getDrawable(R.drawable.mybuttonstyle_default));
                            btnStart.setClickable(true);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void hostStartGameClick(View view) {
        try {
            SharedVar.hostOStream = new DataOutputStream(SharedVar.HostSocket.getOutputStream());
            SharedVar.hostIStream=new DataInputStream(SharedVar.HostSocket.getInputStream());

            SharedVar.hostOStream.writeUTF("游戏开始!");
            SharedVar.hostOStream.flush();
            // 进入画图
            is_waiting_guess = false;
            startActivity(new Intent(HostWifiRoomActivity.this, MainActivity.class));
            finish();
        } catch (Exception e) {
            Log.e("dos", e.toString());
        }
    }

    // 获取当前网络的广播地址
    private InetAddress getBroadcastAddress() throws IOException {
        WifiManager myWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        DhcpInfo myDhcpInfo = myWifiManager.getDhcpInfo();
        if (myDhcpInfo == null) {
            System.out.println("Could not get broadcast address");
            return null;
        }
        int broadcast = (myDhcpInfo.ipAddress & myDhcpInfo.netmask)
                | ~myDhcpInfo.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    private class HostBroadcastAddressThread extends Thread {

        @Override
        public void run() {
            try {
                InetAddress address = getBroadcastAddress();
                JSONObject json = new JSONObject();
                json.put("ip", ip);
                json.put("room_name", roomName);
                byte data[] = json.toString().getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length, address, SharedVar.BroadcastPort);
                while (is_waiting_guess) {
                    SharedVar.HostBroadCastSocket.send(packet);
                    Log.e("send packet", json.toString());
                    Thread.sleep(3000);
                }
            } catch (SocketException e) {
                Log.e("Wifi broadcast", e.toString());
            } catch (IOException e) {
                Log.e("Wifi broadcast", e.toString());
            } catch (JSONException e) {
                Log.e("Wifi broadcast", e.toString());
            } catch (InterruptedException e) {
                Log.e("Wifi broadcast", e.toString());
            }
        }

    }


}
