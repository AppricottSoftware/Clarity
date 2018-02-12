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
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import appricottsoftware.clarity.R;
import appricottsoftware.clarity.RecyclerAdapter;
import appricottsoftware.clarity.RecyclerListItem;
import butterknife.BindView;
import appricottsoftware.clarity.models.Episode;
import appricottsoftware.clarity.models.PlayerInterface;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChannelFragment extends Fragment {

    @BindView(R.id.RecyclerView_Channels)
    RecyclerView channelRecycler;
    @BindView(R.id.button_createChannel)
    Button createChannelButton;
    @BindView(R.id.button_ShuffleChannel)
    Button shuffleChannelsButton;

    private RecyclerView.Adapter rAdapter;
    private List<RecyclerListItem> rListItems;

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
//        View view = inflater.inflate(R.layout.fragment_channel, container, false);
//        ButterKnife.bind(this, view);

        View view;
        if (seeSurvey == false) {
            view = inflater.inflate(R.layout.fragment_survey, container, false);

        }
        else {
            view = inflater.inflate(R.layout.fragment_channel, container, false);

            ButterKnife.bind(this, view);

            channelRecycler.setHasFixedSize(true);
            channelRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

            rListItems = new ArrayList<>();

            RecyclerListItem item1 = new RecyclerListItem("title1", "desciption1");
            RecyclerListItem item2 = new RecyclerListItem("title2", "desciption2");
            RecyclerListItem item3 = new RecyclerListItem("title3", "desciption3");
            rListItems.add(item1);
            rListItems.add(item2);
            rListItems.add(item3);
            rListItems.add(item3);
            rListItems.add(item3);
            rListItems.add(item3);
            rListItems.add(item3);
            rListItems.add(item3);
            rListItems.add(item1);
            rListItems.add(item2);
            rListItems.add(item3);
            rListItems.add(item3);
            rListItems.add(item3);
            rListItems.add(item3);
            rListItems.add(item3);
            rListItems.add(item3);

            rAdapter = new RecyclerAdapter(rListItems, getContext());
            channelRecycler.setAdapter(rAdapter);

        }
//        ButterKnife.bind(this, view);
//
//        channelRecycler.setHasFixedSize(true);
//        channelRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
//
//        rListItems = new ArrayList<>();
//
//        RecyclerListItem item1 = new RecyclerListItem("title1", "desciption1");
//        RecyclerListItem item2 = new RecyclerListItem("title2", "desciption2");
//        RecyclerListItem item3 = new RecyclerListItem("title3", "desciption3");
//        rListItems.add(item1);
//        rListItems.add(item2);
//        rListItems.add(item3);
//        rListItems.add(item3);
//        rListItems.add(item3);
//        rListItems.add(item3);
//        rListItems.add(item3);
//        rListItems.add(item3);
//        rListItems.add(item1);
//        rListItems.add(item2);
//        rListItems.add(item3);
//        rListItems.add(item3);
//        rListItems.add(item3);
//        rListItems.add(item3);
//        rListItems.add(item3);
//        rListItems.add(item3);
//
//        rAdapter = new RecyclerAdapter(rListItems, getContext());
//        channelRecycler.setAdapter(rAdapter);
//
          return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // Initialize view lookups, listeners


    }



    @OnClick(R.id.test_music)
    public void onClickTestMusic() {
        Episode testEpisode = Episode.getSampleEpisode();
        playerInterface.playEpisode(testEpisode);
    }


}
