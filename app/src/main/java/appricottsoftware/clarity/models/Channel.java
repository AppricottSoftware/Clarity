package appricottsoftware.clarity.models;

import android.content.Context;
import android.util.Log;

import org.apache.commons.collections4.CollectionUtils;
import org.parceler.Parcel;

import java.util.ArrayList;

import appricottsoftware.clarity.R;
import appricottsoftware.clarity.sync.ClarityApp;

@Parcel
public class Channel {

    int cid;
    int uid;
    String title;
    String image;
    ArrayList<Metadata> metadata;

    public Channel() { /* Empty constructor required by GSON and Parcel */ }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public ArrayList<Metadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(ArrayList<Metadata> metadata) {
        this.metadata = metadata;
    }

    public String toString() {
        return ClarityApp.getGson().toJson(this);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }

        if(!(obj instanceof Channel)) {
            return false;
        }

        Channel c = (Channel) obj;
        int comMetadataSize = CollectionUtils.retainAll(metadata, c.getMetadata()).size();

        return cid == c.cid
                && uid == c.uid
                && title.equals(c.title)
                && image.equals(c.image)
                && comMetadataSize == metadata.size()
                && comMetadataSize == c.getMetadata().size();
    }

    public String getGenreIds() {
        String genre_ids = "";
        for(Metadata m : metadata) {
            genre_ids += m.getMid() + ", ";
        }
        if(genre_ids.length() > 2) {
            genre_ids = genre_ids.substring(0, genre_ids.lastIndexOf(','));
        }
        return genre_ids;
    }

    public String getSearchTerm(Context context) {
        if(metadata == null || metadata.size() == 0) {
            Log.e("Channel", "getSearchTerm: No metadata");
            return context.getString(R.string.channel_search_term);
        }
        return metadata.get(0).getGenre();
    }

    public static Channel getSampleChannel() {
        Channel channel = new Channel();
        channel.setCid(123);
        channel.setUid(1);
        channel.setTitle("Net Neutrality");
        channel.setImage("https://cdn-images-1.medium.com/max/1200/1*_Q5d0q-_GumdWTFwXxVcuQ.jpeg");
        ArrayList<Metadata> meta = new ArrayList<>();
        meta.add(new Metadata("Professional", 78, 12));
        meta.add(new Metadata("Business", 93, 10));
        channel.setMetadata(meta);
        return channel;
    }

    public static Channel getSampleChannel2() {
        Channel channel = new Channel();
        channel.setCid(124);
        channel.setUid(1);
        channel.setTitle("Android");
        channel.setImage("https://cdn-images-1.medium.com/max/1200/1*_Q5d0q-_GumdWTFwXxVcuQ.jpeg");
        ArrayList<Metadata> meta = new ArrayList<>();
        meta.add(new Metadata("Professional", 78, 12));
        meta.add(new Metadata("Business", 93, 10));
        channel.setMetadata(meta);
        return channel;
    }

    public void addMetadata(Metadata metadata) {
        if (this.metadata != null) {
            this.metadata.add(metadata);
        }
        else {
            this.metadata = new ArrayList<>();
            this.metadata.add(metadata);
        }
    }
}
