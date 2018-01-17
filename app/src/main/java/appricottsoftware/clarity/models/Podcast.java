package appricottsoftware.clarity.models;

/**
 * Created by karen on 1/16/18.
 */

public class Podcast {

    private String image;
    private String id;
    private String audio_length;
    private String rss;
    private String title_original;
    private int itunes_id;
    private String audio;
    private String podcast_id;
    private String podcast_title_original;
    private String description_original;
    private String publisher_original;
    private long pub_date_ms;

    public Podcast() { /* Empty constructor required by GSON */}

    public String getImage() {
        return image.replaceAll("\\/", "/");
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAudio_length() {
        return audio_length;
    }

    public void setAudio_length(String audio_length) {
        this.audio_length = audio_length;
    }

    public String getRss() {
        return rss;
    }

    public void setRss(String rss) {
        this.rss = rss;
    }

    public String getTitle_original() {
        return title_original;
    }

    public void setTitle_original(String title_original) {
        this.title_original = title_original;
    }

    public int getItunes_id() {
        return itunes_id;
    }

    public void setItunes_id(int itunes_id) {
        this.itunes_id = itunes_id;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public String getPodcast_id() {
        return podcast_id;
    }

    public void setPodcast_id(String podcast_id) {
        this.podcast_id = podcast_id;
    }

    public String getPodcast_title_original() {
        return podcast_title_original;
    }

    public void setPodcast_title_original(String podcast_title_original) {
        this.podcast_title_original = podcast_title_original;
    }

    public String getDescription_original() {
        return description_original;
    }

    public void setDescription_original(String description_original) {
        this.description_original = description_original;
    }

    public String getPublisher_original() {
        return publisher_original;
    }

    public void setPublisher_original(String publisher_original) {
        this.publisher_original = publisher_original;
    }

    public long getPub_date_ms() {
        return pub_date_ms;
    }

    public void setPub_date_ms(long pub_date_ms) {
        this.pub_date_ms = pub_date_ms;
    }
}

