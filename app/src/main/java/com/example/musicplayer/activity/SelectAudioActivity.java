package com.example.musicplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.example.musicplayer.Player;
import com.example.musicplayer.R;
import com.example.musicplayer.util.AudioModel;
import com.example.musicplayer.util.MusicRepository;
import com.example.musicplayer.util.Playlist;
import com.example.musicplayer.widget.BackButton;
import com.example.musicplayer.widget.PlaybarLayout;
import com.example.musicplayer.widget.TopBarButton;

import java.util.LinkedList;
import java.util.List;

public class SelectAudioActivity extends AppActivity implements View.OnClickListener {
    private TextView pageNameView;
    private BackButton backButton;
    private TopBarButton addButton;
    private ListView selectAudioView;

    private List<AudioModel> selected;
    private List<AudioModel> allAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_audio);
        setWindowMode(MODE_LIGHT);

        MusicRepository musicRepository = getMusicRepository();
        allAudio = musicRepository.getAllAudio();
        selected = new LinkedList<>();

        Intent intent = getIntent();
        if (intent != null) {
            long pid = intent.getLongExtra(Playlist.class.getSimpleName(), Integer.MIN_VALUE);
            Playlist playlist = musicRepository.getPlaylistById(pid);
            selected.addAll(playlist);

            SelectAudioAdapter adapter = new SelectAudioAdapter();
            selectAudioView.setAdapter(adapter);
        }
    }

    @Override
    protected void findUI() {
        addButton = findViewById(R.id.AddButton);
        backButton = findViewById(R.id.BackButton);
        pageNameView = findViewById(R.id.PageNameView);
        selectAudioView = findViewById(R.id.SelectAudioView);
        addButton.setOnClickListener(this);
    }

    @Override
    protected void fillUI() {
        pageNameView.setText(R.string.title_select_audio);
        backButton.setImageResource(R.drawable.btn_close);
        addButton.setBtnMode(TopBarButton.MODE_BTN_APPLY);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.AddButton) {
            long[] ids = new long[selected.size()];
            for (int i = 0; i < ids.length; i++) {
                ids[i] = selected.get(i).id;
            }

            Intent result = new Intent();
            result.putExtra("Selected", ids);
            setResult(RESULT_OK, result);
            finish();
        }
    }

    protected class SelectAudioAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            if (allAudio == null)
                return 0;

            return allAudio.size();
        }
        @Override
        public Object getItem(int position) {
            return allAudio.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            AudioModel aModel = (AudioModel) getItem(position);

            if (view == null) {
                view = View.inflate(SelectAudioActivity.this, R.layout.item_aduio_selectable, null);
            }

            TextView songTitle = view.findViewById(R.id.SongTitleView);
            TextView songAuthorView = view.findViewById(R.id.SongArtistView);
            CheckBox checkBox = view.findViewById(R.id.CheckBox);

            songTitle.setText(aModel.getTitle());
            songAuthorView.setText(aModel.getArtist());
            checkBox.setChecked(selected.contains(aModel));

            checkBox.setOnClickListener((View v) -> {
                if (selected.contains(aModel))
                    selected.remove(aModel);
                else
                    selected.add(aModel);
            });

            return view;
        }
    }
}
