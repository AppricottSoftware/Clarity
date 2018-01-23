package appricottsoftware.clarity.models;

public class EpisodeMetadata {

    /* Sample API Response
        "pub_date_ms": 1512403205000,
        "audio": "http://feedproxy.google.com/~r/TEDTalks_audio/~5/sdkNwbx1FQo/JustinBaldoni_2017W.mp3",
        "title": "Why I'm done trying to be \"man enough\" | Justin Baldoni",
        "audio_length": 1111,
        "description": "Justin Baldoni wants to start a dialogue with men about redefining masculinity -- to figure out ways to be not just good men but good humans. In a warm, personal talk, he shares his effort to reconcile who he is with who the world tells him a man should be. And he has a challenge for men: \"See if you can use the same qualities that you feel make you a man to go deeper,\" Baldoni says. \"Your strength, your bravery, your toughness: Are you brave enough to be vulnerable? Are you strong enough to be sensitive? Are you confident enough to listen to the women in your life?\"<img src=\"http://feeds.feedburner.com/~r/TEDTalks_audio/~4/WtYkfeP80O4\" height=\"1\" width=\"1\" alt=\"\"/>",
        "id": "7fbabe370daf4e34a225bce232764f96",
        "podcast": {
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
        }
    */

    private String pub_date_ms;
    private String audio;
    private String title;
    private long audio_length;
    private String description;
    private String id;
    private Podcast podcast;

    public EpisodeMetadata() { /* Empty constructor required by GSON */}

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
