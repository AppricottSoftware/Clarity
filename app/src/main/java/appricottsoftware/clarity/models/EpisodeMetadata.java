package appricottsoftware.clarity.models;

import org.parceler.Parcel;

@Parcel
public class EpisodeMetadata {

    /* Sample API Response
        "description": "What is net neutrality and why should you care about it? What happens to science and innovation, and even sites like this one, without an open internet?\r\n\r\nRead the transcript at http://www.quickanddirtytips.com/education/science/net-neutrality-repeal-and-science\r\nCheck out all the Quick and Dirty Tips shows:\r\nwww.quickanddirtytips.com/podcasts\r\n\r\nFOLLOW EVERYDAY EINSTEIN\r\nFacebook: https://www.facebook.com/qdteinstein \r\nTwitter: https://twitter.com/qdteinstein",
        "title": "263 - The End of Net Neutrality: What It Means For Science (and You)",
        "podcast": {
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
        },
        "audio": "http://www.podtrac.com/pts/redirect.mp3/media.blubrry.com/einstein/traffic.libsyn.com/einstein/ede_263-ui3.mp3",
        "pub_date_ms": 1511796600000,
        "audio_length": 599,
        "id": "72e6260159b14d569ba320b56127574f"
        }
    */

    String pub_date_ms;
    String audio;
    String title;
    long audio_length;
    String description;
    String id;
    Podcast podcast;

    public EpisodeMetadata() { /* Empty constructor required by GSON and Parcel */}

    public EpisodeMetadata(String pub_date_ms, String audio, String title, long audio_length, String description, String id, Podcast podcast) {
        this.pub_date_ms = pub_date_ms;
        this.audio = audio;
        this.title = title;
        this.audio_length = audio_length;
        this.description = description;
        this.id = id;
        this.podcast = podcast;
    }

    public String getPub_date_ms() {
        return pub_date_ms;
    }

    public void setPub_date_ms(String pub_date_ms) {
        this.pub_date_ms = pub_date_ms;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getAudio_length() {
        return audio_length;
    }

    public void setAudio_length(long audio_length) {
        this.audio_length = audio_length;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Podcast getPodcast() {
        return podcast;
    }

    public void setPodcast(Podcast podcast) {
        this.podcast = podcast;
    }
}
