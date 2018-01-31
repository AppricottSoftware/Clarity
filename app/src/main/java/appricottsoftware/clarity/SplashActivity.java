package appricottsoftware.clarity;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        GifImageView gifImageView = findViewById(R.id.appricottsoftware_clarity_GifImageView);
        gifImageView.setGifImageResource(R.drawable.clarity_gif_130_sm);

        MediaPlayer play;
        play = new MediaPlayer();
        try {
            AssetFileDescriptor afd = getAssets().openFd("low_sonar_fade.wav");
            play.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            play.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        play.start();

        final Intent loginActivityIntent = new Intent(this, LoginActivity.class);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(loginActivityIntent);
                finish();
            }
        }, 1300);


    } //onCreate

} //Main

