package com.example.musicplayer;

public final class Config {
    public static final String[] FileExtensions;
    public static final boolean NEED_UPDATE = false;
    public static final long MINIMAL_TRACK_DURATION = 45000;
    public static final String MUSIC_QUEUE_DELIMITER = ",";
    public static final String PREFERENCES_NAME = "PlayerPreferences";

    static {
        FileExtensions = new String[]{
            "mp3",
            "m4a",
            "3gp",
            "aac",
            "3gp",
            "amr",
            "flac",
            "ota",
            "mid",
            "ogg",
            "mkv",
            "wav",
            "imy",
            "rtttl",
            "xmf",
        };
    }

    private Config() { /**/ }
}
