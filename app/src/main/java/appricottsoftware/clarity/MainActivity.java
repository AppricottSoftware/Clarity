package appricottsoftware.clarity;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import appricottsoftware.clarity.adapters.EndlessRecyclerViewScrollListener;
import appricottsoftware.clarity.adapters.SearchResultsListAdapter;
import appricottsoftware.clarity.models.Podcast;
import appricottsoftware.clarity.sync.ClarityApp;
import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

import static appricottsoftware.clarity.sync.ClarityApp.getGson;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    private ArrayList<Podcast> searchResults;
    private SearchResultsListAdapter searchResultsAdapter;
    private EndlessRecyclerViewScrollListener scrollListener;

    private String query;
    private int offset;

    @BindView(R.id.rvSearchResults) RecyclerView rvSearchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        searchResults = new ArrayList<>();
        searchResultsAdapter = new SearchResultsListAdapter(searchResults);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvSearchResults.setLayoutManager(linearLayoutManager);
        rvSearchResults.setAdapter(searchResultsAdapter);
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                getPodcasts(query);
            }
        };
        rvSearchResults.addOnScrollListener(scrollListener);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String q) {
                searchResults.clear();
                searchResultsAdapter.notifyDataSetChanged();
                scrollListener.resetState();
                // Focus on the activity, minimize keyboard
                searchView.clearFocus();
                // Query for podcasts once user enters text
                getPodcasts(q);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        MenuItem searchItem = menu.findItem(R.id.action_search);
        return super.onCreateOptionsMenu(menu);
    }

    // Make an API call to get podcasts
    private void getPodcasts(String q) {
        if(this.query != q) {
            this.query = q;
        }
        Log.e("MainActivity", "On Query text" + query);
        // Specify the callback functions for the response handler
//        ClarityApp.getRestClient(this).getFullTextSearch(offset, query, 0, "episode", new JsonHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                super.onSuccess(statusCode, headers, response);
//                Log.e("MainActivity 1", response.toString());
//                addPodcasts(response);
//            }
//
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
//                super.onSuccess(statusCode, headers, response);
//                Log.e("MainActivity 2", response.toString());
//            }
//
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, String responseString) {
//                super.onSuccess(statusCode, headers, responseString);
//                Log.e("MainActivity 3", responseString);
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                super.onFailure(statusCode, headers, throwable, errorResponse);
//                throwable.printStackTrace();
//                Log.e("MainActivity 4", "");
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
//                super.onFailure(statusCode, headers, throwable, errorResponse);
//                throwable.printStackTrace();
//                Log.e("MainActivity 5", "");
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                super.onFailure(statusCode, headers, responseString, throwable);
//                throwable.printStackTrace();
//                Log.e("MainActivity 6", "");
//            }
//        });
    }

    private void addPodcasts(JSONObject response) {
        // Convert the JSON into Podcasts objects (our model)
        try {
            offset = response.getInt("next_offset");
            JSONArray resp = response.getJSONArray("results");
            Log.e(TAG, resp.toString());
            for(int i = 0; i < resp.length(); i++) {
                Podcast p = getGson().fromJson(String.valueOf(resp.getJSONObject(i)), Podcast.class);
                searchResults.add(p);
                searchResultsAdapter.notifyItemInserted(searchResults.size() - 1);
                Log.e("MainActivityAddPodcasts", String.valueOf(resp.getJSONObject(i)));
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }
}
