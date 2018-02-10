package appricottsoftware.clarity.models;

import java.util.List;

/**
 * Created by Mathew on 2/9/2018.
 */

public class Channel {

    int cid;
    int uid;

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

    public List<Integer> getGenres() {
        return genres;
    }

    public void setGenres(List<Integer> genres) {
        this.genres = genres;
    }

    String name;
    List<Integer> genres;
}
