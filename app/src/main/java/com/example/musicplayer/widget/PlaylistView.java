package com.example.musicplayer.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.musicplayer.Player;
import com.example.musicplayer.R;
import com.example.musicplayer.listener.OnItemSelectedListener;
import com.example.musicplayer.util.AudioModel;
import com.example.musicplayer.util.Playlist;

public class PlaylistView extends LinearLayout {
    static final String TAG = PlaylistView.class.getSimpleName();

    private final View playlistEmptyView;
    private final ListView recyclerView;
    private final PlaylistAdapter adapter;

    private Player player;
    private Playlist playlist;
    private OnItemSelectedListener listener;

    public PlaylistView(Context context) {
        this(context, null, 0);
    }
    public PlaylistView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public PlaylistView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_playlist, this, true);

        recyclerView = findViewById(R.id.RecyclerView);
        playlistEmptyView = findViewById(R.id.PlaylistEmptyView);

        adapter = new PlaylistAdapter(context);
        recyclerView.setAdapter(adapter);

        recyclerView.setOnItemClickListener((AdapterView<?> parent, View view, int pos, long id) -> {
            if (listener != null) {
                listener.onItemSelected(pos, playlist.get(pos));
            }
        });

    }

    public void update() {
        post(() -> {
            if (playlist.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                playlistEmptyView.setVisibility(View.VISIBLE);
            }
            else {
                recyclerView.setVisibility(View.VISIBLE);
                playlistEmptyView.setVisibility(View.GONE);

                adapter.notifyDataSetChanged();
            }
        });
    }
    public void setPlayer(Player player) {
        this.player = player;
    }

    public Playlist getPlaylist() {
        return playlist;
    }
    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.listener = listener;
    }

    private class PlaylistAdapter extends BaseAdapter {
        private final Context context;

        private PlaylistAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            if (playlist == null)
                return 0;

            return playlist.size();
        }
        @Override
        public Object getItem(int position) {
            return playlist.get(position);
        }
        @Override
        public long getItemId(int position) {
            return playlist.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            AudioModel audioModel = playlist.get(position);

            if (view == null) {
                view = View.inflate(context, R.layout.item_audio, null);
            }

            final TextView titleView = view.findViewById(R.id.SongTitleView);
            final TextView artistView = view.findViewById(R.id.SongArtistView);
            final AudioVisualizer audioVisualizerView = view.findViewById(R.id.AudioVisualizerView);

            titleView.setText(audioModel.getTitle());
            if (audioModel.getArtist() == null || audioModel.getArtist().length() == 0) {
                artistView.setText(R.string.default_artist);
            } else {
                artistView.setText(audioModel.getArtist());
            }

            if (audioModel.equals(player.getMusicQueue().getCurrent())) {
                audioVisualizerView.setVisibility(VISIBLE);

                if (player.getState() == Player.STATE_PLAYING) {
                    audioVisualizerView.startVisualize();
                } else {
                    audioVisualizerView.stopVisualize();
                }
            } else {
                audioVisualizerView.setVisibility(GONE);
                audioVisualizerView.stopVisualize();
            }

            return view;
        }
    }
}
