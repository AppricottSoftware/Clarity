package appricottsoftware.clarity.models;

import java.util.ArrayList;

import appricottsoftware.clarity.sync.ClarityApp;

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
