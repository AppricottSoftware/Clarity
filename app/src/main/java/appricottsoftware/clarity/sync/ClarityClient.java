package appricottsoftware.clarity.sync;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.common.api.Response;
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

    private Context context;

    public ClarityClient(Context context) {
        this.context = context;
    }

    // Insert API calls here //
    // Calls the /search endpoint (fulltextsearch)
    // Parameters //
    // offset: Offset for search results, for pagination. You'll use next_offset from response for this parameter.
    // q: Search term
    // sort_by_date: Sort by date or not? If 1, sort by date. If 0 (default), sort by relevance.
    // type: What to search: "episode" (default) or "podcast"?
    public void getFullTextSearch(String genre_ids, int offset, String q, int sort_by_date, String type, JsonHttpResponseHandler handler) {
        // Create the rest client and add header(s)
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("X-Mashape-Key", context.getString(R.string.listen_notes_api_key));
        // Next, we add the parameters for the api call (see function description above)
        RequestParams params = new RequestParams();
        params.put("genre_ids", genre_ids);
        params.put("offset", offset);
        params.put("q", q);
        params.put("sort_by_date", sort_by_date);
        params.put("type", type);
        client.get(context.getString(R.string.listen_notes_api_url) + "search", params, handler);
    }

    public void authenticateUser(String email, String password, JsonHttpResponseHandler handler) {
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


    public void registerRequest(String email, String password, JsonHttpResponseHandler handler) {
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

    public void metadataUpVoteRequest(int cid, ArrayList<Integer> genres, JsonHttpResponseHandler handler) {
        AsyncHttpClient client = new AsyncHttpClient();

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("cid", cid);
            JSONArray metadata = new JSONArray();

            for(Integer g : genres) {
                JSONObject elem = new JSONObject();
                elem.put("mid", g);
                metadata.put(elem);
            }

            jsonParams.put("metadata", metadata);

            StringEntity entity = new StringEntity(jsonParams.toString());
            Log.e("TESTING", jsonParams.toString() + " TO: " + context.getString(R.string.put_likes_request_url));
            client.post(context, context.getString(R.string.put_likes_request_url), entity, "application/json", handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void metadataDownVoteRequest(int cid, ArrayList<Integer> genres, JsonHttpResponseHandler handler) {
        AsyncHttpClient client = new AsyncHttpClient();

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("cid", cid);

            JSONArray metadata = new JSONArray();
            for (Integer g : genres) {
                JSONObject elem = new JSONObject();
                elem.put("mid", g);
                metadata.put(elem);
            }

            jsonParams.put("metadata", metadata);

            StringEntity entity = new StringEntity(jsonParams.toString());
            Log.e("TESTING", jsonParams.toString() + " TO: " + context.getString(R.string.put_dislike_request_url));
            client.post(context, context.getString(R.string.put_dislike_request_url), entity, "application/json", handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createChannel(int uid, String name, String imageURL, JsonHttpResponseHandler handler) {
        // Create the rest client and add header(s)
        //pass in metadata

        AsyncHttpClient client = new AsyncHttpClient();

        JSONObject jsonParams = new JSONObject();

        // TODO: Metadata are currently hardcoded below. Get metadata from selected podcast in search so it may be added to db.
        try {

            JSONArray metadata = new JSONArray();

            JSONObject aMetadata = new JSONObject();
            aMetadata.put("genre", "Something1");
            aMetadata.put("mid", 111);
            aMetadata.put("score", 123);
            metadata.put(aMetadata);

            JSONObject bMetadata = new JSONObject();
            bMetadata.put("genre", "Something2");
            bMetadata.put("mid", 114);
            bMetadata.put("score", 123);
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

    public void getChannel(int uid, JsonHttpResponseHandler handler) {
        // Create the rest client and add header(s)

        AsyncHttpClient client = new AsyncHttpClient();

        JSONObject jsonParams = new JSONObject();

        try {
            jsonParams.put("uid", uid);

            StringEntity entity = new StringEntity(jsonParams.toString());
            client.get(context, context.getString(R.string.get_channel_request_url), entity, "application/json", handler);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
