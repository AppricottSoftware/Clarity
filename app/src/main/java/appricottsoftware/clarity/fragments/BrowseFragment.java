package appricottsoftware.clarity.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;

import appricottsoftware.clarity.HomeActivity;
import appricottsoftware.clarity.R;
import appricottsoftware.clarity.adapters.ImageAdapter;
import appricottsoftware.clarity.models.Podcast;
import butterknife.ButterKnife;

public class BrowseFragment extends Fragment {

    private Integer[] images = {
            R.drawable.sample_0, R.drawable.sample_1,
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7,
            R.drawable.sample_0, R.drawable.sample_1,
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7
    };

    private String[] names = {
            "Henry", "James",
            "Tony", "Mathew",
            "Karen", "Gus",
            "Patrick", "Jake",
            "Gabriel", "Andrew",
            "Joseph", "Charmaine",
            "Joe", "Xhoni",
            "Aleina", "Raymond"
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // Initialize view lookups, listeners

        GridView gridview = getActivity().findViewById(R.id.gv_browse);
        gridview.setAdapter(new ImageAdapter(getActivity(), populatePodcasts()));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(getActivity(), "" + position,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private ArrayList<Podcast> populatePodcasts() {
        ArrayList<Podcast> podcasts = new ArrayList<Podcast>();

        for (int i = 0; i < images.length; i++) {
            Podcast p = new Podcast();

            // Using setItunes_Id temporary as the images attribute since my images
            // are int's, not strings.
            p.setItunes_id(images[i]);
            p.setTitle_original(names[i]);
            podcasts.add(p);
        }

        return podcasts;
    }
}
