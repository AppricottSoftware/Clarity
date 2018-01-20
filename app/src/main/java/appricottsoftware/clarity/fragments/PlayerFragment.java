package appricottsoftware.clarity.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import appricottsoftware.clarity.R;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by karen on 1/19/18.
 */

public class PlayerFragment extends Fragment {

    @BindView(R.id.tv_player_fragment_open) TextView tvPlayerFragmentOpen;
    @BindView(R.id.tv_player_fragment_closed) TextView tvPlayerFragmentClosed;

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

    // Hide/show view elements to make fragment full screen
    public void openPanel() {
        tvPlayerFragmentOpen.setVisibility(View.VISIBLE);
        tvPlayerFragmentClosed.setVisibility(View.GONE);
    }

    // Hide/show view elements to make fragment bottom strip
    public void closePanel() {
        tvPlayerFragmentOpen.setVisibility(View.GONE);
        tvPlayerFragmentClosed.setVisibility(View.VISIBLE);
    }
}
