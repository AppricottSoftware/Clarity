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

    public void setMaxLength(int maxLength) {
        preferences.edit().putInt("maxLength", maxLength).apply();
    }

    public int getMaxLength() {
        int maxLength = preferences.getInt("maxLength", -1);
        return maxLength;
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

    public void setCurrentChannel(int cid) {
        if(cid < 1) {
            cid = 0;
        }
        preferences.edit().putInt("currentChannel", cid).apply();
    }

    public int getCurrentChannel() {
        int currentChannel = preferences.getInt("currentChannel", 0);
        return currentChannel;
    }
}
