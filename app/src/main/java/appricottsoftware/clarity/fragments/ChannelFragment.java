package appricottsoftware.clarity.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import appricottsoftware.clarity.R;
import appricottsoftware.clarity.adapters.ChannelsAdapter;
import appricottsoftware.clarity.adapters.RecyclerAdapter;
import appricottsoftware.clarity.adapters.RecyclerListItem;
import appricottsoftware.clarity.models.Channel;
import appricottsoftware.clarity.sync.ClarityApp;
import butterknife.BindView;
import appricottsoftware.clarity.models.Episode;
import appricottsoftware.clarity.models.PlayerInterface;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

import static appricottsoftware.clarity.sync.ClarityApp.getGson;

public class ChannelFragment extends Fragment {

    @BindView(R.id.RecyclerView_Channels) RecyclerView channelRecycler;
    @BindView(R.id.cardView_ChannelButton) CardView channelButtonCardView;
    @BindView(R.id.ConstraintLayout_Survey) ConstraintLayout surveyConstraintLayout;
    @BindView(R.id.constraintLayout_search) ConstraintLayout searchConstraintLayout;

    @BindView(R.id.textView_CreateChannel) TextView createChannelTextView;
    @BindView(R.id.textView_SearchResult) TextView searchResultTextView;
    @BindView(R.id.editText_search) EditText searchEditText;
    @BindView(R.id.imageButton_serachIcon) ImageButton searchIconImageButton;
    @BindView(R.id.imageButton_back) ImageButton backImageButton;
    @BindView(R.id.constraintLayout_Header) ConstraintLayout headerConstraintLayout;

    @BindView(R.id.toggleButton_cat1) ToggleButton btCat1;
    @BindView(R.id.toggleButton_cat2) ToggleButton btCat2;
    @BindView(R.id.toggleButton_cat3) ToggleButton btCat3;
    @BindView(R.id.toggleButton_cat4) ToggleButton btCat4;
    @BindView(R.id.toggleButton_cat5) ToggleButton btCat5;
    @BindView(R.id.toggleButton_cat6) ToggleButton btCat6;

    private RecyclerView.Adapter rAdapter;
    private List<Channel> channels;

    private RecyclerView.Adapter rAdapterSearch;
    private List<Channel> searchChannels;

    int offset = 0;

    // To set - query to see if channels exist for user.
    boolean seeSurvey = false;

    private static final String TAG = "ChannelFragment";

    private PlayerInterface playerInterface;

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
        View view = inflater.inflate(R.layout.fragment_channel, container, false);
        ButterKnife.bind(this, view);

        //View view;

        // TODO: decide on usefulness and form of survey
//        if (seeSurvey) {
//
//            channelButtonCardView.setVisibility(View.INVISIBLE);
//            channelRecycler.setVisibility(View.INVISIBLE);
//            searchConstraintLayout.setVisibility(View.INVISIBLE);
//            surveyConstraintLayout.setVisibility(View.VISIBLE);
//
//        }
//        else {

//        }

        channelRecycler.setHasFixedSize(true);
        channelRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        channels = new ArrayList<>();
        searchChannels = new ArrayList<>();

        rAdapter = new ChannelsAdapter(channels, getContext(), true);
        rAdapterSearch = new ChannelsAdapter(searchChannels, getContext(), false);
        channelRecycler.setAdapter(rAdapter);

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // Initialize view lookups, listeners

        Button createChannelsButton = view.findViewById(R.id.button_createChannel);

