//package appricottsoftware.clarity.services;
//
//import android.content.Context;
//import android.media.AudioManager;
//import android.support.v4.app.NotificationCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v4.media.MediaDescriptionCompat;
//import android.support.v4.media.MediaMetadataCompat;
//import android.support.v4.media.session.MediaButtonReceiver;
//import android.support.v4.media.session.MediaControllerCompat;
//import android.support.v4.media.session.MediaSessionCompat;
//import android.support.v4.media.session.PlaybackStateCompat;
//import android.util.Log;
//
//import appricottsoftware.clarity.R;
//
//// Callback class
//public class PlayerCallback extends MediaSessionCompat.Callback {
//
//    private static final String TAG = "PlayerCallback";
//
//    private PlayerService playerService;
//
//    public PlayerCallback(PlayerService playerService) {
//        this.playerService = playerService;
//    }
//
//    @Override
//    public void onPlay() {
//        super.onPlay();
//        AudioManager audioManager = (AudioManager)playerService.getContext().getSystemService(playerService.getContext().AUDIO_SERVICE);
//        // Request audio focus for playback
//        int result = audioManager.requestAudioFocus(playerService.getAfChangeListener(),
//                AudioManager.STREAM_MUSIC,
//                AudioManager.AUDIOFOCUS_GAIN);
//
//        if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//            // Start the service
////                mediaService.start();
//            // Set the session active
//            playerService.getMediaSession().setActive(true);
//            // Start the player
//            playerService.getMediaPlayer().start();
//            // TODO: Register BECOME_NOISY
////                registerReceiver(receiver, intentFilter)
//            setNotification();
//        }
//    }
//
//    @Override
//    public void onPause() {
//        AudioManager audioManager = (AudioManager)playerService.getContext().getSystemService(playerService.getContext().AUDIO_SERVICE);
//        playerService.getMediaPlayer().pause();
//        // TODO: unregister broadcast receiver
////            unregisterReceiver(receiver, intentFilter)
//        // Stop notification
//        stopForeground(false);
//    }
//
//    @Override
//    public void onStop() {
//        AudioManager audioManager = (AudioManager)playerService.getContext().getSystemService(playerService.getContext().AUDIO_SERVICE);
//        // Remove audio focus
//        audioManager.abandonAudioFocus(playerService.getAfChangeListener());
//        // TODO: Unregister receiver
////            unregisterReceiver(receiver);
//        // Stop the service
////            service.stop(self;
//        // Set the session inactive
//        playerService.getMediaSession().setActive(false);
//        // stop the player
//        playerService.getMediaPlayer().stop();
//        // Take the service out of the foreground
//        stopForeground(false);
//    }
//
//    private void setNotification() {
//        try {
//            MediaControllerCompat controller = playerService.getMediaSession().getController();
//            MediaMetadataCompat metadata = controller.getMetadata();
//            MediaDescriptionCompat description = metadata.getDescription();
//
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(playerService.getContext(), NOTIFICATION_CHANNEL_ID);
//            builder.setContentTitle(description.getTitle()) // Metadata on notification bar
//                    .setContentText(description.getSubtitle())
//                    .setSubText(description.getDescription())
//                    .setLargeIcon(description.getIconBitmap())
//                    .setContentIntent(controller.getSessionActivity()) // Launch activity by clicking notification
//                    .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(playerService.getContext(),
//                            PlaybackStateCompat.ACTION_STOP)) // Swipe notification away to stop service
//                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Show notification on lock screen
//                    .setSmallIcon(R.drawable.ic_launcher_foreground) // App icon + color
//                    .setColor(ContextCompat.getColor(playerService.getContext(),
//                            R.color.colorPrimary))
//                    .addAction(new NotificationCompat.Action(R.drawable.ic_player_pause, // Pause button
//                            playerService.getContext().getString(R.string.player_service_pause),
//                            MediaButtonReceiver.buildMediaButtonPendingIntent(playerService.getContext(),
//                                    PlaybackStateCompat.ACTION_PLAY_PAUSE)))
//                    .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle() // Use MediaStyle features
//                            .setMediaSession(playerService.getMediaSession().getSessionToken())
//                            .setShowActionsInCompactView(0));
//
//            // Show notification and put service in foreground
//            notification = builder.build();
//            startForeground(NOTIFICATION_ID, notification);
//        } catch(Exception e) {
//            Log.e(TAG, e.toString());
//        }
//    }
//}
