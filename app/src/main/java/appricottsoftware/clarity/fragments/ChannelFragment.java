package appricottsoftware.clarity.fragments;

import android.content.Context;
import android.media.Image;
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

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import appricottsoftware.clarity.R;
import appricottsoftware.clarity.RecyclerAdapter;
import appricottsoftware.clarity.RecyclerListItem;
import appricottsoftware.clarity.models.Channel;
import appricottsoftware.clarity.sync.ClarityApp;
import butterknife.BindView;
import appricottsoftware.clarity.models.Episode;
import appricottsoftware.clarity.models.PlayerInterface;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;

import static appricottsoftware.clarity.sync.ClarityApp.getGson;

public class ChannelFragment extends Fragment {

    @BindView(R.id.RecyclerView_Channels)
    RecyclerView channelRecycler;
    @BindView(R.id.cardView_ChannelButton)
    CardView channelButtonCardView;
    @BindView(R.id.ConstraintLayout_Survey)
    ConstraintLayout surveyConstraintLayout;
    @BindView(R.id.constraintLayout_search)
    ConstraintLayout searchConstraintLayout;

    @BindView(R.id.textView_CreateChannel)
    TextView createChannelTextView;
    @BindView(R.id.textView_SearchResult)
    TextView searchResultTextView;
    @BindView(R.id.editText_search)
    EditText searchEditText;
    @BindView(R.id.imageButton_serachIcon)
    ImageButton searchIconImageButton;
    @BindView(R.id.imageButton_back)
    ImageButton backImageButton;
    @BindView(R.id.constraintLayout_Header)
    ConstraintLayout headerConstraintLayout;

    @BindView(R.id.toggleButton_cat1)
    ToggleButton btCat1;
    @BindView(R.id.toggleButton_cat2) ToggleButton btCat2;
    @BindView(R.id.toggleButton_cat3) ToggleButton btCat3;
    @BindView(R.id.toggleButton_cat4) ToggleButton btCat4;
    @BindView(R.id.toggleButton_cat5) ToggleButton btCat5;
    @BindView(R.id.toggleButton_cat6) ToggleButton btCat6;

    private RecyclerView.Adapter rAdapter;
    private List<RecyclerListItem> rListItems;

    private RecyclerView.Adapter rAdapterSearch;
    private List<RecyclerListItem> rListItemsSearch;

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

        rListItems = new ArrayList<>();
        rListItemsSearch = new ArrayList<>();

        rAdapter = new RecyclerAdapter(rListItems, getContext());
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
                rListItems = new ArrayList<>();
                goToChannelList();
            }
        });


        if (!seeSurvey) {
            createChannelsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(getActivity(), "create channel clicked", Toast.LENGTH_SHORT).show();

                    gotoCreateChannel();

                    searchIconImageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            rListItemsSearch = new ArrayList<>();
                            rAdapterSearch = new RecyclerAdapter(rListItemsSearch, getContext());
                            channelRecycler.setAdapter(rAdapterSearch);

                            String searchKeyword = searchEditText.getText().toString();

                            if (searchKeyword.length() == 0){
                                Toast.makeText(getContext(), "Please enter a topic to search", Toast.LENGTH_LONG).show();
                                return;
                            }

                            searchAPI(searchKeyword);

//                            searchResults = new ArrayList<>();
//
//                            RecyclerListItem searchItem1 = new RecyclerListItem("Fake Search Result 1", 1);
//                            RecyclerListItem searchItem2 = new RecyclerListItem("Fake Search Result 2", 1);
//
//                            searchResults.add(searchItem1);
//                            searchResults.add(searchItem2);
//
//                            rAdapterSearch = new RecyclerAdapter(searchResults, getContext());
//                            channelRecycler.setAdapter(rAdapterSearch);

                            goToSearchResults();

                        }
                    });

                        //ADD CHANNEL TO DATABASE
