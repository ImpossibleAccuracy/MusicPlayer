package com.example.musicplayer.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.example.musicplayer.R;

public final class BackButton extends androidx.appcompat.widget.AppCompatImageView {
    public BackButton(Context context) {
        this(context, null, 0);
    }
    public BackButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public BackButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setImageResource(R.drawable.back_icon);
        setPadding(7, 7, 7, 7);

        setOnClickListener((View view) -> ((Activity)getContext()).onBackPressed());
    }
}
