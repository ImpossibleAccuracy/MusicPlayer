package com.example.musicplayer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.listener.OnItemSelectedListener;
import com.example.musicplayer.util.AudioModel;
import com.example.musicplayer.R;

import java.util.List;

public class PlaylistFragment extends Fragment {
    static final String TAG = PlaylistFragment.class.getSimpleName();

    private View playlistEmptyView;
    private RecyclerView playlistView;
    protected PlaylistAdapter adapter;

    private List<AudioModel> playlist;
    private OnItemSelectedListener listener;

    public void setPlaylist(List<AudioModel> playlist) {
        this.playlist = playlist;

        if (playlistView != null) {
            if (playlist.size() == 0) {
                playlistView.setVisibility(View.GONE);
                playlistEmptyView.setVisibility(View.VISIBLE);
            }
            else {
                playlistView.setVisibility(View.VISIBLE);
                playlistEmptyView.setVisibility(View.GONE);

                adapter.notifyDataSetChanged();
            }
        }
    }
    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_playlist, container, false);

        playlistView = root.findViewById(R.id.PlaylistView);
        playlistEmptyView = root.findViewById(R.id.PlaylistEmptyView);

        adapter = new PlaylistAdapter(getContext());
        // DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);

        playlistView.setAdapter(adapter);
        // playlistView.addItemDecoration(itemDecorator);

        if (playlist != null)
            setPlaylist(playlist);

        return root;
    }

    public class PlaylistAdapter
            extends RecyclerView.Adapter<PlaylistFragment.PlaylistAdapter.ViewHolder> {

        private final LayoutInflater inflater;

        public PlaylistAdapter(Context context) {
            this.inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public PlaylistFragment.PlaylistAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.item_audio, parent, false);

            return new ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(PlaylistFragment.PlaylistAdapter.ViewHolder holder, int position) {
            holder.bind(position);
        }
        @Override
        public int getItemCount() {
            return playlist.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView songTitleView;
            private final TextView songArtistView;

            private int position;
            private AudioModel audioModel;

            public ViewHolder(View view) {
                super(view);

                view.setOnClickListener((View v) -> {
                    if (listener != null)
                        listener.onItemSelected(position, audioModel);
                });

                songTitleView = view.findViewById(R.id.SongTitleView);
                songArtistView = view.findViewById(R.id.SongArtistView);;
            }

            public void bind(int pos) {
                position = pos;
                audioModel = playlist.get(pos);

                songTitleView.setText(audioModel.getTitle());
                if (audioModel.getArtist() == null || audioModel.getArtist().length() == 0) {
                    songArtistView.setText(R.string.default_artist);
                } else {
                    songArtistView.setText(audioModel.getArtist());
                }
            }
        }
    }
}
