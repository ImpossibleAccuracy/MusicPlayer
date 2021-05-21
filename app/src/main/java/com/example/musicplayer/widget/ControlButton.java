package com.example.musicplayer.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.example.musicplayer.Player;
import com.example.musicplayer.R;

public class ControlButton extends androidx.appcompat.widget.AppCompatImageView {
    private String btn_color;

    private int state;

    public ControlButton(Context context) {
        this(context, null, 0);
    }
    public ControlButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public ControlButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ControlButton, 0, 0);
        btn_color = a.getString(R.styleable.ControlButton_btn_color);
        a.recycle();

        if (btn_color == null) btn_color = "default";
        setIconPlay();
    }

    public void setIconPlay() {
        switch (btn_color) {
            case "":
            case "default":
                setImageResource(R.drawable.btn_play_normal);
                break;
            case "light":
                setImageResource(R.drawable.btn_play_light);
                break;
            case "dark":
                setImageResource(R.drawable.btn_play_dark);
                break;
        }
    }
    public void setIconPause() {
        switch (btn_color) {
            case "":
            case "default":
                setImageResource(R.drawable.btn_pause_normal);
                break;
            case "light":
                setImageResource(R.drawable.btn_pause_light);
                break;
            case "dark":
                setImageResource(R.drawable.btn_pause_dark);
                break;
        }
    }

    public void update(int state) {
        if (this.state == state)
            return;

        this.state = state;
        if (state != Player.STATE_PLAYING)
            setIconPlay();
        else
            setIconPause();
    }
}
