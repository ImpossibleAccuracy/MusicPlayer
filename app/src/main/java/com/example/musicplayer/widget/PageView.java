package com.example.musicplayer.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.example.musicplayer.R;

public class PageView extends View {
    private int pagesCount;
    private int selectedPage;
    private final int circleColor;
    private final int selectedCircleColor;
    private final float circleRadius;
    private final float circleMargins;
    private final Paint paint = new Paint();

    public PageView(Context context) {
        this(context, null, 0);
    }
    public PageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public PageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setPagesCount(0);
        setCurrentPage(0);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PageView, 0, 0);
        circleColor = a.getColor(R.styleable.PageView_circle_color, Color.RED);
        circleRadius = a.getDimension(R.styleable.PageView_circle_radius, 5);
        circleMargins = a.getDimension(R.styleable.PageView_circle_margins, 2);
        selectedCircleColor = a.getColor(R.styleable.PageView_selected_circle_color, Color.YELLOW);
        a.recycle();

        paint.setStrokeWidth(2);
        paint.setAntiAlias(true);
        paint.setHinting(Paint.HINTING_ON);

        setWillNotDraw(false);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final float startX = (float) (getWidth() / 2.0 - getAllCirclesWidth() / 2);

        for (int i = 0; i < getPagesCount(); i++) {
            if (i == selectedPage - 1) {
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                paint.setColor(selectedCircleColor);
            }
            else {
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(circleColor);
            }

            canvas.drawCircle(
                startX + getCircleX(i + 1),
                circleRadius,
                circleRadius - 1,
                paint
            );
        }
    }

    private float getAllCirclesWidth() {
        return ((circleMargins * getPagesCount()) + (circleRadius * 2 * getPagesCount()) - circleRadius);
    }
    private float getCircleX(int circle) {
        return (circleMargins * circle) + (circleRadius * 2 * circle) - circleRadius;
    }

    public void setCurrentPage(int page) {
        selectedPage = page;
        invalidate();
    }
    public int getPagesCount() {
        return pagesCount;
    }
    public void setPagesCount(int pagesCount) {
        this.pagesCount = pagesCount;
        invalidate();
    }
}
