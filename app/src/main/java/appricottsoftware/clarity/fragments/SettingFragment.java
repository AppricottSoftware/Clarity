package appricottsoftware.clarity.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import appricottsoftware.clarity.R;
import appricottsoftware.clarity.sync.ClarityApp;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingFragment extends Fragment {

    private static final String TAG = "SettingFragment";

    @BindView(R.id.sb_setting_length_set) AppCompatSeekBar sbLength;
    @BindView(R.id.tv_setting_progress) TextView tvProgress;
    @BindView(R.id.tv_setting_length_current) TextView tvCurrent;
    @BindView(R.id.Email) TextView email;
    @BindView(R.id.Password) TextView password;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // Initialize view lookups, listener
        // TODO: write a listener for when fragment is done drawing UI
        tvProgress.setVisibility(View.GONE);
        setLengthText();
        setSeekBar();
    }

    private void setSeekBar() {
        // Get the last used max length and set the seekbar to the max length

        sbLength.setOnSeekBarChangeListener(new AppCompatSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (tvProgress.getVisibility() == View.GONE) {
                    tvProgress.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (tvProgress.getVisibility() == View.VISIBLE) {
                    tvProgress.setVisibility(View.GONE);
                }
                setLengthText();
            }
        });
    }

    private void setProgress(int progress) {
        Log.e(TAG, "Progress: " + progress);
        // Round the progress to the nearest 10
        progress = ((progress + 5) / 10) * 10;
        // Set the text and progress bar
        sbLength.setProgress(progress);
        tvProgress.setText(getLengthText(progress));
        ClarityApp.getSession(getContext()).setMaxLength(progress);
        int progressLeftX = sbLength.getLeft() + sbLength.getPaddingLeft();
        int progressRightX = sbLength.getRight() - sbLength.getPaddingRight();
        int progressX = (((progressRightX - progressLeftX) * progress) / sbLength.getMax()) + progressLeftX;
        progressX -= tvProgress.getWidth() / 2;
        tvProgress.setX(progressX);
        Log.e(TAG, "setProgress: " + sbLength.getRight() + " " + sbLength.getPaddingRight() + " lX: " + progressLeftX + " rX: " + progressRightX + " X: " + progressX);
    }

    private String getLengthText(int length) {
        if(length <= 0) {
            return "off";
        }
        return Integer.toString(length);
    }

    private void setLengthText() {
        int length = ClarityApp.getSession(getContext()).getMaxLength();
        tvCurrent.setText(getLengthText(length));
        setProgress(length);
        Log.e(TAG, "Length: " + length);
    }
}
