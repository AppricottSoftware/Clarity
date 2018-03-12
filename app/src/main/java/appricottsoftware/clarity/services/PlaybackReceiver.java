package appricottsoftware.clarity.services;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

public class PlaybackReceiver extends BroadcastReceiver {

    private static final String TAG = "PlaybackReceiver";
    private static PlayerService playerService;

    public PlaybackReceiver(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
            // TODO: Pause the playback
            Log.v(TAG, "onReceive: Noisy");
        } else if(PlaybackStateCompat.ACTION_PAUSE == Long.parseLong(intent.getAction())) {
            // TODO: Show play on notification
            Log.v(TAG, "onReceive: Play");
        } else if(PlaybackStateCompat.ACTION_PLAY == Long.parseLong(intent.getAction())) {
            // TODO: Show pause on notification
            Log.v(TAG, "onReceive: Pause");
        } else if(PlaybackStateCompat.ACTION_STOP == Long.parseLong(intent.getAction())) {
            // TODO: Stop service
            Log.v(TAG, "onReceive: Stop");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Action: " + intent.getAction() + "\n");
        sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n");
        Log.v(TAG, "onReceive: " + sb.toString());
    }
}
