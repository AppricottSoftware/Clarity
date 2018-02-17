package appricottsoftware.clarity.sync;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import appricottsoftware.clarity.R;
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
        AsyncHttpClient client = new AsyncHttpClient();

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


    public void registerRequest(String email, String password, Context context, JsonHttpResponseHandler handler) {
        // Create the rest client and add header(s)
        AsyncHttpClient client = new AsyncHttpClient();

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
}
