package appricottsoftware.clarity;

import android.media.Image;


public class RecyclerListItem {

    public RecyclerListItem(String title, int imageURL) {
        this.title = title;
        this.imageURL = imageURL;
    }

    private String title;
    private int imageURL;

    public String getTitle() {
        return title;
    }

    public int getImage() {
        return imageURL;
    }

}
