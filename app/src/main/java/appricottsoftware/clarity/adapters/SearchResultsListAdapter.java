package appricottsoftware.clarity.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import appricottsoftware.clarity.R;
import appricottsoftware.clarity.models.Podcast;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by karen on 1/16/18.
 */

public class SearchResultsListAdapter extends RecyclerView.Adapter<SearchResultsListAdapter.ViewHolder> {

    private ArrayList<Podcast> podcasts;
    private Context context;

    public SearchResultsListAdapter(ArrayList<Podcast> podcasts) { this.podcasts = podcasts; }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View podcastView = inflater.inflate(R.layout.item_searchresult, parent, false);
        ViewHolder viewHolder = new ViewHolder(context, podcastView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Podcast podcast = podcasts.get(position);
        holder.podcastName.setText(podcast.getPodcast_title_original());
        holder.podcastDescription.setText(podcast.getDescription_original());
        Log.e("Adapter", podcast.getImage());
        Glide.with(context).load(podcast.getImage()).into(holder.podcastImage);
    }

    @Override
    public int getItemCount() {
        return podcasts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivSearchResultImage) public ImageView podcastImage;
        @BindView(R.id.tvSearchResultName) public TextView podcastName;
        @BindView(R.id.tvSearchResultDescription) public TextView podcastDescription;

        public ViewHolder(Context context, View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
