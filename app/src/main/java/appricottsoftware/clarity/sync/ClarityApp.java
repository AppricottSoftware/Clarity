package appricottsoftware.clarity.sync;

import android.app.Application;
import android.content.Context;

import com.google.gson.Gson;

/**
 * Created by karen on 1/16/18.
 */
// Keep a state for the entire app for API calls
public class ClarityApp extends Application {

    private static Context context;
    private static ClarityClient clarityClient;
    private static Gson gson;

    @Override
    public void onCreate() {
        super.onCreate();
        ClarityApp.context = this;
    }

    // Get the single instance of the API client
    public static ClarityClient getRestClient() {
        if(clarityClient == null) {
            clarityClient = new ClarityClient();
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
}
