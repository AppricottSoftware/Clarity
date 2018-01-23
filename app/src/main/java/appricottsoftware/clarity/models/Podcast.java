package appricottsoftware.clarity.models;

public class Podcast {

    /* Sample API Response
        "image": "http://is5.mzstatic.com/image/thumb/Music71/v4/9c/d5/a6/9cd5a666-79ea-4b76-c5f6-90cc508648e9/source/600x600bb.jpg",
         "title_original": "Star Wars Uplink",
         "publisher_highlighted": "<span class=\"ln-search-highlight\">Star</span> <span class=\"ln-search-highlight\">Wars</span> Uplink",
         "itunes_id": 1160076174,
         "lastest_pub_date_ms": "a year ago",
         "id": "897cb5647c5447ebad5039be8293af5f",
         "description_highlighted": "...Brought to you by Rex Overdrive and Adam Cook, comes the <span class=\"ln-search-highlight\">Star</span> <span class=\"ln-search-highlight\">Wars</span> Uplink podcast feed. Here you will find shows discussing and celebrating everything about <span class=\"ln-search-highlight\">Star</span> <span class=\"ln-search-highlight\">Wars</span>.\n\nSubscribe on Itunes!\nTwitter",
         "title_highlighted": "<span class=\"ln-search-highlight\">Star</span> <span class=\"ln-search-highlight\">Wars</span> Uplink",
         "publisher_original": "Star Wars Uplink",
         "rss": "http://starwarsuplink.libsyn.com/rss",
         "description_original": "Brought to you by Rex Overdrive and Adam Cook, comes the Star Wars Uplink podcast feed. Here you will find shows discussing and celebrating everything about Star Wars.\n\nSubscribe on Itunes!\nTwitter @starwarsuplink"
     */

    private String image;
    private String title_original;
    private String publisher_highlighted;
    private long itunes_id;
    private String lastest_pub_date_ms;
    private String id;
    private String description_highlighted;
    private String title_highlighted;
    private String publisher_original;
    private String rss;
    private String description_original;

    public Podcast() { /* Empty constructor required by GSON */}

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle_original() {
        return title_original;
    }

    public void setTitle_original(String title_original) {
        this.title_original = title_original;
    }

    public String getPublisher_highlighted() {
        return publisher_highlighted;
    }

    public void setPublisher_highlighted(String publisher_highlighted) {
        this.publisher_highlighted = publisher_highlighted;
    }

    public long getItunes_id() {
        return itunes_id;
    }

    public void setItunes_id(long itunes_id) {
        this.itunes_id = itunes_id;
    }

    public String getLastest_pub_date_ms() {
        return lastest_pub_date_ms;
    }

    public void setLastest_pub_date_ms(String lastest_pub_date_ms) {
        this.lastest_pub_date_ms = lastest_pub_date_ms;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription_highlighted() {
        return description_highlighted;
    }

    public void setDescription_highlighted(String description_highlighted) {
        this.description_highlighted = description_highlighted;
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

    public String getRss() {
        return rss;
    }

    public void setRss(String rss) {
        this.rss = rss;
    }

    public String getDescription_original() {
        return description_original;
    }

    public void setDescription_original(String description_original) {
        this.description_original = description_original;
    }
}

