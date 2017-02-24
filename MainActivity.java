package com.lilwulin.team.drawguess;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

//这个类是画图的
public class MainActivity extends Activity implements OnClickListener {

    private DrawingView drawView;
    private ImageButton currPaint, drawBtn, eraseBtn, newBtn, saveBtn;

    private float smallBrush, mediumBrush, largeBrush;
    protected String currColor;


    private TextView tv;
    private String[] hints;

    Random random=new Random();

    //接收对方信息的线程
    Thread reciever=new Thread(){
        public void run(){

            while(true){
                String str=null;
                try {
                    str=SharedVar.hostIStream.readUTF();
                } catch (IOException e) {
                    Log.v("yqf",e.toString());
                    return;
                }
                if(str==null)return;
                if(str.equals("end")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,"对方退出了游戏",Toast.LENGTH_LONG).show();
                            MainActivity.this.finish();
                            return;
                        }
                    });

                }
                else if(str.equals("correct")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "对方答对了", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                else if(str.equals("error")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "对方答错了3次", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                else if(str.equals("newgame")){
                    runOnUiThread(new Runnable() {
                        public void run() {
                            dialogBuild();
                        }
                    });
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        reciever.start();   //接收geust的信息
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //关闭socket
        try {
            if(SharedVar.HostSocket!=null) SharedVar.HostSocket.close();
            if(SharedVar.serverSocket!=null)SharedVar.serverSocket.close();
            if(SharedVar.hbtssocket!=null)SharedVar.hbtssocket.close();
            if(SharedVar.hbtsocket!=null)SharedVar.hbtsocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init(){
        hints=getResources().getStringArray(R.array.hints);
        drawView = (DrawingView) findViewById(R.id.drawing);
        LinearLayout paintLayout = (LinearLayout) findViewById(R.id.paint_colors);
        currPaint = (ImageButton) paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

        tv=(TextView)findViewById(R.id.tv);

        //随机生成
        SharedVar.hint=hints[random.nextInt(hints.length)];
        tv.setText(SharedVar.hint);

        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_siz);

        drawBtn = (ImageButton) findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(this);

        drawView.setBrushSize(mediumBrush);

        eraseBtn = (ImageButton) findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);

        currColor = currPaint.getTag().toString();

        newBtn = (ImageButton) findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);

        saveBtn = (ImageButton) findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);
    }

    public void dialogBuild(){
        final AlertDialog.Builder builder = new  AlertDialog.Builder(MainActivity.this);
        builder.setMessage("对方请求继续游戏");
        builder.setTitle("提示");
        builder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                drawView.startNew();
                //生成新的关键字发送给对手
                SharedVar.hint=hints[random.nextInt(hints.length)];
                //更新hint TextView
                tv.setText(SharedVar.hint);
                try {
                    SharedVar.hostOStream.writeUTF("hint:"+SharedVar.hint);
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
                    SharedVar.hostOStream.writeUTF("end");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                MainActivity.this.finish();
            }
        });
        builder.create().show();
    }

    public void paintClicked(View view) {
        //use chosen color
        drawView.setErase(false);
        drawView.setBrushSize(drawView.getLastBrushSize());
        if (view != currPaint) {
            //update color
            ImageButton imgView = (ImageButton) view;
            String color = view.getTag().toString();
            currColor = color;
            drawView.setColor(color);
            //被选中的颜色框变暗
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint = (ImageButton) view;
        }
    }

    @Override
    public void onClick(View view) {
        // respond to clicks
        if (view.getId() == R.id.draw_btn) {
            // draw button clicked
            drawView.setColor(currColor);
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Brush size:");
            brushDialog.setContentView(R.layout.brush_chooser);
            ImageButton smallBtn = (ImageButton) brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setBrushSize(smallBrush);
                    drawView.setLastBrushSize(smallBrush);
                    drawView.setErase(false);
                    brushDialog.dismiss();
                }
            });

            ImageButton mediumBtn = (ImageButton) brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setBrushSize(mediumBrush);
                    drawView.setLastBrushSize(mediumBrush);
                    drawView.setErase(false);
                    brushDialog.dismiss();
                }
            });

            ImageButton largeBtn = (ImageButton) brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setBrushSize(largeBrush);
                    drawView.setLastBrushSize(largeBrush);
                    drawView.setErase(false);
                    brushDialog.dismiss();
                }
            });

            brushDialog.show();
        } else if (view.getId() == R.id.erase_btn) {
            // switch to erase - choose size
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Erase size:");
            brushDialog.setContentView(R.layout.brush_chooser);
            ImageButton smallBtn = (ImageButton) brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(smallBrush);
                    drawView.setColor("#FFFFFF");
                    brushDialog.dismiss();
                }
            });

            ImageButton mediumBtn = (ImageButton) brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(mediumBrush);
                    drawView.setColor("#FFFFFF");
                    brushDialog.dismiss();
                }
            });

            ImageButton largeBtn = (ImageButton) brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(largeBrush);
                    drawView.setColor("#FFFFFF");
                    brushDialog.dismiss();
                }
            });

            brushDialog.show();
        } else if (view.getId() == R.id.new_btn) {
            // new button
            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            newDialog.setTitle("New drawing");
            newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
            newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    drawView.startNew();
                    dialog.dismiss();
                }
            });
            newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            newDialog.show();
        } else if (view.getId() == R.id.save_btn) {
            // save drawing
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
            saveDialog.setTitle("Save drawing");
            saveDialog.setMessage("Save drawing to device Gallery?");
            saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //save drawing
                    String imgSaved = MediaStore.Images.Media.insertImage(
                            getContentResolver(), drawView.getDrawingCache(),
                            UUID.randomUUID().toString() + ".png", "drawing");
                    if (imgSaved != null) {
                        Toast savedToast = Toast.makeText(getApplicationContext(),
                                "Drawing is saved to Gallery!", Toast.LENGTH_SHORT);
                        savedToast.show();
                    } else {
                        Toast unsavedToast = Toast.makeText(getApplicationContext(),
                                "Error! Image could not be saved!", Toast.LENGTH_SHORT);
                        unsavedToast.show();
                    }
                    drawView.destroyDrawingCache();
                }
            });
            saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            saveDialog.show();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            dialogBuildBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void dialogBuildBack(){
        AlertDialog.Builder builder = new  AlertDialog.Builder(this);
        builder.setMessage("确认退出？");
        builder.setTitle("提示");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                try {
                    SharedVar.hostOStream.writeUTF("end");
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                MainActivity.this.finish();
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
}
