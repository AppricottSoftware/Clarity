package appricottsoftware.clarity.fragments;

import android.app.Activity;
import android.drm.DrmStore;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.ArrayList;

import appricottsoftware.clarity.R;
import appricottsoftware.clarity.models.Podcast;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PlayerFragment extends Fragment /*implements View.OnClickListener*/ {

    // Collapsed state view elements
    @BindView(R.id.rl_collapse) ConstraintLayout rlCollapse;
    @BindView(R.id.iv_collapse_cover) ImageView ivCollapseCover;
    @BindView(R.id.tv_collapse_title) TextView tvCollapseTitle;
    @BindView(R.id.tv_collapse_description) TextView tvCollapseDescription;
    @BindView(R.id.ib_collapse_play_pause) ImageButton ibCollapsePlayPause;
    @BindView(R.id.ib_collapse_skip) ImageButton ibCollapseSkip;

    // Expanded state view elements
    @BindView(R.id.rl_expand) ConstraintLayout rlExpand;
    @BindView(R.id.ib_expand_like) ImageButton ibExpandLike;
    @BindView(R.id.ib_expand_dislike) ImageButton ibExpandDislike;
    @BindView(R.id.ib_expand_play_pause) ImageButton ibExpandPlayPause;
    @BindView(R.id.ib_expand_skip) ImageButton ibExpandSkip;
    @BindView(R.id.tv_expand_speed) TextView tvExpandSpeed;
    @BindView(R.id.sb_expand_seek) AppCompatSeekBar sbExpandSeek;
    @BindView(R.id.tv_expand_time_elapsed) TextView tvExpandTimeElapsed;
    @BindView(R.id.tv_expand_time_remaining) TextView tvExpandTimeRemaining;
    @BindView(R.id.tv_expand_title) TextView tvExpandTitle;
    @BindView(R.id.tv_expand_description) TextView tvExpandDescription;
    @BindView(R.id.iv_expand_cover) ImageView ivExpandCover;

    private static final String TAG = "PlayerFragment";

    private MediaMetadataCompat currentMetadata;
    private boolean currentPlayState;

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

        // Set marquee scrolling
        tvExpandTitle.setSelected(true);
        tvExpandDescription.setSelected(true);
        tvCollapseTitle.setSelected(true);
        tvCollapseDescription.setSelected(true);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void openPanel() {
        // Hide/show view elements to make fragment full screen
        rlExpand.setVisibility(View.VISIBLE);
        rlCollapse.setVisibility(View.GONE);
    }


    public void closePanel() {
        // Hide/show view elements to make fragment bottom strip
        rlExpand.setVisibility(View.GONE);
        rlCollapse.setVisibility(View.VISIBLE);
    }

    public void onPlaybackStateChanged(PlaybackStateCompat state, MediaMetadataCompat metadata) {
        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                Log.d(TAG, "onPlaybackStateChanged: State is playing " + state.getState());
                setPlayPauseControls(true);
                setSeekBarControls((int) state.getPosition(), (int) state.getBufferedPosition());
                onMetadataChanged(metadata);
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                Log.d(TAG, "onPlaybackStateChanged: State is paused " + state.getState());
                setPlayPauseControls(false);
                setSeekBarControls((int) state.getPosition(), (int) state.getBufferedPosition());
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                Log.d(TAG, "onPlaybackStateChanged: State is stopped " + state.getState());
                setPlayPauseControls(false);
                setSeekBarControls((int) state.getPosition(), (int) state.getBufferedPosition());
                break;
            case PlaybackStateCompat.STATE_ERROR:
                Log.d(TAG, "onPlaybackStateChanged: Error state " + state.getErrorMessage());
                break;
            default:
                Log.d(TAG, "onPlaybackStateChanged: Unhandled case " + state.getState());
                break;
        }
    }

    public void onMetadataChanged(MediaMetadataCompat metadata) {
        // Load changed details into player fragment if the metadata has changed
        if (metadata != null
                && (currentMetadata == null
                || (!metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)
                .equals(currentMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI))))) {
            tvCollapseTitle.setText(metadata.getDescription().getTitle());
            tvExpandTitle.setText(metadata.getDescription().getTitle());
            tvCollapseDescription.setText(metadata.getDescription().getDescription());
            tvExpandDescription.setText(metadata.getDescription().getDescription());
            Glide.with(getContext()).load(metadata.getDescription().getIconUri()).into(ivCollapseCover);
            Glide.with(getContext()).load(metadata.getDescription().getIconUri()).into(ivExpandCover);

            if (currentMetadata != null)
                Log.d(TAG, "OnMetadataChanged: " + metadata.toString() + " " + currentMetadata.toString());
            currentMetadata = metadata;
        }
    }

    public void buildTransportControls(@NonNull final Activity activity) {
        setControlOnClick(activity, ibCollapsePlayPause);
        setControlOnClick(activity, ibExpandPlayPause);
        setSeekBarDrag(activity, sbExpandSeek);
        // TODO: playback speed controls

    }

    private void setControlOnClick(@NonNull final Activity activity, ImageButton imageButton) {
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (MediaControllerCompat.getMediaController(activity).getPlaybackState().getState()) {
                    case PlaybackStateCompat.STATE_PLAYING:
                        // If audio is playing, call the service to pause
                        MediaControllerCompat.getMediaController(activity).getTransportControls().pause();
                        break;
                    default:
                        // If audio is not playing, call the service to play audio
                        MediaControllerCompat.getMediaController(activity).getTransportControls().play();
                        break;
                }
            }
        });
    }

    private void setSeekBarDrag(@NonNull final Activity activity, SeekBar seekBar) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Log.d(TAG, "setSeekBarDrag: onProgressChanged: " + progress);
                    // If the user drags the seekbar, call the service to seek to a position (in milliseconds)
                    MediaControllerCompat.getMediaController(activity).getTransportControls().seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setPlayPauseControls(boolean play) {
        if (currentPlayState != play) {
            // If paused, set the play/pause button to play
            int rDrawable = R.drawable.ic_player_play;
            // If playing, set the play/pause button to pause
            if (play) {
                rDrawable = R.drawable.ic_player_pause;
            }

            // Set both the expanded and collapsed view elements
            ibCollapsePlayPause.setImageResource(rDrawable);
            ibExpandPlayPause.setImageResource(rDrawable);

            // Update the current play state
            currentPlayState = play;
        }
    }

    private void setSeekBarControls(int progress, int totalTime) {
        if (progress <= totalTime) {
            // Total time is given in milliseconds - convert to seconds
            sbExpandSeek.setMax(totalTime / 1000);
            sbExpandSeek.setProgress(progress / 1000);

            // Set the elapsed time
            String timeElapsed = DurationFormatUtils.formatDuration(progress, "HH:mm:ss", true);
            tvExpandTimeElapsed.setText(timeElapsed);

            // Set the remaining time
            String timeRemaining = DurationFormatUtils.formatDuration(totalTime - progress, "HH:mm:ss", true);
            tvExpandTimeRemaining.setText(timeRemaining);
        }
    }
}