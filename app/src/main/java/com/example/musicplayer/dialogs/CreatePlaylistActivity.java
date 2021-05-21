package com.example.musicplayer.dialogs;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayer.MusicApplication;
import com.example.musicplayer.util.MusicRepository;
import com.example.musicplayer.R;
import com.example.musicplayer.util.Playlist;
import com.google.android.material.textfield.TextInputLayout;

public class CreatePlaylistActivity extends AppCompatActivity {
    public static final int MIN_NAME_LENGTH = 1;
    public static final int MAX_NAME_LENGTH = 50;

    private Button apply;
    private Button cancel;
    private TextInputLayout editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_playlist);
        setFinishOnTouchOutside(false);

        Window window = getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

        setTitle("Create new playlist");

        apply = findViewById(R.id.apply);
        cancel = findViewById(R.id.cancel);
        editText = findViewById(R.id.textfield);

        apply.setOnClickListener((View view) -> {
            String text = editText.getEditText().getText().toString().trim();

            if (text.length() <= MIN_NAME_LENGTH || text.length() > MAX_NAME_LENGTH) {
                editText.setErrorEnabled(true);
            }
            else {
                MusicRepository musicRepository = ((MusicApplication)getApplication()).getMusicRepository();
                for (Playlist p : musicRepository.getPlaylists()) {
                    if (p.getName().equals(text)) {
                        editText.setErrorEnabled(true);
                        return;
                    }
                }

                editText.setErrorEnabled(false);

                Intent intent = new Intent();
                intent.putExtra("PlaylistName", text);

                setResult(RESULT_OK, intent);
                finish();
            }
        });
        cancel.setOnClickListener((View view) -> {
            setResult(RESULT_CANCELED);
            finish();
        });
        editText.setOnFocusChangeListener((View view, boolean hasFocus) -> {
            if (!hasFocus) {
                apply.performClick();
            }
        });
    }

}
