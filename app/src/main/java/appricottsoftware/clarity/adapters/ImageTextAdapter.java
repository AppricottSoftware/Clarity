package appricottsoftware.clarity.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import appricottsoftware.clarity.R;
import appricottsoftware.clarity.fragments.BrowseFragment;
import appricottsoftware.clarity.models.Podcast;

/**
 * Created by ttony on 1/30/2018.
 */

public class ImageTextAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Podcast> podcasts;

    private static final String TAG = "ImageTextAdapter";

    public ImageTextAdapter(Context c, ArrayList<Podcast> p) {
        context = c;
        podcasts = p;
    }

    public int getCount() {
        return podcasts.size();
    }

    public Object getItem(int position) {
        return podcasts.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_browse_result, parent, false);
            holder.textView = convertView.findViewById(R.id.ivBrowseEpisodeName);
            holder.textView.setSelected(true);
            holder.imageView = convertView.findViewById(R.id.ivBrowseImg);
            holder.imageView.setPadding(8, 8, 8, 8);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

//        holder.imageView.setImageResource((int)podcasts.get(position).getItunes_id());
        Glide.with(context).load(podcasts.get(position).getImage()).into(holder.imageView);
        holder.textView.setText(podcasts.get(position).getTitle_original());

        return convertView;
    }

    static class ViewHolder {
        ImageView imageView;
        TextView textView;
    }
}
