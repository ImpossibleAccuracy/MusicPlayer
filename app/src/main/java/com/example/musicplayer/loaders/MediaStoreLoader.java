package com.example.musicplayer.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.musicplayer.util.AudioModel;

@Deprecated
public class MediaStoreLoader extends Loader {
    public MediaStoreLoader(Context context) {
        super(context);
    }

    public MediaStoreLoader(Context context, int loaderType) {
        super(context, loaderType);
    }

    @Override
    protected void loadAudio() {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.ArtistColumns.ARTIST,
            MediaStore.Audio.AlbumColumns.ALBUM,
        };
        Cursor c = context.getContentResolver().query(uri, projection, null, null, null);

        if (c != null) {
            while (c.moveToNext()) {
                String title = c.getString(0);
                String path = c.getString(1);
                String artist = c.getString(2);
                String author = "";
                String album = c.getString(3);
                long duration = 0;
                boolean is_favorite = false;

                AudioModel track = getCallback().onTrackFound(
                    UNKNOWN_TRACK_ID,
                    title,
                    path,
                    artist,
                    author,
                    album,
                    duration,
                    is_favorite,
                    null
                );
                audio.add(track);
            }
            c.close();
        }
    }

    @Override
    protected void loadPlaylists() {

    }

    @Override
    protected void loadPlayer() {

    }
}
