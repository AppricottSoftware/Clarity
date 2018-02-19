package appricottsoftware.clarity.sync;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import appricottsoftware.clarity.R;
import appricottsoftware.clarity.models.Metadata;
import cz.msebera.android.httpclient.entity.StringEntity;

public class ClarityClient {

    public ClarityClient() {}

    // Insert API calls here //
    // Calls the /search endpoint (fulltextsearch)
    // Parameters //
    // offset: Offset for search results, for pagination. You'll use next_offset from response for this parameter.
    // q: Search term
    // sort_by_date: Sort by date or not? If 1, sort by date. If 0 (default), sort by relevance.
    // type: What to search: "episode" (default) or "podcast"?
    public void getFullTextSearch(int offset, String q, int sort_by_date, String type, Context context, JsonHttpResponseHandler handler) {
        // Create the rest client and add header(s)
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("X-Mashape-Key", context.getString(R.string.listen_notes_api_key));
        // Next, we add the parameters for the api call (see function description above)
        RequestParams params = new RequestParams();
        params.put("offset", offset);
        params.put("q", q);
        params.put("sort_by_date", sort_by_date);
        params.put("type", type);
        client.get(context.getString(R.string.listen_notes_api_url) + "search", params, handler);
    }

    public void authenticateUser(String email, String password, Context context, JsonHttpResponseHandler handler) {
        // Create the rest client and add header(s)

        // Added conditional to handle this issue:
        // W/AsyncHttpRH: Current thread has not called Looper.prepare(). Forcing synchronous mode.
        if (Looper.myLooper() == null) {
            SyncHttpClient client = new SyncHttpClient();

            JSONObject jsonParams = new JSONObject();
            try {
                jsonParams.put("email", email);
                jsonParams.put("password", password);

                StringEntity entity = new StringEntity(jsonParams.toString());
                client.post(context, context.getString(R.string.login_request_url), entity, "application/json", handler);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        else {
            AsyncHttpClient client = new AsyncHttpClient();

            JSONObject jsonParams = new JSONObject();
            try {
                client.setMaxRetriesAndTimeout(1, 1000);

                jsonParams.put("email", email);
                jsonParams.put("password", password);

                StringEntity entity = new StringEntity(jsonParams.toString());
                client.post(context, context.getString(R.string.login_request_url), entity, "application/json", handler);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void registerRequest(String email, String password, Context context, JsonHttpResponseHandler handler) {
        // Create the rest client and add header(s)

        // Added conditional to handle this issue:
        // W/AsyncHttpRH: Current thread has not called Looper.prepare(). Forcing synchronous mode.
        if (Looper.myLooper() == null) {
            SyncHttpClient client = new SyncHttpClient();

            JSONObject jsonParams = new JSONObject();
            try {
                jsonParams.put("email", email);
                jsonParams.put("password", password);

                StringEntity entity = new StringEntity(jsonParams.toString());
                client.post(context, context.getString(R.string.register_request_url), entity, "application/json", handler);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        else {
            AsyncHttpClient client = new AsyncHttpClient();

            JSONObject jsonParams = new JSONObject();
            try {
                client.setMaxRetriesAndTimeout(1, 1000);

                jsonParams.put("email", email);
                jsonParams.put("password", password);

                StringEntity entity = new StringEntity(jsonParams.toString());
                client.post(context, context.getString(R.string.register_request_url), entity, "application/json", handler);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void metadataUpVoteRequest(Context context, JsonHttpResponseHandler handler) {
        AsyncHttpClient client = new AsyncHttpClient();

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("cid", 1);
            JSONArray metadata = new JSONArray();

            JSONObject element1 = new JSONObject();
            element1.put("mid", 1);
            JSONObject element2 = new JSONObject();
            element2.put("mid", 2);

            metadata.put(element1);
            metadata.put(element2);

            jsonParams.put("metadata", metadata);

            jsonParams.put("metadata", metadata);

            StringEntity entity = new StringEntity(jsonParams.toString());
            Log.e("TESTING", jsonParams.toString() + " TO: " + R.string.put_dislike_request_url);
            client.post(context, context.getString(R.string.put_dislike_request_url), entity, "application/json", handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void createChannel(int uid, String name, String imageURL, Context context, JsonHttpResponseHandler handler) {
        // Create the rest client and add header(s)
        //pass in metadata

        AsyncHttpClient client = new AsyncHttpClient();

        JSONObject jsonParams = new JSONObject();

        JSONObject aMetadata = new JSONObject();
        JSONObject bMetadata = new JSONObject();

        try {
            aMetadata.put("genre", "politics");
            aMetadata.put("mid", 123);
            aMetadata.put("score", 5);
            bMetadata.put("genre", "social");
            bMetadata.put("mid", 456);
            bMetadata.put("score", 10);

            JSONArray metadata = new JSONArray();
            metadata.put(aMetadata);
            metadata.put(bMetadata);

            jsonParams.put("uid", uid);
            jsonParams.put("title", name);
            jsonParams.put("image", imageURL);
            jsonParams.put("metadata", metadata);

            StringEntity entity = new StringEntity(jsonParams.toString());
            client.post(context, context.getString(R.string.create_channel_request_url), entity, "application/json", handler);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
