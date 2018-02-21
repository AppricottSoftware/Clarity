package appricottsoftware.clarity.adapters;

import android.content.Context;
import android.graphics.Color;
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

import java.util.List;

import appricottsoftware.clarity.R;
import appricottsoftware.clarity.models.Channel;
import appricottsoftware.clarity.models.Episode;
import appricottsoftware.clarity.models.PlayerInterface;
import appricottsoftware.clarity.sync.ClarityApp;
import appricottsoftware.clarity.sync.ClarityClient;
import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class ChannelsAdapter extends RecyclerView.Adapter<ChannelsAdapter.ChannelViewHolder> {

    private static final String TAG = "ChannelsAdapter";

    private List<Channel> channels;
    private Context context;
    private boolean isChannelView;

    private int selected_position = 0;

    private PlayerInterface playerInterface;

    public ChannelsAdapter(List<Channel> channels, Context context, boolean isChannelView) {
        this.channels = channels;
        this.context = context;
        this.isChannelView = isChannelView;
    }

    @Override
    public ChannelsAdapter.ChannelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_recycle_view_item, parent, false);
        return new ChannelsAdapter.ChannelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChannelsAdapter.ChannelViewHolder holder, int position) {
        Channel channel = channels.get(position);

        holder.tvTitle.setText(channel.getTitle());
        Glide.with(context).load(channel.getImage()).into(holder.ivAlbum);

        holder.itemView.setBackgroundColor(selected_position == position ? Color.LTGRAY : Color.TRANSPARENT);
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }

    public class ChannelViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.imageView_album) public ImageView ivAlbum;
        @BindView(R.id.textView_title) public TextView tvTitle;

        public ChannelViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(final View view) {
            notifyItemChanged(selected_position);
            selected_position = getLayoutPosition();
            notifyItemChanged(selected_position);

            if (isChannelView){
                if(view.getContext() instanceof PlayerInterface) {
                    playerInterface = (PlayerInterface) view.getContext();
                } else {
                    Log.e(TAG, view.getContext().toString() + " must implement PlayerInterface");
                    throw new ClassCastException(view.getContext().toString() + " must implement PlayerInterface");
                }

                Channel channel = channels.get(selected_position);
                playerInterface.playChannel(channel);
            }
            else{
                int uid = ClarityApp.getSession(view.getContext()).getUserID();
                Channel channel = channels.get(selected_position);

                // TODO: re-add onSuccess and on Failure methods to be handled in future.
                ClarityApp.getRestClient(view.getContext()).createChannel(uid, channel, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        Toast.makeText(view.getContext(), "Created channel!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        try {
                            switch(statusCode) {
                                case(0):
                                    Toast.makeText(view.getContext(),"Server is down. Please try later.", Toast.LENGTH_LONG).show();
                                    break;
                                default:
                                    Log.i(TAG, "Channel onFailure. Default Switch. Status Code: " + statusCode);
                                    break;
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                    }
                });
            }
        }
    }
}
