package appricottsoftware.clarity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.IOException;
import java.net.URL;
import java.util.List;

import appricottsoftware.clarity.sync.ClarityApp;
import cz.msebera.android.httpclient.Header;


public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.CustomViewHolder> {

    private static final String TAG = "RecyclerAdapter";

    private List<RecyclerListItem> recyclerList_ItemList;
    private Context theContext;
    private boolean isChannelView;

    int selected_position = 0;

    public RecyclerAdapter(List<RecyclerListItem> recyclerList_ItemList, Context theContext, boolean isChannelView) {
        this.recyclerList_ItemList = recyclerList_ItemList;
        this.theContext = theContext;
        this.isChannelView = isChannelView;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View theView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_recycle_view_item, parent, false);
        return new CustomViewHolder(theView);

    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        RecyclerListItem recyclerListItem = recyclerList_ItemList.get(position);

        holder.textViewTitle.setText(recyclerListItem.getTitle());
        Glide.with(theContext).load(recyclerListItem.getImage()).into(holder.ImageViewAlbum);

        holder.itemView.setBackgroundColor(selected_position == position ? Color.LTGRAY : Color.TRANSPARENT);
    }

    @Override
    public int getItemCount() {
        return recyclerList_ItemList.size();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView ImageViewAlbum;
        public TextView textViewTitle;

        public CustomViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            textViewTitle = itemView.findViewById(R.id.textView_title);
            ImageViewAlbum = itemView.findViewById(R.id.imageView_album);

        }

        @Override
        public void onClick(final View view) {
            notifyItemChanged(selected_position);
            selected_position = getLayoutPosition();
            notifyItemChanged(selected_position);

            if (isChannelView){
                Toast.makeText(view.getContext(),"You are in CHANNEL View", Toast.LENGTH_SHORT).show();

                //TODO pass channel to Player fragment
            }
            else{
                Toast.makeText(view.getContext(),"CHANNEL ADDED TO DATABASE AND CHANNEL LIST", Toast.LENGTH_SHORT).show();

                int uid = 1;
                String title = recyclerList_ItemList.get(selected_position).getTitle();
                String imageURL = recyclerList_ItemList.get(selected_position).getImage();

                ClarityApp.getRestClient().createChannel(uid, title, imageURL, view.getContext(), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.e(TAG, "onSuccess1 : " + response.toString() );
                        super.onSuccess(statusCode, headers, response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        try {
                            switch(statusCode) {
                                case(0):
                                    Toast.makeText(view.getContext(),
                                            "Server is down. Please try later.",
                                            Toast.LENGTH_LONG).show();
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
