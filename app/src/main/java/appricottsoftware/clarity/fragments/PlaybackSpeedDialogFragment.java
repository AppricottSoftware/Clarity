package appricottsoftware.clarity.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.drm.DrmStore;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import appricottsoftware.clarity.R;
import appricottsoftware.clarity.sync.ClarityApp;
import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class PlaybackSpeedDialogFragment extends DialogFragment {

    private static final String TAG = "PlaybackSpeedDialog";

    //@BindView(R.id.sb_playback_speed_dialog)
    AppCompatSeekBar sbSpeed;
    //@BindView(R.id.tv_playback_speed_dialog)
    TextView tvSpeed;

    // Listen to clicks
    public interface PlaybackSpeedDialogListener {
        void onDialogOK(float speed);
    }

    private PlaybackSpeedDialogListener dialogListener;
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_playback_speed_dialog, container, false);
//        ButterKnife.bind(this, view);
//        return view;
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        setSpeed(sbSpeed.getProgress());
//    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_playback_speed_dialog, null);

        sbSpeed = (AppCompatSeekBar) view.findViewById(R.id.sb_playback_speed_dialog);
        tvSpeed = (TextView) view.findViewById(R.id.tv_playback_speed_dialog);
        initializeSeekBar();

        //        // Inflate the fragment layout, set parent view to null to go in dialog
        builder.setIcon(android.R.drawable.ic_media_play)
                .setTitle("Playback Speed")
                .setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setSpeed(sbSpeed.getProgress());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        Dialog d = builder.create();
        return d;
    }



//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
////        sbSpeed = (AppCompatSeekBar) getActivity().findViewById(R.id.sb_playback_speed_dialog);
////        tvSpeed = (TextView) getActivity().findViewById(R.id.tv_playback_speed_dialog);
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
////        initializeSeekBar();
//    }
//
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof PlaybackSpeedDialogListener) {
            dialogListener = (PlaybackSpeedDialogListener) context;
        } else {
            Log.e(TAG, "Calling activity must implement PlaybackSpeedDialogListener");
            throw new ClassCastException("Must implement PlaybackSpeedDialogListener");
        }
    }

    private void setSeekBarListener() {
        sbSpeed.setOnSeekBarChangeListener(new AppCompatSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    showSpeed(progress);
                    Log.e(TAG, "onProgressChanged: " + progress);
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

    private void initializeSeekBar() {
        float playbackSpeed = ClarityApp.getSession(getActivity()).getPlaybackSpeed();
        Log.e(TAG, "initializeSeekbar: " + playbackSpeed);
        sbSpeed.setProgress(playbackSpeedToProgress(playbackSpeed));
        tvSpeed.setText(playbackSpeed + "x");
        setSeekBarListener();
    }

    private int playbackSpeedToProgress(float speed) {
        // Convert from 0.5, 1.0, ... 3.0 to progress for the seekbar
        return (int) ((speed * 2) - 1);
    }

    private float progressToPlaybackSpeed(int progress) {
        return ((float) progress + 1.0f) / 2.0f;
    }

    private void showSpeed(int progress) {
        float playbackSpeed = progressToPlaybackSpeed(progress);
        tvSpeed.setText(playbackSpeed + "x");
    }

    private void setSpeed(int progress) {
        final float playbackSpeed = progressToPlaybackSpeed(progress);
        dialogListener.onDialogOK(playbackSpeed);
        tvSpeed.setText(playbackSpeed + "x");
        int uid = ClarityApp.getSession(getContext()).getUserID();
        ClarityApp.getRestClient().updatePlaybackSpeed(uid, playbackSpeed, getContext(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ClarityApp.getSession(getActivity()).setPlaybackSpeed(playbackSpeed);
                dialogListener.onDialogOK(playbackSpeed);
                Log.e(TAG, "SetSpeed: Success");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(TAG, "SetSpeed: Failure");
            }
        });
    }
}
