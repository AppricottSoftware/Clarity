package appricottsoftware.clarity.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import appricottsoftware.clarity.R;
import appricottsoftware.clarity.models.Episode;
import butterknife.BindView;
import butterknife.ButterKnife;

public class LikeFragment extends Fragment {

    private static final String TAG = "LikeFragment";

    @BindView(R.id.likesRecyclerView) RecyclerView likesRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_like, container, false);
        ButterKnife.bind(this, view);

        likesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // this is data fro recycler view
        Episode itemsData;


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // Initialize view lookups, listeners
    }


}
