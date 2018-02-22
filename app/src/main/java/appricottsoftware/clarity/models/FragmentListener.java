package appricottsoftware.clarity.models;

// Allows fragment to communicate to its host activity when onCreateView has finished
public interface FragmentListener {
    void onCreatedView();
    void returnToHomeFragment();
}
