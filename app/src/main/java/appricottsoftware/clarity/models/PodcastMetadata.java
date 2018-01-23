package appricottsoftware.clarity.models;

public class PodcastMetadata {

    /* Sample API
      "image": "http://is4.mzstatic.com/image/thumb/Music128/v4/d5/c6/50/d5c65035-505e-b006-48e5-be3f0f8f19f8/source/600x600bb.jpg",
      "publisher": "TED",
      "itunes_id": 160904630,
      "lastest_pub_date_ms": 1516636594000,
      "title": "TED Talks Daily",
      "website": "https://www.ted.com/talks?utm_source=listennotes.com&utm_campaign=Listen+Notes&utm_medium=website",
      "description": "Want TED Talks on the go? Every weekday, this feed brings you our latest talks in audio format. Hear thought-provoking ideas on every subject imaginable -- from Artificial Intelligence to Zoology, and everything in between -- given by the world's leading thinkers and doers. This collection of talks, given at TED and TEDx conferences around the globe, is also available in video format.",
      "genres": [
        "Podcasts",
        "News & Politics",
        "Arts",
        "Science & Medicine",
        "Education",
        "Society & Culture",
        "Technology"
      ],
      "id": "9d6939745ed34e3aab0eb78a408ab40d",
      "language": "English",
      "rss": "http://feeds.feedburner.com/TEDTalks_audio"
     */

    private String image;
    private String publisher;
    private long itunes_id;
    private long lastest_pub_date_ms;
    private String title;
    private String website;
    private String description;
    private String[] genres;
    private String id;
    private String language;
    private String rss;

    public PodcastMetadata() { /* Empty constructor required by GSON */}

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public long getItunes_id() {
        return itunes_id;
    }

    public void setItunes_id(long itunes_id) {
        this.itunes_id = itunes_id;
    }

    public long getLastest_pub_date_ms() {
        return lastest_pub_date_ms;
    }

    public void setLastest_pub_date_ms(long lastest_pub_date_ms) {
        this.lastest_pub_date_ms = lastest_pub_date_ms;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getGenres() {
        return genres;
    }

    public void setGenres(String[] genres) {
        this.genres = genres;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getRss() {
        return rss;
    }

    public void setRss(String rss) {
        this.rss = rss;
    }
}
