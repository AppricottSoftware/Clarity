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
import android.widget.Toast;

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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog, attach it to HomeActivity, set parent view to null to go in dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_playback_speed_dialog, null);

        // Note: Manual binding without Butterknife because we are using Dialog onCreate
        sbSpeed = view.findViewById(R.id.sb_playback_speed_dialog);
        tvSpeed = view.findViewById(R.id.tv_playback_speed_dialog);

        // Set the initial state of the playback speed
        initializeSeekBar();

        // Configure the dialog fragment
        builder.setIcon(android.R.drawable.ic_media_play)
                .setTitle(getActivity().getString(R.string.fragment_dialog_playback_speed_title))
                .setView(view)
                .setPositiveButton(getActivity().getString(R.string.fragment_dialog_playback_speed_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setSpeed(sbSpeed.getProgress());
                    }
                })
                .setNegativeButton(getActivity().getString(R.string.fragment_dialog_playback_speed_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        // Create the dialog fragment
        return builder.create();
    }

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

    private void initializeSeekBar() {
        // Set the seekbar and text to show the user's current playback speed
        float playbackSpeed = ClarityApp.getSession(getActivity()).getPlaybackSpeed();
        sbSpeed.setProgress(playbackSpeedToProgress(playbackSpeed));
        tvSpeed.setText(playbackSpeed + "x");
        setSeekBarListener();
    }

    private void setSeekBarListener() {
        sbSpeed.setOnSeekBarChangeListener(new AppCompatSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    showSpeed(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }


    private int playbackSpeedToProgress(float speed) {
        // Convert from [0.5, 3.0] to progress [0, 5] for the seekbar
        return (int) ((speed * 2) - 1);
    }

    private float progressToPlaybackSpeed(int progress) {
        // Convert the raw progress [0, 5] to playback speed [0.5, 3.0]
        return ((float) progress + 1.0f) / 2.0f;
    }

    private void showSpeed(int progress) {
        // Update the text that shows the playback speed
        float playbackSpeed = progressToPlaybackSpeed(progress);
        tvSpeed.setText(playbackSpeed + "x");
    }

    private void setSpeed(int progress) {
        final float playbackSpeed = progressToPlaybackSpeed(progress);

        // Update the backend with the new progress
        int uid = ClarityApp.getSession(getContext()).getUserID();
        ClarityApp.getRestClient().updatePlaybackSpeed(uid, playbackSpeed, getContext(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                saveSpeed(playbackSpeed);
                Log.v(TAG, "SetSpeed: Success: " + playbackSpeed);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                saveSpeed(playbackSpeed);

                // Alert the user the server's not responding
                Toast.makeText(getContext(), "Unable to sync playback speed", Toast.LENGTH_LONG).show();
                Log.e(TAG, "SetSpeed: Failure", throwable);
            }
        });
    }

    private void saveSpeed(final float playbackSpeed) {
        // Save the playback speed to shared preferences
        ClarityApp.getSession(getActivity()).setPlaybackSpeed(playbackSpeed);

        // Alert the parent activity the playback speed has been updated
        dialogListener.onDialogOK(playbackSpeed);
    }
}
