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
import android.widget.ImageButton;
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

    @BindView(R.id.toggleButton_cat1)
    ToggleButton btCat1;
    @BindView(R.id.toggleButton_cat2) ToggleButton btCat2;
    @BindView(R.id.toggleButton_cat3) ToggleButton btCat3;
    @BindView(R.id.toggleButton_cat4) ToggleButton btCat4;
    @BindView(R.id.toggleButton_cat5) ToggleButton btCat5;
    @BindView(R.id.toggleButton_cat6) ToggleButton btCat6;


    private RecyclerView.Adapter rAdapter;
    private List<RecyclerListItem> rListItems;

    int offset = 0;
    String query = "starwars";

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

        if (seeSurvey) {


            channelButtonCardView.setVisibility(View.INVISIBLE);
            channelRecycler.setVisibility(View.INVISIBLE);
            surveyConstraintLayout.setVisibility(View.VISIBLE);

        }
        else {

            channelButtonCardView.setVisibility(View.VISIBLE);
            channelRecycler.setVisibility(View.VISIBLE);
            surveyConstraintLayout.setVisibility(View.INVISIBLE);

            ButterKnife.bind(this, view);

            channelRecycler.setHasFixedSize(true);
            channelRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

            rListItems = new ArrayList<>();

            RecyclerListItem item1 = new RecyclerListItem("title1", "desciption1");
            RecyclerListItem item2 = new RecyclerListItem("title2", "desciption2");
            RecyclerListItem item3 = new RecyclerListItem("title3", "desciption3");
            RecyclerListItem itemU = new RecyclerListItem("title unique", "desciption unique");
            rListItems.add(item1);
            rListItems.add(item2);
            rListItems.add(item3);
            rListItems.add(item3);
            rListItems.add(itemU);
            rListItems.add(item3);


            rAdapter = new RecyclerAdapter(rListItems, getContext());
            channelRecycler.setAdapter(rAdapter);

        }

          return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // Initialize view lookups, listeners

        Button createChannelsButton = view.findViewById(R.id.button_createChannel);
        if (!seeSurvey) {
            createChannelsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "create channel clicked", Toast.LENGTH_SHORT).show();

                    // making API call
                    searchAPI(query);

                    int uid = 1;
                    String name = "testChannelTitle";

                    ClarityApp.getRestClient().createChannel(uid, name, getActivity(), new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Log.e(TAG, "onSuccess1 : " + response.toString() );
                            super.onSuccess(statusCode, headers, response);
                        }
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                            Log.e(TAG, "onSuccess2 : " + response.toString());
                            super.onSuccess(statusCode, headers, response);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.e(TAG, "onFailue1 : " + errorResponse.toString());
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                            Log.e(TAG, "onFailue2 : " + errorResponse.toString());
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Log.e(TAG, "onFailue3 : " + responseString.toString());
                            super.onFailure(statusCode, headers, responseString, throwable);
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String responseString) {
                            Log.e(TAG, "onSuccess3 : " + responseString.toString());
                            super.onSuccess(statusCode, headers, responseString);
                        }
                    });


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

    private void searchAPI(String query) {
        ClarityApp.getRestClient().getFullTextSearch(offset, query, 0, "episode", getActivity(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    offset = response.getInt("next_offset");
                    //Log.e(TAG, response.toString());
                    createChannel(response);

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

            populateRecycler(aChannel);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void populateRecycler(Channel aChannel) {

        RecyclerListItem APIresult = new RecyclerListItem(aChannel.getName(), "");

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



/*

TV & Film                   68
Sports & Recreation         77
Technology                  127
History                     125
News & Politics             99
Religion & Spirituality     69

*/
