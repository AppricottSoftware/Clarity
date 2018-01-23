package appricottsoftware.clarity.models;

public class Episode {

    /* Sample API Response
          "image": "http://is1.mzstatic.com/image/thumb/Music71/v4/36/40/fd/3640fdd7-8dd3-8e30-0880-f5147fa9f8a1/source/600x600bb.jpg",
          "title_original": "News in Slow French #253 - Language learning in the context of current events",
          "publisher_highlighted": "Linguistica 360",
          "podcast_title_original": "French Podcast",
          "pub_date_ms": 1451622600000,
          "description_highlighted": "... personnes. Nous discuterons du résultat d'une étude montrant que les dirigeants élus ont une espérance de vie plus courte que les candidats qui arrivent deuxième, et nous conclurons avec le film <span class=\"ln-search-highlight\">Star</span> <span class=\"ln-search-highlight\">Wars</span>",
          "id": "4b7ae5ea786247d0a2002202debe246c",
          "publisher_original": "Linguistica 360",
          "podcast_id": "39e505db4e6f44c493614595a1507cac",
          "description_original": "Dans la première partie, nous parlerons de la victoire de l'Irak contre l'EI à Ramadi. Nous continuerons avec les tempêtes meurtrières qui ont frappé les États-Unis à Noël et ont tué au moins43 personnes. Nous discuterons du résultat d'une étude montrant que les dirigeants élus ont une espérance de vie plus courte que les candidats qui arrivent deuxième, et nous conclurons avec le film Star Wars qui bat des records à peine une semaine après sa sortie. - L’Irak repousse l’État Islamique hors de Ramadi - Des tempêtes meurtrières frappent les États-Unis à Noël - Selon une étude les chefs d’État vieillissent plus rapidement - Star Wars bat les records du box-office mondial La deuxième partie sera consacrée comme toujours à la langue et la culture françaises. Dans la section grammaticale, nous reverrons la forme interrogative et l'inversion et nous terminerons l'épisode d'aujourd'hui avec une nouvelle expression idiomatique : « Avoir/Prendre la grosse tête ».",
          "podcast_title_highlighted": "French Podcast",
          "audio": "http://traffic.libsyn.com/nsf/nsf253-itunes.mp3?dest-id=63713",
          "itunes_id": 427774337,
          "audio_length": "00:08:56",
          "title_highlighted": "News in Slow French #253 - Language learning in the context of current events",
          "rss": "http://nsf.libsyn.com/rss"
     */

    private String image;
    private String title_original;
    private String publisher_highlighted;
    private String podcast_title_original;
    private long pub_date_ms;
    private String description_highlighted;
    private String id;
    private String publisher_original;
    private String podcast_id;
    private String description_original;
    private String podcast_title_highlighted;
    private String audio;
    private long itunes_id;
    private String audio_length;
    private String title_highlighted;
    private String rss;

    public Episode() { /* Empty constructor required by GSON */}

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

    public String getPodcast_title_original() {
        return podcast_title_original;
    }

    public void setPodcast_title_original(String podcast_title_original) {
        this.podcast_title_original = podcast_title_original;
    }

    public long getPub_date_ms() {
        return pub_date_ms;
    }

    public void setPub_date_ms(long pub_date_ms) {
        this.pub_date_ms = pub_date_ms;
    }

    public String getDescription_highlighted() {
        return description_highlighted;
    }

    public void setDescription_highlighted(String description_highlighted) {
        this.description_highlighted = description_highlighted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPublisher_original() {
        return publisher_original;
    }

    public void setPublisher_original(String publisher_original) {
        this.publisher_original = publisher_original;
    }

    public String getPodcast_id() {
        return podcast_id;
    }

    public void setPodcast_id(String podcast_id) {
        this.podcast_id = podcast_id;
    }

    public String getDescription_original() {
        return description_original;
    }

    public void setDescription_original(String description_original) {
        this.description_original = description_original;
    }

    public String getPodcast_title_highlighted() {
        return podcast_title_highlighted;
    }

    public void setPodcast_title_highlighted(String podcast_title_highlighted) {
        this.podcast_title_highlighted = podcast_title_highlighted;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public long getItunes_id() {
        return itunes_id;
    }

    public void setItunes_id(long itunes_id) {
        this.itunes_id = itunes_id;
    }

    public String getAudio_length() {
        return audio_length;
    }

    public void setAudio_length(String audio_length) {
        this.audio_length = audio_length;
    }

    public String getTitle_highlighted() {
        return title_highlighted;
    }

    public void setTitle_highlighted(String title_highlighted) {
        this.title_highlighted = title_highlighted;
    }

    public String getRss() {
        return rss;
    }

    public void setRss(String rss) {
        this.rss = rss;
    }
}