//                    int uid = 1;
//                    String name = "testChannelTitle";
//
//                    ClarityApp.getRestClient().createChannel(uid, name, getActivity(), new JsonHttpResponseHandler() {
//                        @Override
//                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                            Log.e(TAG, "onSuccess1 : " + response.toString() );
//                            super.onSuccess(statusCode, headers, response);
//                        }
//
//                        @Override
//                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                            try {
//                                switch(statusCode) {
//                                    case(0):
//                                        Toast.makeText(getContext(),
//                                                "Server is down. Please try later.",
//                                                Toast.LENGTH_LONG).show();
//                                        break;
//                                    default:
//                                        Log.i(TAG, "Channel onFailure. Default Switch. Status Code: " + statusCode);
//                                        break;
//                                }
//                            }
//                            catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            super.onFailure(statusCode, headers, throwable, errorResponse);
//                        }
//
//                    });

                }
            });
        }else {

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


        rListItems = new ArrayList<>();

        RecyclerListItem item1 = new RecyclerListItem("Fake Channel 1", 1);
        RecyclerListItem item2 = new RecyclerListItem("Fake Channel 2", 1);
        RecyclerListItem item3 = new RecyclerListItem("Fake Channel 3", 1);
        RecyclerListItem item4 = new RecyclerListItem("Fake Channel 4", 1);
        rListItems.add(item1);
        rListItems.add(item2);
        rListItems.add(item3);
        rListItems.add(item4);

        rAdapter = new RecyclerAdapter(rListItems, getContext());
        channelRecycler.setAdapter(rAdapter);

    }

    private void searchAPI(String query) {
        ClarityApp.getRestClient().getFullTextSearch(offset, query, 0, "episode", getActivity(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    offset = response.getInt("next_offset");
                    //Log.e(TAG, "MY JSON SEARCH RESPONSE: " + response.toString());

                    ////createChannel(response);

                    createSearchResult(response);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                throwable.printStackTrace();
                //should gracefully fail
            }
        });
    }

    private void createSearchResult(JSONObject response) {

        Channel aChannel = new Channel();
        ArrayList<Episode> episodes = new ArrayList<Episode>();

        try {
            JSONArray resp = response.getJSONArray("results");

            for (int i = 0; i < 10 && i < resp.length(); i++) {
                Episode e = getGson().fromJson(String.valueOf(resp.getJSONObject(i)), Episode.class);
                episodes.add(e);
            }
            for (int i = 0; i < episodes.size(); i++) {
                aChannel.setImage(episodes.get(i).getImage());
                aChannel.setName(episodes.get(i).getTitle_original());
                addChannelToSearchRecycler(aChannel);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void createChannel(JSONObject response) {

        Channel aChannel = new Channel();
        ArrayList<Episode> episodes = new ArrayList<Episode>();

        try {
            JSONArray resp = response.getJSONArray("results");

            for (int i = 0; i < 1 && i < resp.length(); i++) {
                Episode e = getGson().fromJson(String.valueOf(resp.getJSONObject(i)), Episode.class);
                episodes.add(e);

            }

            aChannel.setImage(episodes.get(0).getImage());
            aChannel.setName(episodes.get(0).getTitle_original());

            addChannelToRecycler(aChannel);


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void addChannelToSearchRecycler(Channel aChannel) {
        RecyclerListItem APIresult = new RecyclerListItem(aChannel.getName(), 1);

        rListItemsSearch.add(APIresult);

        rAdapterSearch = new RecyclerAdapter(rListItemsSearch, getContext());
        channelRecycler.setAdapter(rAdapterSearch);

    }

    private void addChannelToRecycler(Channel aChannel) {
        RecyclerListItem APIresult = new RecyclerListItem(aChannel.getName(), 1);

        rListItems.add(APIresult);

        rAdapter = new RecyclerAdapter(rListItems, getContext());
        channelRecycler.setAdapter(rAdapter);

    }



//    @OnClick(R.id.test_music)
//    public void onClickTestMusic() {
//        Episode testEpisode = Episode.getSampleEpisode();
//        playerInterface.playEpisode(testEpisode);
//    }


}

