package appricottsoftware.clarity.sync;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

public class ClarityClient {
    private static final String REST_URL = "https://listennotes.p.mashape.com/api/v1/";
    private static final String REST_KEY = "";
    private static final String POSTENDPOINT = "";

    // Insert API calls here //
    // Calls the /search endpoint (fulltextsearch)
    // Parameters //
    // offset: Offset for search results, for pagination. You'll use next_offset from response for this parameter.
    // q: Search term
    // sort_by_date: Sort by date or not? If 1, sort by date. If 0 (default), sort by relevance.
    // type: What to search: "episode" (default) or "podcast"?
    public void getFullTextSearch(int offset, String q, int sort_by_date, String type, JsonHttpResponseHandler handler) {
        // Create the rest client and add header(s)
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("X-Mashape-Key", REST_KEY);
        // Next, we add the parameters for the api call (see function description above)
        RequestParams params = new RequestParams();
        params.put("offset", offset);
        params.put("q", q);
        params.put("sort_by_date", sort_by_date);
        params.put("type", type);
        client.get(REST_URL + "search", params, handler);
    }

    public void checkLogin(String email, String password, JsonHttpResponseHandler handler) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Accept", "application/json");
        client.addHeader("Content-type", "application/json");

        RequestParams params = new RequestParams();
        params.put("typeOfRequest", "verifyLogin");
        params.put("email", email);
        params.put("password", password);

        client.get(POSTENDPOINT, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println(response);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable
                    error)
            {
                error.printStackTrace(System.out);
            }
        });
    }

}
