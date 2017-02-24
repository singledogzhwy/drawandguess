package com.lilwulin.team.drawguess;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by wulin on 15/4/13.
 */

public class GuestWatchView extends View {

    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    protected int paintColor = 0xFF660000;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;

    int height;
    int width;

    // brush size
    private float brushSize;
    private Context ctx;

    public GuestWatchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
        new Thread(new GuestWatchRunnable()).start();
        setupDrawing();
    }

    public GuestWatchView(Context context) {
        super(context);
        ctx = context;
        new Thread(new GuestWatchRunnable()).start();
        setupDrawing();
    }

    private void setupDrawing() {
//        Log.e("width, height:", "" + width + " " + height);
//        this.onSizeChanged(width, height, this.getWidth(), this.getHeight());
        brushSize = getResources().getInteger(R.integer.medium_size);
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
        setDrawingCacheEnabled(true);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //view given size
        super.onSizeChanged(w, h, oldw, oldh);
        SharedVar.scale = ((double) w) / SharedVar.host_width;
        canvasBitmap = Bitmap.createBitmap(w, w, Bitmap.Config.ARGB_8888);
        Log.e("wwwwwww, hhhhhh", "" + w + " " + h);
        drawCanvas = new Canvas(canvasBitmap);
        //double s = SharedVar.host_height / SharedVar.host_width;
        //double heightDouble = (double)(w)*s;
        //this.setLayoutParams(new LinearLayout.LayoutParams(w, w));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec,widthMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //draw view
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    class GuestWatchRunnable implements Runnable {

        @Override
        public void run() {
            String drawJsonStr = null;
            while (true) {
                try {
                    drawJsonStr = SharedVar.guestDIStream.readUTF();
                    if (drawJsonStr.equals("end")) {
                        ((Activity) ctx).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ctx,"对方退出了游戏",Toast.LENGTH_LONG).show();
                                ((Activity) ctx).finish();
                            }
                        });
                        return;
                    }
                } catch (IOException e) {
                    Log.e("get json", e.toString());
                    return;
                }

                if (drawJsonStr.equals("clear")) {
                    ((Activity) ctx).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
                            invalidate();
                        }
                    });
                    continue;
                }

                if (drawJsonStr.contains("hint:")) {
                    SharedVar.answer = drawJsonStr.substring(5);
                    ((GuestWatchActivity) ctx).runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    ((GuestWatchActivity) ctx).rebuilt();
                                }
                            }
                    );
                    continue;
                }


//                Log.e("HUAWEI JSON INFO", drawJsonStr);
                JSONObject drawJson = null;
                try {
                    drawJson = new JSONObject(drawJsonStr);
                    Log.e("HUAWEI JSON INFO", drawJsonStr);
                } catch (JSONException e) {
                    Log.e("client json creation", e.toString());
                }
                Log.v("yqf", "JSon " + drawJsonStr);

                int paintColor;
                final int action;
                final double brushSize;
                final double x;
                final double y;
                try {
                    x = drawJson.getDouble("x") * SharedVar.scale;
                    y = drawJson.getDouble("y") * SharedVar.scale;
                    paintColor = drawJson.getInt("color");
                    action = drawJson.getInt("action");
                    brushSize = drawJson.getDouble("size") * SharedVar.scale;
                } catch (JSONException e) {
                    Log.e("json parse", e.toString());
                    return;
                }
                drawPaint.setColor(paintColor);
                drawPaint.setStrokeWidth((float) brushSize);

                ((Activity) ctx).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                drawPath.moveTo((float) x, (float) y);
                                break;
                            case MotionEvent.ACTION_MOVE:
                                drawPath.lineTo((float) x, (float) y);
                                break;
                            case MotionEvent.ACTION_UP:
                                drawCanvas.drawPath(drawPath, drawPaint);
                                drawPath.reset();
                                break;
                            default:
                                return;
                        }
//                        Log.e("get json on UI", "x:" + x + ", y:" + y + " size:" + brushSize);
                        invalidate();
                    }
                });
            }
        }
    }
}
