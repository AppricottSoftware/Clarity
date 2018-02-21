package appricottsoftware.clarity.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import appricottsoftware.clarity.R;
import appricottsoftware.clarity.models.Channel;
import appricottsoftware.clarity.models.Podcast;
import butterknife.OnClick;

/**
 * Created by ttony on 1/30/2018.
 */

public class BrowseAdapter extends BaseAdapter {
    private View view;
    private Context context;
    private ArrayList<Channel> channels;

    private static final String TAG = "ImageTextAdapter";

    public BrowseAdapter(Context c, ArrayList<Channel> ch) {
        context = c;
        channels = ch;
    }

    public int getCount() {
        return channels.size();
    }

    public Object getItem(int position) {
        return channels.get(position);
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
            holder.imageView = convertView.findViewById(R.id.ivBrowseImg);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        Glide.with(context).load(channels.get(position).getImage()).into(holder.imageView);
        holder.textView.setText(channels.get(position).getTitle());

        view = convertView;
        return convertView;
    }

    public class ViewHolder {
        ImageView imageView;
        TextView textView;
    }
}
