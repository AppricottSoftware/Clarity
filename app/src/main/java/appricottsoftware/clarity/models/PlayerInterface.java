package appricottsoftware.clarity.models;

import java.util.ArrayList;

public interface PlayerInterface {

    public void loadPlaylist(ArrayList<Podcast> podcasts);
    public void play(Podcast podcast);
    public void pause();
    public void skip();
}
