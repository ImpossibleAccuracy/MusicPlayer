package com.example.musicplayer.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.example.musicplayer.R;

public final class TopBarButton extends androidx.appcompat.widget.AppCompatButton {
    public static final int MODE_BTN_PLUS = 1;
    public static final int MODE_BTN_APPLY = 2;

    public TopBarButton(Context context) {
        this(context, null, 0);
    }
    public TopBarButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public TopBarButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBtnMode(MODE_BTN_PLUS);
    }

    public void setBtnMode(int mode) {
        switch (mode) {
            case MODE_BTN_PLUS:
                setBackgroundResource(R.drawable.btn_plus);
                break;
            case MODE_BTN_APPLY:
                setBackgroundResource(R.drawable.btn_apply);
                break;
        }
    }
}
