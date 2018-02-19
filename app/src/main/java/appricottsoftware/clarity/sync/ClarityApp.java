package appricottsoftware.clarity.sync;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.gson.Gson;

import appricottsoftware.clarity.models.Session;

// Keep a state for the entire app for API calls
public class ClarityApp extends Application {

    private static ClarityClient clarityClient;
    private static GoogleSignInClient googleSignInClient;
    private static Session session;
    private static Gson gson;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    // Get the single instance of the API client
    public static ClarityClient getRestClient(Context context) {
        if(clarityClient == null) {
            clarityClient = new ClarityClient(context);
        }
        return clarityClient;
    }

    // Get the single instance of the JSON converter
    public static Gson getGson() {
        if(gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    // Get the single instance of a user Session
    public static Session getSession(Context context) {
        if (session == null) {
            session = new Session(context);
        }
        return session;
    }

    public void setGoogleSignInClient(GoogleSignInClient googleSignInClient) {
        this.googleSignInClient = googleSignInClient;
    }

    public GoogleSignInClient getGoogleSignInClient() {
        return this.googleSignInClient;
    }

    public void clearGoogleSignInClient() {
        googleSignInClient = null;
    }
}
