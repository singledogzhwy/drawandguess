package com.lilwulin.team.drawguess;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lilwulin.team.drawguess.util.DensityUtil;

import java.io.IOException;
import java.util.Random;
import java.util.Stack;

public class GuestWatchActivity extends Activity implements View.OnClickListener {
    private GuestWatchView guestWatchView;

    private LinearLayout main;
    private TextView[] tvs;
    private LinearLayout llt;
    private LinearLayout ll;      //显示TextViewl行
    private LinearLayout ll1;    //显示Button上行
    private LinearLayout ll2;   //显示Button下行


    private char[] letters=new char[16];
    private Button[] bts=new Button[16];
    private Stack<Integer>stack=new Stack<>();

    private int index=0;  //输入字母的索引
    private StringBuilder sb=new StringBuilder();  //输入的字符
    private int currBt=0;  //当前按Button的序号
    private int errorCount=0;   //输错次数


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.guest_watch);
        guestWatchView = (GuestWatchView) this.findViewById(R.id.watch_drawing);
        main=(LinearLayout)findViewById(R.id.root);
        init();
    }


    //重新生成TextView和Button
    public void rebuilt(){
        main.removeView((View)llt);
        init();
    }

    public void init(){
        llt=new LinearLayout(this);
        llt.setOrientation(LinearLayout.VERTICAL);
        llt.setGravity(Gravity.BOTTOM|Gravity.CENTER);

        ll=new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams tvlp=new LinearLayout.LayoutParams(DensityUtil.dip2px(this,30),DensityUtil.dip2px(this,30));
        tvlp.setMargins(DensityUtil.dip2px(this,2),DensityUtil.dip2px(this,2),DensityUtil.dip2px(this,2),DensityUtil.dip2px(this,2));

        tvs=new TextView[SharedVar.answer.length()];

        for(int i=0;i<SharedVar.answer.length();i++){
            tvs[i]=new TextView(this);
            tvs[i].setBackgroundColor(Color.WHITE);
            tvs[i].setLayoutParams(tvlp);
            tvs[i].setGravity(Gravity.CENTER);
            ll.addView(tvs[i]);
        }
        llt.addView(ll);

        String alphabet="abcdefghijklmnopqrstuvwxyz";
        char[] temp=new char[16];
        Random random=new Random();
        for(int i=0;i<SharedVar.answer.length();i++){
            temp[i]=SharedVar.answer.charAt(i);
        }
        for(int i= SharedVar.answer.length();i<16;i++){
            temp[i]=alphabet.charAt(random.nextInt(26));
        }

        letters=m3(temp);//使数组乱序

        LinearLayout.LayoutParams btlp=new LinearLayout.LayoutParams(DensityUtil.dip2px(this,35),DensityUtil.dip2px(this,35));
        btlp.setMargins(DensityUtil.dip2px(this,2),DensityUtil.dip2px(this,2),DensityUtil.dip2px(this,2),DensityUtil.dip2px(this,2));

        ll1=(LinearLayout)LayoutInflater.from(this).inflate(R.layout.mylinearlayout,null);
        ll1.setOrientation(LinearLayout.HORIZONTAL);
        ll1.setGravity(Gravity.CENTER);
        for(int i=0;i<8;i++){
            bts[i]= new Button(this);
            bts[i].setLayoutParams(btlp);
            bts[i].setBackgroundDrawable(getResources().getDrawable(R.drawable.letter_button));
            bts[i].setTextSize(DensityUtil.dip2px(this,6));
            bts[i].setText(letters[i] + "");
            bts[i].setOnClickListener(this);
            ll1.addView(bts[i]);
        }
        llt.addView(ll1);

        ll2=(LinearLayout)LayoutInflater.from(this).inflate(R.layout.mylinearlayout,null);
        ll2.setOrientation(LinearLayout.HORIZONTAL);
        ll2.setGravity(Gravity.CENTER);
        for(int i=8;i<16;i++){
            bts[i]=new Button(this);
            bts[i].setLayoutParams(btlp);
            //bts[i].setBackgroundColor(Color.LTGRAY);
            bts[i].setBackgroundDrawable(getResources().getDrawable(R.drawable.letter_button));
            bts[i].setTextSize(DensityUtil.dip2px(this,6));
            bts[i].setText(letters[i] + "");
            bts[i].setOnClickListener(this);
            ll2.addView(bts[i]);
        }
        llt.addView(ll2);

        main.addView(llt);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //关闭socket
        try {
        if(SharedVar.GuestSocket!=null) SharedVar.GuestSocket.close();
        if(SharedVar.gbtsocket!=null)SharedVar.gbtsocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //乱序数组函数
    public char []  m3(char [] arr) {
        char [] arr2 =new char[arr.length];
        int count = arr.length;
        int cbRandCount = 0;// 索引
        int cbPosition = 0;// 位置
        int k =0;
        do {
            Random rand = new Random();
            int r = count - cbRandCount;
            cbPosition = rand.nextInt(r);
            arr2[k++] = arr[cbPosition];
            cbRandCount++;
            arr[cbPosition] = arr[r - 1];// 将最后一位数值赋值给已经被使用的cbPosition
        } while (cbRandCount < count);
        return arr2;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_guest_wathch, menu);
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

    public void initTextViewButton(){
        index=0;
        for(int i=0;i<SharedVar.answer.length();i++)tvs[i].setText("");
        for(int i=0;i<16;i++)bts[i].setClickable(true);
        for(int i=0;i<16;i++)bts[i].setBackgroundDrawable(getResources().getDrawable(R.drawable.letter_button));
        sb.delete(0,sb.length());
    }
    @Override
    public void onClick(View v) {
        Button b = (Button)v;
        for(int i=0;i<16;i++){if(b==bts[i]){currBt=i;break;}}  //查看哪个button被点击
        stack.push(currBt);     //当前button入栈，方便后面删除

        sb.append(b.getText().toString());
        tvs[index].setText(b.getText().toString());
        b.setClickable(false);
        b.setBackgroundDrawable(getResources().getDrawable(R.drawable.letter_button_gray));
        index++;

        //输入满字数了进行判断
        if(index==SharedVar.answer.length()){

            //答案正确
            if(sb.toString().equals(SharedVar.answer)){
                initTextViewButton();
                //通知host答对了
                try {
                    SharedVar.guestOStream.writeUTF("correct");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //弹出对话框
                dialogBuild();
            }

            //答案错误
            else {
                initTextViewButton();
                errorCount++;
                Toast.makeText(this,"答案错误",Toast.LENGTH_LONG).show();
                //错误次数达到3次
                if(errorCount>=3){
                    //通知host答错了3次
                    try {
                        SharedVar.guestOStream.writeUTF("error");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //弹出对话框
                    dialogBuildError();
                }
            }
        }
    }

    public void dialogBuild(){
        AlertDialog.Builder builder = new  AlertDialog.Builder(this);
        builder.setMessage("恭喜你答对了");
        builder.setTitle("提示");
        builder.setPositiveButton("下一题", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                try {
                    SharedVar.guestOStream.writeUTF("newgame");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("结束游戏", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                try {
                    SharedVar.guestOStream.writeUTF("end");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                GuestWatchActivity.this.finish();
            }
        });
        builder.create().show();
    }

    public void dialogBuildError(){
        AlertDialog.Builder builder = new  AlertDialog.Builder(this);
        builder.setMessage("对不起你答错了3次");
        builder.setTitle("提示");
        builder.setPositiveButton("下一题", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                try {
                    SharedVar.guestOStream.writeUTF("newgame");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("结束游戏", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                try {
                    SharedVar.guestOStream.writeUTF("end");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                GuestWatchActivity.this.finish();
            }
        });
        builder.create().show();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            //do something...
            if(index>0){
                index--;
                tvs[index].setText("");
                int btIndex=stack.pop();
                bts[btIndex].setClickable(true);
                bts[btIndex].setBackgroundDrawable(getResources().getDrawable(R.drawable.letter_button));
                sb.deleteCharAt(index);

                Log.v("yqf","delete:"+sb.toString());
            }
            else{
                AlertDialog.Builder builder = new  AlertDialog.Builder(this);
                    builder.setMessage("确认退出？");
                    builder.setTitle("提示");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            try {
                                SharedVar.guestOStream.writeUTF("end");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            GuestWatchActivity.this.finish();
                        }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}

