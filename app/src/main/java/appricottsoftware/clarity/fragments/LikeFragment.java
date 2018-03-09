package appricottsoftware.clarity.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.exoplayer2.Player;

import appricottsoftware.clarity.R;
import appricottsoftware.clarity.models.PlayerInterface;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LikeFragment extends Fragment {

    private static final String TAG = "LikeFragment";

    private PlayerInterface playerInterface;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_like, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // Initialize view lookups, listeners
    }

    @Override
    public void onAttach(Context context) {
        if(context instanceof PlayerInterface) {
            playerInterface = (PlayerInterface) context;
        } else {
            Log.e(TAG, "Context needs to be an instance of PlayerInterface");
            throw new ClassCastException("Invalid context");
        }
        super.onAttach(context);
    }
}
