package appricottsoftware.clarity.services;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// TODO
// Keeps track of notification, updates automatically for a given mediasession
public class NotificationReceiver extends BroadcastReceiver {

    private PlayerService service;

    public NotificationReceiver(PlayerService service) {
        this.service = service;

    }

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
