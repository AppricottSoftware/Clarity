package appricottsoftware.clarity.models;

import org.parceler.Parcel;

@Parcel
public class PodcastMetadata {

    /* Sample API Response
        "description": "How do astronomers photograph a black hole? How often do planes get hit by lightning? What does the EPA actually do? Science is all around us and transforming our world at a rapid pace. Extragalactic astrophysicist Sabrina Stierwalt is here to guide you through it. She'll help you make sense of the everyday and the once-in-a-lifetime.",
        "title": "Everyday Einstein's Quick and Dirty Tips for Making Sense of Science",
        "genres": ["Podcasts", "Education", "K-12"],
        "rss": "http://www.quickanddirtytips.com/xml/einstein.xml",
        "language": "English",
        "itunes_id": 510036484,
        "lastest_pub_date_ms": 1516635915000,
        "image": "https://d3sv2eduhewoas.cloudfront.net/channel/image/a77267bda67144dea582a918999148bc.jpeg",
        "website": "http://www.quickanddirtytips.com/everyday-einstein?utm_source=listennotes.com&utm_campaign=Listen+Notes&utm_medium=website",
        "publisher": "Macmillan Holdings, LLC",
        "id": "704e77179b6d4dc7a3df506e0eaa00fc"
     */

    String image;
    String publisher;
    long itunes_id;
    long lastest_pub_date_ms;
    String title;
    String website;
    String description;
    String[] genres;
    String id;
    String language;
    String rss;

    public PodcastMetadata() { /* Empty constructor required by GSON and Parcel */}

    public PodcastMetadata(String image, String publisher, long itunes_id, long lastest_pub_date_ms, String title, String website, String description, String[] genres, String id, String language, String rss) {
        this.image = image;
        this.publisher = publisher;
        this.itunes_id = itunes_id;
        this.lastest_pub_date_ms = lastest_pub_date_ms;
        this.title = title;
        this.website = website;
        this.description = description;
        this.genres = genres;
        this.id = id;
        this.language = language;
        this.rss = rss;
    }

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
