package appricottsoftware.clarity.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import appricottsoftware.clarity.R;
import appricottsoftware.clarity.adapters.ImageTextAdapter;
import appricottsoftware.clarity.models.Channel;
import appricottsoftware.clarity.models.Metadata;
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
    private ArrayList<Channel> channels;
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
        channels = new ArrayList<>();

        // Parse browse.json from assets to populate Channel array.
        parseJSON();
        populateBrowseGrid();
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

    // Parses json file and adds contexts to Channel ArrayList
    void parseJSON() {
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset());
            JSONArray resp = obj.getJSONArray("results");
            for (int i = 0; i < resp.length(); i++) {
                Channel c = getGson().fromJson(String.valueOf(resp.getJSONObject(i)), Channel.class);
                c.setUid(ClarityApp.getSession(getActivity()).getUserID());
                channels.add(c);
            }
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
        final GridView gridview = getActivity().findViewById(R.id.gv_browse);
        final ImageTextAdapter adapter = new ImageTextAdapter(getActivity(), channels);
        gridview.setAdapter(adapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

                // Removes channel from browse once user clicks it
//                channels.remove(position);
//                adapter.notifyDataSetChanged();

                // TODO write getter interface to retreive all Channels from ChannelFragment to avoid displaying duplicate Channels Here
            }
        });
    }
}
