package appricottsoftware.clarity.models;

import org.parceler.Parcel;

@Parcel
public class Metadata {

    String genre;
    int mid;
    int score;

    public Metadata() { /* Empty constructor required by GSON and Parcel */ }

    public Metadata(String genre, int mid, int score) {
        this.genre = genre;
        this.mid = mid;
        this.score = score;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getMid() {
        return mid;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }

        if(!(obj instanceof Metadata)) {
            return false;
        }

        Metadata m = (Metadata) obj;
        return genre.equals(m.genre)
                && mid == m.mid
                && score == m.score;
    }
}
