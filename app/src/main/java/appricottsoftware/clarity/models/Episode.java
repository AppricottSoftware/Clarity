package appricottsoftware.clarity.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import org.parceler.Parcel;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;

import appricottsoftware.clarity.sync.ClarityApp;
import appricottsoftware.clarity.sync.ClarityClient;

@Parcel
public class Episode {

    /* Sample API Response
		"pub_date_ms": 1511796600000,
		"description_original": "What is net neutrality and why should you care about it? What happens to science and innovation, and even sites like this one, without an open internet?\n\nRead the transcript at http://www.quickanddirtytips.com/education/science/net-neutrality-repeal-and-science\nCheck out all the Quick and Dirty Tips shows:\nwww.quickanddirtytips.com/podcasts\n\nFOLLOW EVERYDAY EINSTEIN\nFacebook: https://www.facebook.com/qdteinstein \nTwitter: https://twitter.com/qdteinstein",
		"rss": "http://www.quickanddirtytips.com/xml/einstein.xml",
		"audio": "http://www.podtrac.com/pts/redirect.mp3/media.blubrry.com/einstein/traffic.libsyn.com/einstein/ede_263-ui3.mp3",
		"description_highlighted": "...What is <span class=\"ln-search-highlight\">net</span> <span class=\"ln-search-highlight\">neutrality</span> and why should you care about it? What happens to science and innovation, and even sites like this one, without an open internet?\n\nRead the transcript at http",
		"itunes_id": 510036484,
		"title_highlighted": "263 - The End of <span class=\"ln-search-highlight\">Net</span> <span class=\"ln-search-highlight\">Neutrality</span>: What It Means For Science (and You)",
		"publisher_original": "Macmillan Holdings, LLC",
		"audio_length": "00:09:59",
		"podcast_title_highlighted": "Everyday Einstein's Quick and Dirty Tips for Making Sense of Science",
		"image": "https://d3sv2eduhewoas.cloudfront.net/channel/image/a77267bda67144dea582a918999148bc.jpeg",
		"genres": [111, 114],
		"title_original": "263 - The End of Net Neutrality: What It Means For Science (and You)",
		"podcast_title_original": "Everyday Einstein's Quick and Dirty Tips for Making Sense of Science",
		"publisher_highlighted": "Macmillan Holdings, LLC",
		"podcast_id": "704e77179b6d4dc7a3df506e0eaa00fc",
		"id": "72e6260159b14d569ba320b56127574f"
     */

    long pub_date_ms;
    String description_original;
    String rss;
    String audio;
    String description_highlighted;
    long itunes_id;
    String title_highlighted;
    String publisher_original;
    String audio_length;
    String podcast_title_highlighted;
    String image;
    ArrayList<String> genres;
    String title_original;
    String podcast_title_original;
    String publisher_highlighted;
    String podcast_id;
    String id;

    public Episode() { /* Empty constructor required by GSON and Parcel */}

    public Episode(long pub_date_ms, String description_original, String rss, String audio, String description_highlighted, long itunes_id, String title_highlighted, String publisher_original, String audio_length, String podcast_title_highlighted, String image, ArrayList<String> genres, String title_original, String podcast_title_original, String publisher_highlighted, String podcast_id, String id) {
        this.pub_date_ms = pub_date_ms;
        this.description_original = description_original;
        this.rss = rss;
        this.audio = audio;
        this.description_highlighted = description_highlighted;
        this.itunes_id = itunes_id;
        this.title_highlighted = title_highlighted;
        this.publisher_original = publisher_original;
        this.audio_length = audio_length;
        this.podcast_title_highlighted = podcast_title_highlighted;
        this.image = image;
        this.genres = genres;
        this.title_original = title_original;
        this.podcast_title_original = podcast_title_original;
        this.publisher_highlighted = publisher_highlighted;
        this.podcast_id = podcast_id;
        this.id = id;
    }

    public long getPub_date_ms() {
        return pub_date_ms;
    }

    public void setPub_date_ms(long pub_date_ms) {
        this.pub_date_ms = pub_date_ms;
    }

    public String getDescription_original() {
        return description_original;
    }

    public void setDescription_original(String description_original) {
        this.description_original = description_original;
    }

    public String getRss() {
        return rss;
    }

