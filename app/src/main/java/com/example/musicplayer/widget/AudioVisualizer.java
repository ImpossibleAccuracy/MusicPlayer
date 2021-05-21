package com.example.musicplayer.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.example.musicplayer.R;

public class AudioVisualizer extends View {
    private int linesCount;
    private final float lineWidth;
    private final float lineMargin;

    private boolean visualize;
    private final Paint paint = new Paint();

    public AudioVisualizer(Context context) {
        this(context, null, 0);
    }
    public AudioVisualizer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public AudioVisualizer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AudioVisualizer, 0, 0);
        int lineColor = a.getColor(R.styleable.AudioVisualizer_lineColor, Color.WHITE);
        lineWidth = a.getDimension(R.styleable.AudioVisualizer_lineWidth, 3);
        lineMargin = a.getDimension(R.styleable.AudioVisualizer_lineMargin, 3);
        a.recycle();

        paint.setColor(lineColor);

        post(() -> {
            setWillNotDraw(false);
            invalidate();
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        linesCount = (int) ((w - (getPaddingLeft() + getPaddingRight()) + lineMargin) / (lineWidth + lineMargin));
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final float paddingLeft = getPaddingLeft();
        final int height = getHeight() - 10 - getPaddingTop();

        if (linesCount == 0)
            return;

        for (int i = 0; i < linesCount; i++) {
            float x1 = (lineMargin * (i + 1)) + (lineWidth * i);
            float y1 = (float) (height - (Math.random() * height));
            float x2 = (lineMargin * (i + 1)) + (lineWidth * (i + 1));
            float y2 = getHeight() - getPaddingBottom();

            canvas.drawRect(
                paddingLeft + x1,
                y1,
                paddingLeft + x2,
                y2,
                paint
            );
        }
    }

    public void startVisualize() {
        if (!visualize) {
            visualize = true;
            new Thread(() -> {
                while (visualize) {
                    invalidate();

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
    public void stopVisualize() {
        visualize = false;
    }

    public int getLinesCount() {
        return linesCount;
    }
    public float getLineWidth() {
        return lineWidth;
    }
    public float getLineMargin() {
        return lineMargin;
    }
}
