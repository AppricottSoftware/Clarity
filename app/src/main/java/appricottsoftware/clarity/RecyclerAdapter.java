package appricottsoftware.clarity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.CustomViewHolder> {

    private List<RecyclerListItem> recyclerList_ItemList;
    private Context theContext;

    int selected_position = 0;

    public RecyclerAdapter(List<RecyclerListItem> recyclerList_ItemList, Context theContext) {
        this.recyclerList_ItemList = recyclerList_ItemList;
        this.theContext = theContext;
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
        //holder.ImageViewAlbum.setImageResource(recyclerListItem.getAlbum());

        holder.itemView.setBackgroundColor(selected_position == position ? Color.LTGRAY : Color.TRANSPARENT);
    }

    @Override
    public int getItemCount() {
        return recyclerList_ItemList.size();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //public ImageView ImageViewAlbum;
        public TextView textViewTitle;

        public CustomViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            textViewTitle = itemView.findViewById(R.id.textView_title);
            //ImageViewAlbum = itemView.findViewById(R.id.imageView_album);

        }

        @Override
        public void onClick(View view) {
            notifyItemChanged(selected_position);
            selected_position = getLayoutPosition();
            notifyItemChanged(selected_position);

            String toastText = recyclerList_ItemList.get(selected_position).getTitle();
            Toast.makeText(view.getContext(), toastText, Toast.LENGTH_SHORT).show();


            // CHANGE CHANNEL ON CLICK
            //ClarityApp.getRestClient().NEED METHOD

        }
    }


}