    public void setRss(String rss) {
        this.rss = rss;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public String getDescription_highlighted() {
        return description_highlighted;
    }

    public void setDescription_highlighted(String description_highlighted) {
        this.description_highlighted = description_highlighted;
    }

    public long getItunes_id() {
        return itunes_id;
    }

    public void setItunes_id(long itunes_id) {
        this.itunes_id = itunes_id;
    }

    public String getTitle_highlighted() {
        return title_highlighted;
    }

    public void setTitle_highlighted(String title_highlighted) {
        this.title_highlighted = title_highlighted;
    }

    public String getPublisher_original() {
        return publisher_original;
    }

    public void setPublisher_original(String publisher_original) {
        this.publisher_original = publisher_original;
    }

    public String getAudio_length() {
        return audio_length;
    }

    public void setAudio_length(String audio_length) {
        this.audio_length = audio_length;
    }

    public String getPodcast_title_highlighted() {
        return podcast_title_highlighted;
    }

    public void setPodcast_title_highlighted(String podcast_title_highlighted) {
        this.podcast_title_highlighted = podcast_title_highlighted;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public ArrayList<String> getGenres() {
        return genres;
    }

    public void setGenres(ArrayList<String> genres) {
        this.genres = genres;
    }

    public String getTitle_original() {
        return title_original;
    }

    public void setTitle_original(String title_original) {
        this.title_original = title_original;
    }

    public String getPodcast_title_original() {
        return podcast_title_original;
    }

    public void setPodcast_title_original(String podcast_title_original) {
        this.podcast_title_original = podcast_title_original;
    }

    public String getPublisher_highlighted() {
        return publisher_highlighted;
    }

    public void setPublisher_highlighted(String publisher_highlighted) {
        this.publisher_highlighted = publisher_highlighted;
    }

    public String getPodcast_id() {
        return podcast_id;
    }

    public void setPodcast_id(String podcast_id) {
        this.podcast_id = podcast_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<Integer> getIntGenres() {
        ArrayList<Integer> gens = new ArrayList<>();
        for(String g : genres) {
            gens.add(Integer.parseInt(g));
        }
        return gens;
    }

    public ArrayList<Metadata> getMetadata() {
        ArrayList<Metadata> metadata = new ArrayList<>();
        ArrayList<Integer> genres = getIntGenres();
        for(Integer g : genres) {
            Metadata m = new Metadata();
            m.setMid(g);
            metadata.add(m);
        }
        return metadata;
    }

    public boolean isValid() {
        return !(description_original == null
                || rss == null
                || audio == null
                || description_highlighted == null
                || title_highlighted == null
                || publisher_original == null
                || audio_length == null
                || podcast_title_highlighted == null
                || image == null
                || title_original == null
                || podcast_title_original == null
                || publisher_highlighted == null
                || podcast_id == null
                || id == null);
    }

    public String toString() {
        return ClarityApp.getGson().toJson(this);
    }

    public MediaMetadataCompat toMediaMetadataCompat() {
        Bitmap bitmap = null;
        try {
            URL url = new URL(image);
            bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch(IOException e) {
            Log.e("Episode", "Failed to download album art", e);
        }
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
        builder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title_original)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title_original)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, publisher_original)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, description_original)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, podcast_title_original)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, image)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, audio)
                .putString(MediaMetadataCompat.METADATA_KEY_AUTHOR, publisher_original)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap);
        return builder.build();
    }

    public static Episode getSampleEpisode() {
        Episode episode = new Episode();
        episode.setPub_date_ms(Long.parseLong("1511796600000"));
        episode.setDescription_original("What is net neutrality and why should you care about it? What happens to science and innovation, and even sites like this one, without an open internet?\n\nRead the transcript at http://www.quickanddirtytips.com/education/science/net-neutrality-repeal-and-science\nCheck out all the Quick and Dirty Tips shows:\nwww.quickanddirtytips.com/podcasts\n\nFOLLOW EVERYDAY EINSTEIN\nFacebook: https://www.facebook.com/qdteinstein \nTwitter: https://twitter.com/qdteinstein");
        episode.setRss("http://www.quickanddirtytips.com/xml/einstein.xml");
        episode.setAudio("http://www.podtrac.com/pts/redirect.mp3/media.blubrry.com/einstein/traffic.libsyn.com/einstein/ede_263-ui3.mp3");
        episode.setDescription_highlighted("...What is <span class=\"ln-search-highlight\">net</span> <span class=\"ln-search-highlight\">neutrality</span> and why should you care about it? What happens to science and innovation, and even sites like this one, without an open internet?\n\nRead the transcript at http");
        episode.setItunes_id(510036484);
        episode.setTitle_highlighted("263 - The End of <span class=\"ln-search-highlight\">Net</span> <span class=\"ln-search-highlight\">Neutrality</span>: What It Means For Science (and You)");
        episode.setPublisher_original("Macmillan Holdings, LLC");
        episode.setAudio_length("00:09:59");
        episode.setPodcast_title_highlighted("Everyday Einstein's Quick and Dirty Tips for Making Sense of Science");
        episode.setImage("https://d3sv2eduhewoas.cloudfront.net/channel/image/a77267bda67144dea582a918999148bc.jpeg");
        ArrayList<String> genres = new ArrayList<>();
        genres.add("111");
        genres.add("114");
        episode.setGenres(genres);
        episode.setTitle_original("263 - The End of Net Neutrality: What It Means For Science (and You)");
        episode.setPodcast_title_original("Everyday Einstein's Quick and Dirty Tips for Making Sense of Science");
        episode.setPublisher_highlighted("Macmillan Holdings, LLC");
        episode.setPodcast_id("704e77179b6d4dc7a3df506e0eaa00fc");
        episode.setId("72e6260159b14d569ba320b56127574f");
        return episode;
    }
}
