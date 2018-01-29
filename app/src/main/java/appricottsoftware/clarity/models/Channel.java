package appricottsoftware.clarity.models;

import org.parceler.Parcel;

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

    public Channel getExampleChannel() {
        return new Channel("Cryptocurrency");
    }
}
