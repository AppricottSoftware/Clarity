package appricottsoftware.clarity.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import appricottsoftware.clarity.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PlayerFragment extends Fragment {

//    @BindView(R.id.tv_player_fragment_open) TextView tvPlayerFragmentOpen;
//    @BindView(R.id.tv_player_fragment_closed) TextView tvPlayerFragmentClosed;
    @BindView(R.id.rl_player_fragment_expanded) ConstraintLayout rlPlayerFragmentExpanded;
    @BindView(R.id.rl_player_fragment_collapsed) ConstraintLayout rlPlayerFragmentCollapsed;

    @BindView(R.id.tv_player_fragment_collapse_title) TextView tvPlayerFragmentCollapseTitle;
    @BindView(R.id.tv_player_fragment_collapse_description) TextView tvPlayerFragmentCollapseDescription;

    @BindView(R.id.tv_player_fragment_expand_title) TextView tvPlayerFragmentExpandTitle;
    @BindView(R.id.tv_player_fragment_expand_description) TextView tvPlayerFragmentExpandDescription;

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
        tvPlayerFragmentCollapseTitle.setSelected(true);
        tvPlayerFragmentCollapseDescription.setSelected(true);

        tvPlayerFragmentExpandTitle.setSelected(true);
        tvPlayerFragmentExpandDescription.setSelected(true);

    }

    // Hide/show view elements to make fragment full screen
    public void openPanel() {
        rlPlayerFragmentExpanded.setVisibility(View.VISIBLE);
        rlPlayerFragmentCollapsed.setVisibility(View.GONE);
//        tvPlayerFragmentOpen.setVisibility(View.VISIBLE);
//        tvPlayerFragmentClosed.setVisibility(View.GONE);
    }

    // Hide/show view elements to make fragment bottom strip
    public void closePanel() {
        rlPlayerFragmentExpanded.setVisibility(View.GONE);
        rlPlayerFragmentCollapsed.setVisibility(View.VISIBLE);
//        tvPlayerFragmentOpen.setVisibility(View.GONE);
//        tvPlayerFragmentClosed.setVisibility(View.VISIBLE);
    }
}
