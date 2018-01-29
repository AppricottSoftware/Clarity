package appricottsoftware.clarity.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v4.media.session.MediaControllerCompat;
import android.util.Log;

public class PlaybackReceiver extends BroadcastReceiver {

    private static final String TAG = "PlaybackReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
            // TODO: Pause the playback
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Action: " + intent.getAction() + "\n");
        sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n");
        Log.e(TAG, sb.toString());
    }
}
