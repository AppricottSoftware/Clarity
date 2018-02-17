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
}
