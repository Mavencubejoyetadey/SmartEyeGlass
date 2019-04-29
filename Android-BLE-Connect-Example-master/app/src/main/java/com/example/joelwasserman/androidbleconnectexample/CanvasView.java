package com.example.joelwasserman.androidbleconnectexample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Random;

public class CanvasView extends View {
    public Paint mPaint;
    public Paint mPaint1;
    public Paint mPaint2;
    public Paint mPaint3;
    public Paint mPaint4;
    public Paint mPaint5;

    public static Canvas mCanvas;
    private int mPivotX = 100;
    private int mPivotY = 100;
    private int radius = 100;

    //constructor
    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint1 = new Paint();
        mPaint2 = new Paint();
        mPaint3 = new Paint();
        mPaint4 = new Paint();
        mPaint5 = new Paint();
        mPaint4.setColor(ContextCompat.getColor(context,R.color.colorBackgroundSmallLayout));
        mPaint4.setStyle(Paint.Style.FILL);
        mPaint3.setColor(ContextCompat.getColor(context,R.color.colorBackgroundLayout));
        mPaint3.setStyle(Paint.Style.FILL);
        mPaint.setColor(ContextCompat.getColor(context,R.color.circle2));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(4);

        mPaint1.setColor(ContextCompat.getColor(context,R.color.circle1));
        mPaint1.setStyle(Paint.Style.FILL);
        mPaint1.setAntiAlias(true);
        mPaint1.setStrokeWidth(4);

        mPaint5.setColor(ContextCompat.getColor(context,R.color.circleoutline));
        mPaint5.setStyle(Paint.Style.STROKE);
        mPaint5.setAntiAlias(true);
        mPaint5.setStrokeWidth(5);

        mPaint2.setColor(ContextCompat.getColor(context,R.color.circle3));
        mPaint2.setStyle(Paint.Style.FILL);
        mPaint2.setAntiAlias(true);
        mPaint2.setStrokeWidth(4);

    }

    public void drawCircle(int radius1) {
        radius  = radius1;

        invalidate();

    }

    //what I want to draw is here
    protected void onDraw(Canvas canvas) {
        mCanvas = canvas;
        super.onDraw(mCanvas);












        RectF bounds = new RectF(canvas.getClipBounds());
        float centerX = bounds.centerX();
        float centerY = bounds.centerY();
        canvas.drawRect(0f, 0f, canvas.getWidth()-10,canvas.getHeight()-10, mPaint3);
        canvas.drawRect(30.0f, 30.0f, canvas.getWidth()-30,canvas.getHeight()-30, mPaint4);

        canvas.drawCircle(centerX, centerY, 120*3, mPaint1);
        canvas.drawCircle(centerX, centerY, radius*3, mPaint);
        canvas.drawCircle(centerX, centerY, 20*3, mPaint2);
        canvas.drawCircle(centerX, centerY, 120*3, mPaint5);

        invalidate();
    }
}
