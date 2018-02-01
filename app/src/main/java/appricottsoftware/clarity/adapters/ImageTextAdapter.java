package appricottsoftware.clarity.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import appricottsoftware.clarity.R;
import appricottsoftware.clarity.models.Podcast;

/**
 * Created by ttony on 1/30/2018.
 */

public class ImageTextAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Podcast> podcasts;

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
            holder.imageView = convertView.findViewById(R.id.ivBrowseImg);
            holder.imageView.setLayoutParams(new LinearLayout.LayoutParams(450, 450));
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.imageView.setPadding(8, 8, 8, 8);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.imageView.setImageResource(podcasts.get(position).getItunes_id());
        holder.textView.setText(podcasts.get(position).getTitle_original());

        return convertView;
    }

    static class ViewHolder {
        ImageView imageView;
        TextView textView;
    }

    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.sample_0, R.drawable.sample_1,
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7,
            R.drawable.sample_0, R.drawable.sample_1,
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7
    };
}
