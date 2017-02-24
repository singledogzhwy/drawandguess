package com.lilwulin.team.drawguess;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

/**
 * Created by wulin on 15/4/11.
 */
public class SharedVar {
    public static ServerSocket serverSocket=null;
    public static Socket HostSocket=null;
    public static Socket GuestSocket=null;
    public static final int PORT = 6663;
    public static DataOutputStream hostOStream = null;
    public static DataInputStream hostIStream=null;
    public static DataInputStream guestDIStream = null;
    public static DataOutputStream guestOStream=null;


    public static final UUID MY_UUID =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    public static DatagramSocket HostBroadCastSocket = null;
    public static int BroadcastPort = 6666;
    public static double scale = 1;
    public static double host_width = 0;
    public static double host_height=0;

    //蓝牙
    public static BluetoothSocket gbtsocket=null;

    public static BluetoothServerSocket hbtssocket=null;

    public static BluetoothSocket hbtsocket=null;


    public static String answer="";
    public static String hint="";
}
