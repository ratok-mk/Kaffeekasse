package com.zeiss.koch.kaffeekasse;

import android.media.MediaPlayer;
import android.content.Context;

import java.util.HashMap;

public class SoundManager {

    public enum SoundType {
        NFC,
        BUTTON,
        PAY,
        BACK,
        DENIED,
        ALERT
    }

    private static SoundManager instance;
    private MediaPlayer mediaPlayer;
    private HashMap<SoundType, Integer> map;

    private SoundManager() {
        this.map = new HashMap<>();
        this.map.put(SoundType.NFC, R.raw.nfc);
        this.map.put(SoundType.BUTTON, R.raw.button);
        this.map.put(SoundType.PAY, R.raw.payment);
        this.map.put(SoundType.BACK, R.raw.back);
        this.map.put(SoundType.DENIED, R.raw.denied);
        this.map.put(SoundType.ALERT, R.raw.alert);
    }

    public static SoundManager getInstance() {
        if (SoundManager.instance == null) {
            SoundManager.instance = new SoundManager();
        }
        return SoundManager.instance;
    }

    public void play(Context context, SoundType soundType) {
        if (this.mediaPlayer != null) {
            this.mediaPlayer.stop();
            this.mediaPlayer.release();
        }
        Integer resId = this.map.get(soundType);
        if (resId != null) {
            this.mediaPlayer = MediaPlayer.create(context, resId.intValue());
            this.mediaPlayer.start();
        }
    }
}
