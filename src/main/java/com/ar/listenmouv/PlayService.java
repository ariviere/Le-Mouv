package com.ar.listenmouv;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import java.io.IOException;

public class PlayService extends Service {

    MediaPlayer mediaPlayer;
    String url_low_quality = "http://mp3.live.tv-radio.com/lemouv/all/lemouv-32k.mp3";
    String url_high_quality = "http://mp3.live.tv-radio.com/lemouv/all/lemouvhautdebit.mp3";

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        PlayService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PlayService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /** method for clients */
    public void play(boolean hq) {
        try {
            mediaPlayer = new MediaPlayer();

            AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/10, 0);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            if(hq)
                mediaPlayer.setDataSource(url_high_quality);
            else
                mediaPlayer.setDataSource(url_low_quality);

            mediaPlayer.prepare();
            mediaPlayer.start();

            showNotification();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop(){
        mediaPlayer.stop();
        stopForeground(true);
    }

    private void showNotification(){
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification();
        notification.icon = R.drawable.notif;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.setLatestEventInfo(getApplicationContext(), getResources().getString(R.string.app_name),
                getResources().getString(R.string.notif_description), pi);
        startForeground(2, notification);
    }
}
