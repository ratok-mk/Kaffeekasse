package com.zeiss.koch.kaffeekasse;

import android.media.MediaPlayer;
import android.content.Context;

public class SoundManager {

    private static SoundManager instance;
    MediaPlayer mediaPlayer;

    private SoundManager() {
    }

    public static SoundManager getInstance() {
        if (SoundManager.instance == null) {
            SoundManager.instance = new SoundManager();
        }
        return SoundManager.instance;
    }

    public void play(Context context, int resId) {
        if (this.mediaPlayer != null) {
            this.mediaPlayer.stop();
            this.mediaPlayer.release();
        }
        this.mediaPlayer = MediaPlayer.create(context, resId);
        this.mediaPlayer.start();
    }
}
