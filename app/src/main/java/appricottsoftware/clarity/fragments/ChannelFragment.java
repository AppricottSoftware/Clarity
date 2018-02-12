package appricottsoftware.clarity.fragments;

import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectorResult;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import appricottsoftware.clarity.R;
import appricottsoftware.clarity.RecyclerAdapter;
import appricottsoftware.clarity.RecyclerListItem;
import appricottsoftware.clarity.SurveyActivity;
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

    /*
    random number 10 - 20
    int random = new Random().nextInt(21) + 10;
     */

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

        if (seeSurvey == true) {

            //view = inflater.inflate(R.layout.activity_survey, container);
            Intent surveyActivityIntent = new Intent(getActivity(), SurveyActivity.class);
            surveyActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(surveyActivityIntent);
        }


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
