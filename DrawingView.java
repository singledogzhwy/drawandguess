package com.lilwulin.team.drawguess;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONObject;

import java.io.IOException;


/**
 * TODO: document your custom view class.
 */

// 对应画图的View
public class DrawingView extends View {
    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    protected int paintColor = 0xFF660000;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;

    // brush size
    private float brushSize, lastBrushSize;

    private Context ctx;

    private boolean erase = false;

    int width;
    int height;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
//        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
//        Display display = wm.getDefaultDisplay();
//        width = display.getWidth();
//        height = display.getHeight();
        setupDrawing();
    }

    private void setupDrawing() {
//        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
//        Display display = wm.getDefaultDisplay();
//        int width = display.getWidth();
//        int height = display.getHeight();
//        this.onSizeChanged(350, 400, this.getWidth(), this.getHeight());
        brushSize = getResources().getInteger(R.integer.medium_size);
        lastBrushSize = brushSize;
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
        SharedVar.host_width = (double) w;
        SharedVar.host_height = (double) h;
        try {
            SharedVar.hostOStream.writeDouble(SharedVar.host_width);
            SharedVar.hostOStream.writeDouble(SharedVar.host_height);
            SharedVar.hostOStream.writeUTF(SharedVar.hint);
        } catch (IOException e) {
            e.printStackTrace();
        }


//        SharedVar.scale = ((double) w) / 700.0;
        canvasBitmap = Bitmap.createBitmap(w, w, Bitmap.Config.ARGB_8888);
        Log.e("wwwwwww, hhhhhh", "" + w + " " + h);
//        canvasBitmap = Bitmap.createScaledBitmap(canvasBitmap, w, h, true);
        drawCanvas = new Canvas(canvasBitmap);
        //this.setLayoutParams(new LinearLayout.LayoutParams(w,w));
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //detect user touch
        float touchX = event.getX();
        float touchY = event.getY();
        JSONObject json = new JSONObject();
        int action = event.getAction();
        try {
            json.put("action", action);
            json.put("color", paintColor);
            json.put("size", brushSize);
            json.put("x", touchX);
            json.put("y", touchY);
        } catch (Exception e) {
            Log.e("json", e.toString());
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                break;
            default:
                return false;
        }

        try {
            SharedVar.hostOStream.writeUTF(json.toString());
            Log.e("send json", json.toString());
        } catch (IOException e) {
            Log.e("json send", e.toString());
        }

        invalidate(); //invalidate the view will cause onDraw to execute
        return true;
    }

    public void setColor(String newColor) {
        //set color
        invalidate();
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
    }

    public void setBrushSize(float newSize) {
        // update size
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, getResources().getDisplayMetrics());
        brushSize = pixelAmount;
        drawPaint.setStrokeWidth(brushSize);
    }

    public void setLastBrushSize(float lastSize) {
        lastBrushSize = lastSize;
    }

    public float getLastBrushSize() {
        return lastBrushSize;
    }

    public void setErase(boolean isErase) {
        //set erase true or false
        erase = isErase;
        if (erase) {
            drawPaint.setColor(0xFFFFFFFF);
        }
    }

    public void startNew() {
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        try {
            SharedVar.hostOStream.writeUTF("clear");
        } catch (IOException e) {
            Log.e("send clear", e.toString());
        }
        invalidate();
    }
}
