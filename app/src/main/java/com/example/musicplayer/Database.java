package com.example.musicplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import com.example.musicplayer.util.AudioModel;
import com.example.musicplayer.util.Playlist;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class Database {
    static final String TAG = Database.class.getSimpleName();

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "MusicDB";

    public static final String TABLE_AUDIO = "audio";
    public static final String AUDIO_ID = "id";
    public static final String AUDIO_TITLE = "name";
    public static final String AUDIO_DURATION = "duration";
    public static final String AUDIO_PATH = "path";
    public static final String AUDIO_ARTIST = "artist";
    public static final String AUDIO_AUTHOR = "author";
    public static final String AUDIO_ALBUM = "album";
    public static final String AUDIO_ICON = "icon";
    public static final String AUDIO_IS_FAVORITE = "is_favorite";

    public static final String TABLE_PLAYLISTS = "playlists";
    public static final String PLAYLIST_ID = "id";
    public static final String PLAYLIST_NAME = "name";

    public static final String TABLE_AUDIO_PLAYLISTS = "audio_playlists";
    public static final String AUDIO_PLAYLISTS_ID = "id";
    public static final String AUDIO_PLAYLISTS_PLAYLIST_ID = "playlist_id";
    public static final String AUDIO_PLAYLISTS_AUDIO_ID = "audio_id";
    public static final String AUDIO_PLAYLISTS_AUDIO_POSITION = "audio_pos";

    private DBHelper helper;

    private Database(Context context) {
        helper = new DBHelper(context);
    }
    public void close() {
        helper.close();
    }
    public void reset(Context context) {
        SQLiteDatabase db = helper.getReadableDatabase();

        db.execSQL("DELETE FROM " + TABLE_AUDIO);
        db.execSQL("DELETE FROM " + TABLE_PLAYLISTS);
        db.execSQL("DELETE FROM " + TABLE_AUDIO_PLAYLISTS);
        db.close();

        helper = new DBHelper(context);
    }

    public boolean isEmpty() {
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor c = db.query(TABLE_AUDIO, null, null, null, null, null, null);
        int length = c.getCount();
        c.close();

        return (length == 0);
    }

    public Cursor getAudio() {
        SQLiteDatabase db = helper.getReadableDatabase();

        return db.query(
                TABLE_AUDIO, null, null, null, null, null, null);
    }
    public Cursor getPlaylists() {
        SQLiteDatabase db = helper.getReadableDatabase();

        return db.query(
            TABLE_PLAYLISTS, null, null, null, null, null, null);
    }
    public Cursor getPlaylistAudio(long playlistId) {
        SQLiteDatabase db = helper.getReadableDatabase();

        return db.query(
            TABLE_AUDIO_PLAYLISTS,
            new String[] { AUDIO_PLAYLISTS_AUDIO_ID },
            AUDIO_PLAYLISTS_PLAYLIST_ID + " = ?",
            new String[] {String.valueOf(playlistId)},
            null,
            null,
            AUDIO_PLAYLISTS_AUDIO_POSITION
        );
    }

    public long insertAudio(AudioModel a) {
        return insertAudio(a.getTitle(),
                a.getPath(),
                a.getArtist(),
                a.getAuthor(),
                a.getAlbum(),
                a.getDuration(),
                a.isFavorite(),
                a.getIcon());
    }
    public long insertAudio(String title,
                            String path,
                            String artist,
                            String author,
                            String album,
                            long duration,
                            boolean isFavorite,
                            @Nullable Bitmap icon) {
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AUDIO_TITLE, title);
        values.put(AUDIO_PATH, path);
        values.put(AUDIO_ARTIST, artist);
        values.put(AUDIO_AUTHOR, author);
        values.put(AUDIO_ALBUM, album);
        values.put(AUDIO_DURATION, duration);
        values.put(AUDIO_IS_FAVORITE, isFavorite);

        if (icon != null)
            values.put(AUDIO_ICON, bitmapToByte(icon));

        return db.insert(
            TABLE_AUDIO,
            null,
            values
        );
    }
    public long insertPlaylist(Playlist playlist) {
        return insertPlaylist(playlist.getName());
    }
    public long insertPlaylist(String name) {
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PLAYLIST_NAME, name);

        return db.insert(
            TABLE_PLAYLISTS,
            null,
            values
        );
    }
    public void insertAllAudio(List<AudioModel> audioModels) {
        for (AudioModel aModel : audioModels) {
            insertAudio(aModel);
        }
    }
    public void attachAudioToPlaylist(Playlist playlist, AudioModel track) {
        SQLiteDatabase db = helper.getWritableDatabase();

        Cursor c = db.query(
            TABLE_AUDIO_PLAYLISTS,
            new String[] { AUDIO_PLAYLISTS_AUDIO_ID },
            String.format("%s = ? AND %s = ?", AUDIO_PLAYLISTS_PLAYLIST_ID, AUDIO_PLAYLISTS_AUDIO_ID),
            new String[] {String.valueOf(playlist.id), String.valueOf(track.id)},
            null,
            null,
            null
        );
        boolean found = c.getCount() > 0;
        c.close();

        ContentValues values = new ContentValues();
        values.put(AUDIO_PLAYLISTS_AUDIO_POSITION, playlist.indexOf(track));
        if (found) {
            db.update(
                TABLE_AUDIO_PLAYLISTS,
                values,
                "id = ?",
                new String[] { String.valueOf(playlist.id) }
            );
        } else {
            values.put(AUDIO_PLAYLISTS_PLAYLIST_ID, playlist.id);
            values.put(AUDIO_PLAYLISTS_AUDIO_ID, track.id);
            db.insert(TABLE_AUDIO_PLAYLISTS, null, values);
        }
    }

    public void updateAudio(AudioModel audioModel) {
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AUDIO_TITLE, audioModel.getTitle());
        values.put(AUDIO_PATH, audioModel.getPath());
        values.put(AUDIO_ARTIST, audioModel.getArtist());
        values.put(AUDIO_AUTHOR, audioModel.getAuthor());
        values.put(AUDIO_ALBUM, audioModel.getAlbum());
        values.put(AUDIO_DURATION, audioModel.getDuration());
        values.put(AUDIO_IS_FAVORITE, audioModel.isFavorite());

        if (audioModel.getIcon() != null)
            values.put(AUDIO_ICON, bitmapToByte(audioModel.getIcon()));

        db.update(
                TABLE_AUDIO,
                values,
                "id = ?",
                new String[] { String.valueOf(audioModel.id) }
        );
    }
    public void updatePlaylist(Playlist playlist) {
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PLAYLIST_NAME, playlist.getName());

        db.update(
            TABLE_PLAYLISTS,
            values,
            "id = ?",
            new String[] { String.valueOf(playlist.id) }
        );

        if (!playlist.isEmpty()) {
            Cursor c = getPlaylistAudio(playlist.id);
            if (c != null) {
                while (c.moveToNext()) {
                    if (!playlist.contains(c.getInt(0))) {
                        db.delete(
                            TABLE_AUDIO_PLAYLISTS,
                            AUDIO_PLAYLISTS_AUDIO_ID + " = ?",
                            new String[]{String.valueOf(c.getInt(0))}
                        );
                    }
                }
                c.close();
            }

            for (AudioModel aModel : playlist) {
                attachAudioToPlaylist(playlist, aModel);
            }
        } else {
            db.delete(
                TABLE_AUDIO_PLAYLISTS,
                AUDIO_PLAYLISTS_PLAYLIST_ID + " = ?",
                new String[] { "" + playlist.id }
            );
        }
    }
    public void updateAllAudio(List<AudioModel> audioModels) {
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        for (AudioModel aModel : audioModels) {
            values.put(AUDIO_TITLE, aModel.getTitle());
            values.put(AUDIO_PATH, aModel.getPath());
            values.put(AUDIO_ARTIST, aModel.getArtist());
            values.put(AUDIO_AUTHOR, aModel.getAuthor());
            values.put(AUDIO_ALBUM, aModel.getAlbum());
            values.put(AUDIO_DURATION, aModel.getDuration());
            values.put(AUDIO_IS_FAVORITE, aModel.isFavorite());

            if (aModel.getIcon() != null)
                values.put(AUDIO_ICON, bitmapToByte(aModel.getIcon()));

            db.update(
                TABLE_AUDIO,
                values,
                "id = ?",
                new String[] { String.valueOf(aModel.id) }
            );
        }
    }

    public void deleteAudio(int id) {
        SQLiteDatabase db = helper.getWritableDatabase();

        db.delete(
                TABLE_AUDIO,
                AUDIO_ID + " = ?",
                new String[] { "" + id }
        );
    }
    public void deletePlaylist(Playlist playlist) {
        SQLiteDatabase db = helper.getWritableDatabase();

        db.delete(
            TABLE_PLAYLISTS,
            PLAYLIST_ID + " = ?",
            new String[] { "" + playlist.id }
        );

        db.delete(
            TABLE_AUDIO_PLAYLISTS,
            AUDIO_PLAYLISTS_PLAYLIST_ID + " = ?",
            new String[] { "" + playlist.id }
        );
    }

    @Deprecated
    public long getRowsCount(String table) {
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor c = db.query(table, null, null, null, null, null, null);
        int length = c.getCount();
        c.close();

        return length;
    }

    private byte[] bitmapToByte(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        return bos.toByteArray();
    }

    private static volatile Database instance;
    public static Database getInstance(Context context) {
        Database result = instance;
        if (result != null) {
            return result;
        }
        synchronized(Database.class) {
            if (instance == null) {
                instance = new Database(context);
            }
            return instance;
        }
    }

    protected static class DBHelper extends SQLiteOpenHelper {
        private DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String CREATE_AUDIO_TABLE = "create table " + TABLE_AUDIO + " ("
                    + AUDIO_ID + " integer PRIMARY KEY AUTOINCREMENT NOT NULL,"
                    + AUDIO_TITLE + " text NOT NULL,"
                    + AUDIO_PATH + " text NOT NULL,"
                    + AUDIO_DURATION + " integer NOT NULL,"
                    + AUDIO_ARTIST + " text,"
                    + AUDIO_AUTHOR + " text,"
                    + AUDIO_ALBUM + " text,"
                    + AUDIO_ICON + " blob,"
                    + AUDIO_IS_FAVORITE + " boolean NOT NULL);";


            String CREATE_PLAYLIST_TABLE = "create table " + TABLE_PLAYLISTS + " ("
                    + PLAYLIST_ID + " integer PRIMARY KEY AUTOINCREMENT NOT NULL,"
                    + PLAYLIST_NAME + " text NOT NULL" + ");";


            String CREATE_AUDIO_PLAYLISTS_TABLE = "create table " + TABLE_AUDIO_PLAYLISTS + " ("
                    + AUDIO_PLAYLISTS_ID + " integer PRIMARY KEY AUTOINCREMENT NOT NULL,"
                    + AUDIO_PLAYLISTS_PLAYLIST_ID + " integer NOT NULL,"
                    + AUDIO_PLAYLISTS_AUDIO_ID + " integer NOT NULL,"
                    + AUDIO_PLAYLISTS_AUDIO_POSITION + " integer NOT NULL" + ");";

            db.execSQL(CREATE_AUDIO_TABLE);
            db.execSQL(CREATE_PLAYLIST_TABLE);
            db.execSQL(CREATE_AUDIO_PLAYLISTS_TABLE);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_AUDIO);
        }
    }
}
