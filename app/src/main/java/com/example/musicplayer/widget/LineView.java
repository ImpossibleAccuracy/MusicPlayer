package com.example.musicplayer.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.example.musicplayer.R;

public final class LineView extends View {
    public LineView(Context context) {
        this(context, null, 0);
    }
    public LineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public LineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackgroundResource(R.color.lineColor);
    }
}
