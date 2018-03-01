package appricottsoftware.clarity.models;

import android.content.Context;
import android.content.SharedPreferences;

public class Session {

    private static SharedPreferences preferences;

    public Session(Context context) {
        preferences = context.getSharedPreferences("userSessionInformation", 0);
    }

    public void setUserID(int userID) {
        preferences.edit().putInt("userID", userID).apply();
    }

    public int getUserID() {
        int userID = preferences.getInt("userID", -1);
        return userID;
    }

    public void setPlaybackSpeed(float playbackSpeed) {
        // If the playback speed is invalid, set it to 1
        if(playbackSpeed < 0.5f || playbackSpeed > 3f) {
            playbackSpeed = 1f;
        }
        preferences.edit().putFloat("playbackSpeed", playbackSpeed).apply();
    }

    public float getPlaybackSpeed() {
        float playbackSpeed = preferences.getFloat("playbackSpeed", 1);
        return playbackSpeed;
    }
}