        channelRecycler.setHasFixedSize(true);
        channelRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        goToChannelList();

        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                channels = new ArrayList<>();
                goToChannelList();
            }
        });

        if (!seeSurvey) {
            createChannelsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    gotoCreateChannel();

                    searchIconImageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            searchChannels = new ArrayList<>();
                            rAdapterSearch = new ChannelsAdapter(searchChannels, getContext(), false);
                            channelRecycler.setAdapter(rAdapterSearch);

                            String searchKeyword = searchEditText.getText().toString();

                            if (searchKeyword.length() == 0){
                                Toast.makeText(getContext(), "Please enter a topic to search", Toast.LENGTH_LONG).show();
                                return;
                            }

                            searchAPI(searchKeyword);
                            goToSearchResults();
                        }
                    });

                }
            });
        } else {

            btCat1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        btCat1.setBackground(getResources().getDrawable(R.drawable.ic_nav_drawer_like_green));
                    } else {
                        btCat1.setBackground(getResources().getDrawable(R.drawable.ic_nav_drawer_like_white));
                    }
                }
            });
            btCat2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                    {btCat2.setBackground(getResources().getDrawable(R.drawable.ic_nav_drawer_like_green));}
                    else {btCat2.setBackground(getResources().getDrawable(R.drawable.ic_nav_drawer_like_white));}
                }
            });
            btCat3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                    {btCat3.setBackground(getResources().getDrawable(R.drawable.ic_nav_drawer_like_green));}
                    else {btCat3.setBackground(getResources().getDrawable(R.drawable.ic_nav_drawer_like_white));}
                }
            });
            btCat4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                    {btCat4.setBackground(getResources().getDrawable(R.drawable.ic_nav_drawer_like_green));}
                    else {btCat4.setBackground(getResources().getDrawable(R.drawable.ic_nav_drawer_like_white));}
                }
            });
            btCat5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                    {btCat5.setBackground(getResources().getDrawable(R.drawable.ic_nav_drawer_like_green));}
                    else {btCat5.setBackground(getResources().getDrawable(R.drawable.ic_nav_drawer_like_white));}
                }
            });
            btCat6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                    {btCat6.setBackground(getResources().getDrawable(R.drawable.ic_nav_drawer_like_green));}
                    else {btCat6.setBackground(getResources().getDrawable(R.drawable.ic_nav_drawer_like_white));}
                }
            });
        }
    }

    private void goToSearchResults() {
        channelButtonCardView.setVisibility(View.INVISIBLE);
        createChannelTextView.setVisibility(View.INVISIBLE);
        searchResultTextView.setVisibility(View.VISIBLE);
        channelRecycler.setVisibility(View.VISIBLE);
        searchConstraintLayout.setVisibility(View.INVISIBLE);
        createChannelTextView.setVisibility(View.INVISIBLE);
        searchResultTextView.setVisibility(View.VISIBLE);
    }

    private void gotoCreateChannel() {
        channelButtonCardView.setVisibility(View.INVISIBLE);
        createChannelTextView.setVisibility(View.VISIBLE);
        searchResultTextView.setVisibility(View.INVISIBLE);
        channelRecycler.setVisibility(View.INVISIBLE);
        searchConstraintLayout.setVisibility(View.VISIBLE);
        headerConstraintLayout.setVisibility(View.VISIBLE);
        createChannelTextView.setVisibility(View.VISIBLE);
        searchResultTextView.setVisibility(View.INVISIBLE);
    }

    private void goToChannelList() {

        channelButtonCardView.setVisibility(View.VISIBLE);
        channelRecycler.setVisibility(View.VISIBLE);
        surveyConstraintLayout.setVisibility(View.INVISIBLE);
        searchConstraintLayout.setVisibility(View.INVISIBLE);
        headerConstraintLayout.setVisibility(View.INVISIBLE);

        channels = new ArrayList<>();

        int uid = ClarityApp.getSession(getContext()).getUserID();
        ClarityApp.getRestClient(getContext()).getChannel(uid, new JsonHttpResponseHandler() {
            
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    // Convert the response to Channels
                    TypeToken<ArrayList<Channel>> token = new TypeToken<ArrayList<Channel>>() {};
                    channels = ClarityApp.getGson().fromJson(response.toString(), token.getType());
                    rAdapter = new ChannelsAdapter(channels, getContext(), true);
                    channelRecycler.setAdapter(rAdapter);

                } catch(Exception e) {
                    Log.e(TAG, "goToChannelList: Failed to get channels", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    switch(statusCode) {
                        case(0):
                            Toast.makeText(getContext(),
                                    "Server is down. Please try later.",
                                    Toast.LENGTH_LONG).show();
                            break;
                        default:
                            Log.i(TAG, "Channel onFailure. Default Switch. Status Code: " + statusCode);
                            break;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    // TODO: currently hard coded search with "episode" type. May need to be changed eventually.
    private void searchAPI(String query) {
        ClarityApp.getRestClient(getContext()).getFullTextSearch("", offset, query, 0, "episode", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    offset = response.getInt("next_offset");
                    createSearchResult(response);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                throwable.printStackTrace();
                // TODO: how to fail?
            }
        });
    }

    private void createSearchResult(JSONObject response) {
        try {
            TypeToken<ArrayList<Episode>> token = new TypeToken<ArrayList<Episode>>() {};
            ArrayList<Episode> episodes = ClarityApp.getGson().fromJson(response.getString("results"), token.getType());

            for(Episode episode : episodes) {
                Channel channel = new Channel();
                channel.setImage(episode.getImage());
                channel.setTitle(episode.getTitle_original());
                channel.setMetadata(episode.getMetadata());
                addChannelToSearchRecycler(channel);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    // TODO: does this need to be separate from serach create?
//    private void createChannel(JSONObject response) {
//
//        Channel aChannel = new Channel();
//        ArrayList<Episode> episodes = new ArrayList<Episode>();
//
//        try {
//            JSONArray resp = response.getJSONArray("results");
//
//            for (int i = 0; i < 1 && i < resp.length(); i++) {
//                Episode e = getGson().fromJson(String.valueOf(resp.getJSONObject(i)), Episode.class);
//                episodes.add(e);
//
//            }
//            aChannel.setImage(episodes.get(0).getImage());
//            aChannel.setName(episodes.get(0).getTitle_original());
//
//            addChannelToRecycler(aChannel);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    private void addChannelToSearchRecycler(Channel channel) {
        searchChannels.add(channel);
        rAdapterSearch = new ChannelsAdapter(searchChannels, getContext(), false);
        channelRecycler.setAdapter(rAdapterSearch);
    }

    // TODO: does this need to be separate from search recycler?
//    private void addChannelToRecycler(Channel aChannel) {
//
//        RecyclerListItem APIresult = new RecyclerListItem(aChannel.getName(), 1);
//
//        rListItems.add(APIresult);
//
//        rAdapter = new RecyclerAdapter(rListItems, getContext(), true);
//        channelRecycler.setAdapter(rAdapter);
//    }
}

