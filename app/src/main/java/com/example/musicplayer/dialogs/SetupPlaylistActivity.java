package com.example.musicplayer.dialogs;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayer.R;

public class SetupPlaylistActivity extends AppCompatActivity {
    public static final int RENAME_PLAYLIST_KEY = 1;
    public static final int DELETE_PLAYLIST_KEY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_setup_playlist);
        setFinishOnTouchOutside(false);

        Window window = getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

        Button rename = findViewById(R.id.rename);
        Button delete = findViewById(R.id.delete);

        rename.setOnClickListener((View view) -> {
            Intent intent = new Intent();
            intent.putExtra("result", RENAME_PLAYLIST_KEY);
            setResult(RESULT_OK, intent);
            finish();
        });
        delete.setOnClickListener((View view) -> {
            Intent intent = new Intent();
            intent.putExtra("result", DELETE_PLAYLIST_KEY);
            setResult(RESULT_OK, intent);
            finish();
        });
    }
}
