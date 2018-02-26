package appricottsoftware.clarity.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import appricottsoftware.clarity.R;
import appricottsoftware.clarity.adapters.ChannelSearchAdapter;
import appricottsoftware.clarity.adapters.ChannelsAdapter;
import appricottsoftware.clarity.adapters.EndlessRecyclerViewScrollListener;
import appricottsoftware.clarity.adapters.SearchResultsListAdapter;
import appricottsoftware.clarity.models.Channel;
import appricottsoftware.clarity.models.Episode;
import appricottsoftware.clarity.models.FragmentListener;
import appricottsoftware.clarity.sync.ClarityApp;
import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class ChannelSearchFragment extends Fragment {

    private static final String TAG = "ChannelSearchFragment";

    @BindView(R.id.rvChannelSearch) RecyclerView rvSearchResults;

    private FragmentListener fragmentListener;

    private List<Channel> searchResults;
    private ChannelSearchAdapter searchResultsAdapter;
    private EndlessRecyclerViewScrollListener scrollListener;

    private String searchQuery;
    private int nextOffset;

    Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if(context instanceof FragmentListener) {
            fragmentListener = (FragmentListener) context;
        } else {
            Log.e(TAG, context.toString() + " must implement FragmentListener");
            throw new ClassCastException(context.toString() + " must implement FragmentListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_channel_search, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // Initialize view lookups, listeners

        // Search params
        searchQuery = "";
        nextOffset = 0;

        // Set up the recycler view with searchResultsAdapter
        searchResults = new ArrayList<>();
        searchResultsAdapter = new ChannelSearchAdapter(searchResults, context, fragmentListener);
        rvSearchResults.setAdapter(searchResultsAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        rvSearchResults.setLayoutManager(linearLayoutManager);

        // Create a scroll listener and add it to the recyclerview for pagination
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                search(searchQuery);
            }
        };
        rvSearchResults.addOnScrollListener(scrollListener);

        // Alert the activity onCreateView has finished
        fragmentListener.onCreatedView();
    }

    public void search(final String query) {
        Log.v(TAG, "search: " + query);

        // If this is a new search, clear everything
        if(!query.equals(searchQuery)) {
            clearSearchResults();
        }

        // If there are search quota left, search
        if(ClarityApp.getRestClient().isSearchQuotaRemaining()) {
            ClarityApp.getRestClient().getFullTextSearch("", nextOffset, query, 0, "episode", context, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    try {
                        // Set the search query
                        searchQuery = query;

                        // Get where the next page starts
                        nextOffset = response.getInt("next_offset") + 1;

                        // Set the remaining search quota based on header values
                        ClarityApp.getRestClient().setSearchQuotaRemaining(headers, context);

                        // Fill the recyclerview with results
                        populateSearchResults(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.e(TAG, "search: onFailure", throwable);
                }
            });
        }
    }

    private void populateSearchResults(JSONObject response) {
        try {
            // Convert the json into a list of Episodes
            TypeToken<ArrayList<Episode>> token = new TypeToken<ArrayList<Episode>>() {};
            ArrayList<Episode> episodes = ClarityApp.getGson().fromJson(response.getString("results"), token.getType());

            for(Episode episode : episodes) {
                // Convert the Episode into Channel
                Channel channel = getChannel(episode);
                addChannelToSearchResults(channel);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Channel getChannel(Episode episode) {
        Channel channel = new Channel();
        channel.setImage(episode.getImage());
        channel.setTitle(episode.getTitle_original());
        channel.setMetadata(episode.getMetadata());
        return channel;
    }

    private void addChannelToSearchResults(Channel channel) {
        searchResults.add(channel);
        searchResultsAdapter.notifyItemInserted(searchResults.size() - 1);
    }

    private void clearSearchResults() {
        searchResults.clear();
        searchResultsAdapter.notifyDataSetChanged();
    }
}
