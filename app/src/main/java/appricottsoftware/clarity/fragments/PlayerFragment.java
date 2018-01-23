package appricottsoftware.clarity.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import appricottsoftware.clarity.R;
import appricottsoftware.clarity.models.Podcast;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PlayerFragment extends Fragment implements View.OnClickListener {

    // Collapsed state view elements
    @BindView(R.id.rl_collapse) ConstraintLayout rlCollapse;
    @BindView(R.id.iv_collapse_cover) ImageView ivCollapseCover;
    @BindView(R.id.tv_collapse_title) TextView tvCollapseTitle;
    @BindView(R.id.tv_collapse_description) TextView tvCollapseDescription;
    @BindView(R.id.ib_collapse_play) ImageButton ibCollapsePlay;
    @BindView(R.id.ib_collapse_pause) ImageButton ibCollapsePause;
    @BindView(R.id.ib_collapse_skip) ImageButton ibCollapseSkip;

    // Expanded state view elements
    @BindView(R.id.rl_expand) ConstraintLayout rlExpand;
    @BindView(R.id.ib_expand_like) ImageButton ibExpandLike;
    @BindView(R.id.ib_expand_dislike) ImageButton ibExpandDislike;
    @BindView(R.id.ib_expand_play) ImageButton ibExpandPlay;
    @BindView(R.id.ib_expand_pause) ImageButton ibExpandPause;
    @BindView(R.id.ib_expand_skip) ImageButton ibExpandSkip;
    @BindView(R.id.tv_expand_speed) TextView tvExpandSpeed;
    @BindView(R.id.sb_expand_seek) AppCompatSeekBar sbExpandSeek;
    @BindView(R.id.tv_expand_time_elapsed) TextView tvExpandTimeElapsed;
    @BindView(R.id.tv_expand_time_remaining) TextView tvExpandTimeRemaining;
    @BindView(R.id.tv_expand_title) TextView tvExpandTitle;
    @BindView(R.id.tv_expand_description) TextView tvExpandDescription;
    @BindView(R.id.iv_expand_cover) ImageView ivExpandCover;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // Initialize view lookups, listeners

    }

    @Override
    public void onClick(View v) {

    }

    // Hide/show view elements to make fragment full screen
    public void openPanel() {
        rlExpand.setVisibility(View.VISIBLE);
        rlCollapse.setVisibility(View.GONE);

        // Set marquee scrolling
        tvExpandTitle.setSelected(true);
        tvExpandDescription.setSelected(true);
    }

    // Hide/show view elements to make fragment bottom strip
    public void closePanel() {
        rlExpand.setVisibility(View.GONE);
        rlCollapse.setVisibility(View.VISIBLE);

        // Set marquee scrolling
        tvCollapseTitle.setSelected(true);
        tvCollapseDescription.setSelected(true);
    }

    public void loadPlaylist(ArrayList<Podcast> podcasts) {

    }

    public void play(Podcast podcast) {

    }

    public void pause() {

    }

    public void skip() {

    }
}
