package appricottsoftware.clarity.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import java.util.List;

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
    private ArrayList<Channel> channels;
    private JSONObject browseChannels;
    private int position;

    private BrowseToChannelInterface interfaceCallback;
    public interface BrowseToChannelInterface {
        void requestChannels();
        void addChannel(Channel channel);
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

        // Send channels from ChannelFragment to BrowseFragment
        try {
            interfaceCallback = (BrowseToChannelInterface) getActivity();
        }
        catch(ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement BrowseToChannelInterface");
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
        // Initialize adapter with channels array
        adapter = new BrowseAdapter(getActivity(), channels);

        // Populate gridview
        gridView.setAdapter(adapter);

        // Code for long press context menu. May be used to remove uninteresting channels
        registerForContextMenu(gridView);

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

                // Get user confirmation
                showDialog(channels.get(position), position);

                // TODO: write getter interface to retreive all Channels from ChannelFragment to avoid displaying duplicate Channels Here
                // TODO: or add default browse database and take the set difference of the user's channels on the backend
            }
        });
    }

    // May use for long-press actions
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        // Set header
        menu.setHeaderTitle("Context Menu");

        // Get context menu
        AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) menuInfo;

        // Get long-press position
        position = cmi.position;

        // Add menu items
        menu.add(1, cmi.position, 0, "Action 1");
        menu.add(2, cmi.position, 0, "Action 2");
    }

    // May use for long-press actions
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String name = channels.get(position).getTitle();

        // Forcing channels to be received
        requestChannelsFromChannelFragment();

        switch (item.getGroupId()) {
            case 1:
                Toast.makeText(getActivity(), "Action 1, Item "+name, Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(getActivity(), "Action 2, Item "+name, Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    private void addToChannels(final Channel channel, final int position) {
        int uid = ClarityApp.getSession(getContext()).getUserID();

        // TODO: re-add onSuccess and on Failure methods to be handled in future.
        ClarityApp.getRestClient().createChannel(uid, channel, getActivity(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(getContext(), "Added channel!", Toast.LENGTH_LONG).show();

                // Add channel to ChannelFragment front-end
                addChannelToChannelFragment(channel);

                // Removes channel from browse once user clicks it
                channels.remove(position);
                adapter.notifyDataSetChanged();

                // Play channel
                playerInterface.playChannel(channels.get(position));
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

    private void showDialog(final Channel channel, final int position) {
        new AlertDialog.Builder(getActivity())
                // Prompt message
                .setMessage("Add " + channel.getTitle() + " to your channels?")

                // Yes button
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        // On confirmation from user, add channel via backend call
                        addToChannels(channel, position);
                    }})

                // No button
                .setNegativeButton(android.R.string.no, null).show();
    }

    public void receiveChannelsFromChannelFragment(List<Channel> channels) {
        Log.i(TAG, "Success, channels received!");
        if (channels != null) {
            for (int i = 0; i < channels.size(); i++) {
                Log.e(TAG, "Channel 1: " + channels.get(i).getTitle());
            }
        }
    }

    void requestChannelsFromChannelFragment() {
        Log.i(TAG, "Requesting channels from contextual menu");
        interfaceCallback.requestChannels();
    }

    void addChannelToChannelFragment(Channel channel) {
        Log.i(TAG, "Attempt to send channel " + channel.getTitle() + " to ChannelFragment front-end");
        interfaceCallback.addChannel(channel);
    }

    private void mergeUserChannelsWithBrowseChannels() {

    }
}
