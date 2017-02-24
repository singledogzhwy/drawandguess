package com.lilwulin.team.drawguess;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.view.KeyEvent;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;


public class LoginActivity extends Activity {

    private TranslateAnimation myAnimation_up;
    private TranslateAnimation myAnimation_left;
    private TranslateAnimation myAnimation_right;
    private TranslateAnimation myAnimation_down;
    private Button createRoomButton;
    private Button joinRoomButton;
    private ImageView titleIV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        myAnimation_up = new TranslateAnimation(0, 0, 0, -300);
        myAnimation_up.setDuration(100);
        myAnimation_left = new TranslateAnimation(0, -300, 0, 0);
        myAnimation_left.setDuration(100);
        myAnimation_right = new TranslateAnimation(0, 300, 0, 0);
        myAnimation_right.setDuration(100);
        myAnimation_down = new TranslateAnimation(0, 0, 0, 300);
        myAnimation_down.setDuration(200);

        createRoomButton = (Button)findViewById(R.id.create_room_btn);
        joinRoomButton = (Button)findViewById(R.id.join_room_btn);
        titleIV = (ImageView)findViewById(R.id.title);
    }

    protected void createRoomDialog()              //对话框选择创建房间的连接方式
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setMessage("请选择创建房间的方式");
        builder.setPositiveButton("wifi连接", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                wifiNameDialog();
            }
        });
        builder.setNegativeButton("蓝牙连接", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(LoginActivity.this,HostBluetoothRoomActivity.class));
                }
        });
        builder.create().show();
    }

    protected void connectRoomDialog()               //对话框选择连接到房间的方式
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setMessage("请选择连接房间的方式");
        builder.setPositiveButton("wifi连接", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(LoginActivity.this, GuestWifiRoomActivity.class));
            }
        });
        builder.setNegativeButton("蓝牙连接", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(LoginActivity.this, GuestBluetoothRoomActivity.class));
            }
        });
        builder.create().show();
    }

    protected void wifiNameDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        final EditText et = new EditText(this);
        builder.setTitle("请输入要创建房间的名字");
        builder.setView(et);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Bundle bundle = new Bundle();
                bundle.putString("roomName", et.getText().toString());
                Intent intent = new Intent(LoginActivity.this, HostWifiRoomActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("取消",null);
        builder.create().show();
    }

    public void createRoomClick(View view) {
       createRoomDialog();
    }

    public void joinRoomClick(View view) {
        connectRoomDialog();
    }

    private long mPressedTime = 0;

    long firstTime = 0;
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 800) {//如果两次按键时间间隔大于800毫秒，则不退出
                Toast.makeText(LoginActivity.this, "再按一次返回键退出程序",
                        Toast.LENGTH_SHORT).show();
                firstTime = secondTime;//更新firstTime
                return true;
            } else {
                System.exit(0);//否则退出程序
            }
        }
        return super.onKeyUp(keyCode, event);
    }
}
