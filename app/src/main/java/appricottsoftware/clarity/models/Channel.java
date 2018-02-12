package appricottsoftware.clarity.models;

import java.util.List;
import org.parceler.Parcel;

//public class Channel {
//
//    int cid;
//    int uid;
//
//    public int getCid() {
//        return cid;
//    }
//
//    public void setCid(int cid) {
//        this.cid = cid;
//    }
//
//    public int getUid() {
//        return uid;
//    }
//
//    public void setUid(int uid) {
//        this.uid = uid;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public List<Integer> getGenres() {
//        return genres;
//    }
//
//    public void setGenres(List<Integer> genres) {
//        this.genres = genres;
//    }
//
//    String name;
//    List<Integer> genres;
//}



@Parcel
public class Channel {

    String topic;

    public Channel() { /* Empty constructor required by GSON and Parcel */}

    public Channel(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public appricottsoftware.clarity.models.Channel getExampleChannel() {
        return new appricottsoftware.clarity.models.Channel("Cryptocurrency");
    }
}


