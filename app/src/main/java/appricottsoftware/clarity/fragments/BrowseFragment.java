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

import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import appricottsoftware.clarity.R;
import appricottsoftware.clarity.adapters.BrowseAdapter;
import appricottsoftware.clarity.models.Channel;
import appricottsoftware.clarity.models.PlayerInterface;
import appricottsoftware.clarity.models.Podcast;
import appricottsoftware.clarity.sync.ClarityApp;
import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

import static appricottsoftware.clarity.sync.ClarityApp.getGson;

public class BrowseFragment extends Fragment {

    private static final String TAG = "BrowseFragment";

    @BindView(R.id.gv_browse) GridView gridView;

    private BrowseAdapter adapter;
    private PlayerInterface playerInterface;
    private ArrayList<Podcast> podcasts;
    private ArrayList<Channel> channels;
    private String query;
    private int offset;

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
        channels = new ArrayList<>();

        // Parse browse.json from assets to populate Channel array.
        parseJSON();
        populateBrowseGrid();
    }

    // Parses json file and adds contexts to Channel ArrayList
    void parseJSON() {
        try {
            JSONObject object = new JSONObject(loadJSONFromAsset());
            JSONArray response = object.getJSONArray("results");

            // This code is a messier way to parse JSON, but allows setting UID. Leaving commented code
            // in case UID must be set here.
//            for (int i = 0; i < response.length(); i++) {
//                Channel c = getGson().fromJson(String.valueOf(response.getJSONObject(i)), Channel.class);
//                c.setUid(ClarityApp.getSession(getActivity()).getUserID());
//                channels.add(c);
//            }

            // This code below is a more elegant way of parsing the JSON file but doesn't allow setting UID
            TypeToken<ArrayList<Channel>> token = new TypeToken<ArrayList<Channel>>() {};
            channels = ClarityApp.getGson().fromJson(response.toString(), token.getType());


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Gets json from file. Will eventually update this code to pull json from database
    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getActivity().getAssets().open("browse.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void populateBrowseGrid() {
        adapter = new BrowseAdapter(getActivity(), channels);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                // TODO write interface to send onClicked Channel object to ChannelFragment
                Toast.makeText(getActivity(), "Title: " + channels.get(position).getTitle()
                                            + "\nCid: " + channels.get(position).getCid()
                                            + "\nUid: " + channels.get(position).getUid()
                                            + "\nMetadata: " + channels.get(position).getMetadata().get(0).getGenre()
                                            + " " + channels.get(position).getMetadata().get(0).getMid()
                                            + " " + channels.get(position).getMetadata().get(0).getScore(),
                        Toast.LENGTH_SHORT).show();

                // Get the channel that was clicked on
                Channel channel = channels.get(position);

                // Play the channel
                playerInterface.playChannel(channels.get(position));

                // Add it to the user's channels
                addToChannels(channel, position);

                // TODO: write getter interface to retreive all Channels from ChannelFragment to avoid displaying duplicate Channels Here
                // TODO: or add default browse database and take the set difference of the user's channels on the backend
            }
        });
    }

    private void addToChannels(Channel channel, final int position) {
        int uid = ClarityApp.getSession(getContext()).getUserID();

        // TODO: re-add onSuccess and on Failure methods to be handled in future.
        ClarityApp.getRestClient().createChannel(uid, channel, getContext(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(getContext(), "Added channel!", Toast.LENGTH_LONG).show();
                // Removes channel from browse once user clicks it
                channels.remove(position);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                switch(statusCode) {
                    case(0):
                        Toast.makeText(getContext(),"Server is down. Please try later.", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        Log.e(TAG, "Channel onFailure. Default Switch. Status Code: " + statusCode);
                        break;
                }
            }
        });
    }
}
