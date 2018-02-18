package appricottsoftware.clarity.models;

import org.parceler.Parcel;

import java.util.ArrayList;

import appricottsoftware.clarity.sync.ClarityApp;

@Parcel
public class Channel {

    int cid;
    int uid;
    String name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getGenreIds() {
        String genre_ids = "";
        for(Metadata m : metadata) {
            genre_ids += m.getMid() + ", ";
        }
        if(genre_ids.length() > 2) {
            genre_ids = genre_ids.substring(0, genre_ids.length() - 2);
        }
        return genre_ids;
    }

    public static Channel getSampleChannel() {
        Channel channel = new Channel();
        channel.setCid(123);
        channel.setUid(1);
        channel.setName("Net Neutrality");
        channel.setImage("https://cdn-images-1.medium.com/max/1200/1*_Q5d0q-_GumdWTFwXxVcuQ.jpeg");
        ArrayList<Metadata> meta = new ArrayList<>();
        meta.add(new Metadata("Professional", 78, 12));
        meta.add(new Metadata("Business", 93, 10));
        channel.setMetadata(meta);
        return channel;
    }
}
