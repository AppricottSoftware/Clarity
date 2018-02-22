package appricottsoftware.clarity.adapters;

import android.app.Fragment;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import appricottsoftware.clarity.R;
import appricottsoftware.clarity.models.Channel;
import appricottsoftware.clarity.models.FragmentListener;
import appricottsoftware.clarity.models.PlayerInterface;
import appricottsoftware.clarity.sync.ClarityApp;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;

public class ChannelSearchAdapter extends RecyclerView.Adapter<ChannelSearchAdapter.ChannelSearchViewHolder> {

    private static final String TAG = "ChannelSearchAdapter";

    private List<Channel> channels;
    private Context context;
    private FragmentListener fragmentListener;

    private int selected_position = 0;

    public ChannelSearchAdapter(List<Channel> cl, Context ct, FragmentListener fl) {
        channels = cl;
        context = ct;
        fragmentListener = fl;
    }

    @Override
    public ChannelSearchAdapter.ChannelSearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_channel_adapter, parent, false);
        return new ChannelSearchAdapter.ChannelSearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChannelSearchAdapter.ChannelSearchViewHolder holder, int position) {
        Channel channel = channels.get(position);
        // Populate the view holder with the channel's attributes
        holder.tvTitle.setText(channel.getTitle());
        Glide.with(context).load(channel.getImage()).into(holder.ivAlbum);
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }

    public class ChannelSearchViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imageView_album) public ImageView ivAlbum;
        @BindView(R.id.textView_title) public TextView tvTitle;

        public ChannelSearchViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick
        void onClick(View view) {
            int uid = ClarityApp.getSession(context).getUserID();
            Channel channel = channels.get(selected_position);

            // TODO: re-add onSuccess and on Failure methods to be handled in future.
            ClarityApp.getRestClient(context).createChannel(uid, channel, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Toast.makeText(context, "Created channel!", Toast.LENGTH_LONG).show();
                    // Return to the home fragment
                    fragmentListener.returnToHomeFragment();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    switch(statusCode) {
                        case(0):
                            Toast.makeText(context,"Server is down. Please try later.", Toast.LENGTH_LONG).show();
                            break;
                        default:
                            Log.e(TAG, "Channel onFailure. Default Switch. Status Code: " + statusCode);
                            break;
                    }
                }
            });
        }
    }
}
