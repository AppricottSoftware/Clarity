package appricottsoftware.clarity.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import appricottsoftware.clarity.R;
import appricottsoftware.clarity.adapters.ImageTextAdapter;
import appricottsoftware.clarity.models.PlayerInterface;
import appricottsoftware.clarity.models.Podcast;
import appricottsoftware.clarity.sync.ClarityApp;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

import static appricottsoftware.clarity.sync.ClarityApp.getGson;

public class BrowseFragment extends Fragment {

    private static final String TAG = "BrowseFragment";

    private PlayerInterface playerInterface;
    private ArrayList<Podcast> podcasts;
    private String query;
    private int offset;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // Initialize view lookups, listeners

        podcasts = new ArrayList<>();
        getPodcasts("episode");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof PlayerInterface) {
            playerInterface = (PlayerInterface) context;
        } else {
            Log.e(TAG, context.toString() + " must implement PlayerInterface");
            throw new ClassCastException(context.toString() + " must implement PlayerInterface");
        }
    }

    // Make an API call to get podcasts
    private void getPodcasts(String q) {
        if(this.query != q) {
            this.query = q;
        }
        Log.e(TAG, "On Query text " + query);
        // Specify the callback functions for the response handler
        ClarityApp.getRestClient().getFullTextSearch(offset, query, 0, "episode", getActivity(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.e("BrowseFragment 1", response.toString());
                addPodcasts(response);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                throwable.printStackTrace();
                Log.e("BrowseFragment 4", "");
            }
        });
    }

    private void addPodcasts(JSONObject response) {
        // Convert the JSON into Podcasts objects (our model)
        try {
            offset = response.getInt("next_offset");
            JSONArray resp = response.getJSONArray("results");
            Log.e(TAG, resp.toString());
            for (int i = 0; i < 6 && i < resp.length(); i++) {
                Podcast p = getGson().fromJson(String.valueOf(resp.getJSONObject(i)), Podcast.class);
                Log.e(TAG, String.valueOf(resp.getJSONObject(i)));
                Log.e("Adapter", p.getImage());
                podcasts.add(p);
            }
            populateBrowseGrid();
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    private void populateBrowseGrid() {
        GridView gridview = getActivity().findViewById(R.id.gv_browse);
        gridview.setAdapter(new ImageTextAdapter(getActivity(), podcasts));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(getActivity(), "" + position,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
