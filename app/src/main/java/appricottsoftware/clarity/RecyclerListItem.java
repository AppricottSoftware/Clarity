package appricottsoftware.clarity;

import android.media.Image;

/**
 * Created by Mathew on 1/30/2018.
 */

public class RecyclerListItem {

    public RecyclerListItem(String title, String description) {
        this.title = title;
        this.description = description;
    }

    private String title;
    private String description;


    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
