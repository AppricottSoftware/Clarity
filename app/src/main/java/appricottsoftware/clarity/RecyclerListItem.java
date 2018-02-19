package appricottsoftware.clarity;

import android.media.Image;


public class RecyclerListItem {

    public RecyclerListItem(String title, String imageURL) {
        this.title = title;
        this.imageURL = imageURL;
    }

    private String title;
    private String imageURL;

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return imageURL;
    }

}
