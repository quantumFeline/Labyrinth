package com.example.veronika.ball;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by veronika on 15/04/2017.
 */

public class Drawer extends View {
    Ball ball = null;
    Labyrinth labyrinth = null;
    float[] values;

    public Drawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i("trace", "Drawer()");
        ball = new Ball();
    }

    void coordChange () {
        values = MainActivity.pc.valuesAccel;
        //Log.i("Drawer.coordChange", String.format("%.3f %.3f", values[0], values[1]));
        ball.coordChange(values[0], values[1]);
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        //Log.i("trace", "onDraw() called");
        int width = getWidth();
        int height = getHeight();
        super.onDraw(canvas);
        ball.draw(canvas, width, height);
        labyrinth.draw(canvas, ball, width, height);
    }
}
