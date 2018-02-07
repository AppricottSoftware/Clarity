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
        } else if(PlaybackStateCompat.ACTION_PAUSE == Long.parseLong(intent.getAction())) {
            // TODO: show play on notification
            Log.d(TAG, "Play");
        } else if(PlaybackStateCompat.ACTION_PLAY == Long.parseLong(intent.getAction())) {
            // TODO: show pause on notification
            Log.d(TAG, "Pause");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Action: " + intent.getAction() + "\n");
        sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n");
        Log.d(TAG, sb.toString());
    }
}
