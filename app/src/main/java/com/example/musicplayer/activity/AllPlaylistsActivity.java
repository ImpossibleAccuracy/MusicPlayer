package com.example.musicplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.musicplayer.Database;
import com.example.musicplayer.Player;
import com.example.musicplayer.R;
import com.example.musicplayer.dialogs.CreatePlaylistActivity;
import com.example.musicplayer.dialogs.SetupPlaylistActivity;
import com.example.musicplayer.listener.Callback;
import com.example.musicplayer.util.AudioModel;
import com.example.musicplayer.util.MusicRepository;
import com.example.musicplayer.util.Playlist;
import com.example.musicplayer.widget.PlaybarLayout;

import java.util.ArrayList;
import java.util.List;

public class AllPlaylistsActivity extends AppActivity implements View.OnClickListener {
    public static final int CREATE_PLAYLIST_REQUEST_CODE = 1;
    public static final int RENAME_PLAYLIST_REQUEST_CODE = 2;
    public static final int SETUP_PLAYLIST_REQUEST_CODE = 3;

    private TextView pageNameView;
    private ListView playlistsView;
    private PlaybarLayout playbarLayout;
    private LinearLayout noPlaylistsView;

    private PlaylistsAdapter adapter;
    private Playlist selectedPlaylist;
    private List<Playlist> allPlaylists;

    private final Callback updatePlaybarCallback = new Callback() {
        @Override
        public void onPlayerStateChanged(Player player, int state) {
            playbarLayout.update(player.getMusicQueue().getCurrent(), state);
        }

        @Override
        public void onCurrentAudioChanged(Player player, AudioModel playingAudio) {
            playbarLayout.update(playingAudio, player.getState());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_playlists);
        setWindowMode(MODE_LIGHT);

        synchronized (getMusicRepository()) {
            allPlaylists = new ArrayList<>();
            for (Playlist p : getMusicRepository().getPlaylists())
                if (p.id != MusicRepository.ALL_AUDIO_PLAYLIST_ID &&
                        p.id != MusicRepository.FAVORITES_PLAYLIST_ID &&
                        p.id != MusicRepository.RECENT_OPENED_PLAYLIST_ID)
                    allPlaylists.add(p);
        }

        adapter = new PlaylistsAdapter();
        playlistsView.setAdapter(adapter);

        getPlayer().registerCallback(updatePlaybarCallback);

        new UpdateSeekBarThread().start();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == CREATE_PLAYLIST_REQUEST_CODE) {
                createPlaylistFromIntent(data);
            }
            else if (requestCode == SETUP_PLAYLIST_REQUEST_CODE) {
                int result = data.getIntExtra("result", 0);

                if (result == SetupPlaylistActivity.RENAME_PLAYLIST_KEY) {
                    startActivityForResult(
                        new Intent(this, CreatePlaylistActivity.class), RENAME_PLAYLIST_REQUEST_CODE);
                }
                else if (result == SetupPlaylistActivity.DELETE_PLAYLIST_KEY) {
                    deleteSelectedPlaylist();
                }
            }
            else if (requestCode == RENAME_PLAYLIST_REQUEST_CODE) {
                renamePlaylistFromIntent(data);
            }

        }
    }
    @Override
    public void onClick(View v) {
        Player player = getPlayer();
        if (v.getId() == R.id.AddButton) {
            startActivityForResult(
                new Intent(this, CreatePlaylistActivity.class), CREATE_PLAYLIST_REQUEST_CODE);
        }
        else if (v.getId() == R.id.ControlButton) {
            if (player.getState() == Player.STATE_PLAYING)
                player.stop();
            else
                player.play();
        }
        else if (v.getId() == R.id.NextSongButton) {
            player.skipToNext();
        }
        else if (v.getId() == R.id.PreviousSongButton) {
            player.skipToPrevious();
        }
    }

    @Override
    protected void findUI() {
        pageNameView = findViewById(R.id.PageNameView);
        playlistsView = findViewById(R.id.PlaylistsView);
        playbarLayout = findViewById(R.id.PlaybarLayout);
        noPlaylistsView = findViewById(R.id.NoPlaylistsView);

        playlistsView.setOnItemClickListener((AdapterView<?> p, View v, int pos, long id) -> {
            Intent intent = new Intent(AllPlaylistsActivity.this, PlaylistActivity.class);
            intent.putExtra("PlaylistId", (int) id);
            startActivity(intent);
        });
        playlistsView.setOnItemLongClickListener((AdapterView<?> p, View v, int pos, long id) -> {
            selectedPlaylist = allPlaylists.get(pos);
            startActivityForResult(new Intent(this, SetupPlaylistActivity.class), SETUP_PLAYLIST_REQUEST_CODE);
            return true;
        });

        findViewById(R.id.AddButton).setOnClickListener(this);

        playbarLayout.setOnControlButtonClickListener(this);
        playbarLayout.setOnNextSongButtonClickListener(this);
        playbarLayout.setOnPreviousSongButtonClickListener(this);
    }
    @Override
    protected void fillUI() {
        pageNameView.setText(R.string.your_playlists);
        updateMain();

        Player player = getPlayer();
        playbarLayout.update(player.getMusicQueue().getCurrent(),
            player.getState());
        playbarLayout.updateSeekBar(player.getTime(),
            player.getMusicQueue().getCurrent().getDuration());
    }
    private void updateMain() {
        if (allPlaylists.isEmpty()) {
            playlistsView.setVisibility(View.GONE);
            noPlaylistsView.setVisibility(View.VISIBLE);
        }
        else {
            playlistsView.setVisibility(View.VISIBLE);
            noPlaylistsView.setVisibility(View.GONE);

            adapter.notifyDataSetChanged();
        }
    }

    private void createPlaylistFromIntent(Intent data) {
        String playlistName = data.getStringExtra("PlaylistName");
        long id = Database.getInstance(this).insertPlaylist(playlistName);

        Playlist playlist = new Playlist(id, playlistName);
        getMusicRepository().addPlaylist(playlist);
        allPlaylists.add(playlist);

        updateMain();
    }
    private void renamePlaylistFromIntent(Intent data) {
        String playlistName = data.getStringExtra("PlaylistName");
        selectedPlaylist.setName(playlistName);

        updateMain();

        Database.getInstance(this).updatePlaylist(selectedPlaylist);

        selectedPlaylist = null;
    }
    private void deleteSelectedPlaylist() {
        getMusicRepository().removePlaylist(selectedPlaylist);
        allPlaylists.remove(selectedPlaylist);

        updateMain();

        Database.getInstance(this).deletePlaylist(selectedPlaylist);

        selectedPlaylist = null;
    }

    public class PlaylistsAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return allPlaylists.size();
        }
        @Override
        public Object getItem(int position) {
            return allPlaylists.get(position);
        }
        @Override
        public long getItemId(int position) {
            return allPlaylists.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            Playlist playlist = allPlaylists.get(position);

            if (view == null) {
                view = View.inflate(AllPlaylistsActivity.this, R.layout.item_playlist, null);
            }

            TextView playlistTitleView = view.findViewById(R.id.PlaylistTitleView);
            TextView playlistSizeView = view.findViewById(R.id.PlaylistSizeView);

            playlistTitleView.setText(playlist.getName());
            playlistSizeView.setText(String.valueOf(playlist.size()));

            return view;
        }
    }

    public class UpdateSeekBarThread extends Thread {
        @Override
        public void run() {
            setName("UpdateSeekBarThread");

            Player player = getPlayer();
            while (activityExist()) {
                try {
                    playbarLayout.updateSeekBar(player.getTime(), player.getMusicQueue().getCurrent().getDuration());

                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
