package appricottsoftware.clarity.models;

import org.parceler.Parcel;

@Parcel
public class Metadata {

    String category;
    int mid;
    int score;

    public Metadata() { /* Empty constructor required by GSON and Parcel */ }

    public Metadata(String category, int mid, int score) {
        this.category = category;
        this.mid = mid;
        this.score = score;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
        return category.equals(m.category)
                && mid == m.mid
                && score == m.score;
    }
}
