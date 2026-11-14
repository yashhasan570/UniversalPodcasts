package com.universalpodcast.app.playback.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import com.universalpodcast.app.playback.base.BuildConfig;

/**
 * Receives media button events.
 */
public class MediaButtonReceiver extends BroadcastReceiver {
    private static final String TAG = "MediaButtonReceiver";
    public static final String EXTRA_KEYCODE = "com.universalpodcast.app.core.service.extra.MediaButtonReceiver.KEYCODE";
    public static final String EXTRA_CUSTOM_ACTION =
            "com.universalpodcast.app.core.service.extra.MediaButtonReceiver.CUSTOM_ACTION";
    public static final String EXTRA_SOURCE = "com.universalpodcast.app.core.service.extra.MediaButtonReceiver.SOURCE";
    public static final String EXTRA_HARDWAREBUTTON
            = "com.universalpodcast.app.core.service.extra.MediaButtonReceiver.HARDWAREBUTTON";
    public static final String PLAYBACK_SERVICE_INTENT = "com.universalpodcast.app.intents.PLAYBACK_SERVICE";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received intent");
        if (intent == null || intent.getExtras() == null || BuildConfig.USE_MEDIA3_PLAYBACK_SERVICE) {
            return;
        }
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
            Intent serviceIntent = new Intent(PLAYBACK_SERVICE_INTENT);
            serviceIntent.setPackage(context.getPackageName());
            serviceIntent.putExtra(EXTRA_KEYCODE, event.getKeyCode());
            serviceIntent.putExtra(EXTRA_SOURCE, event.getSource());
            serviceIntent.putExtra(EXTRA_HARDWAREBUTTON, event.getEventTime() > 0 || event.getDownTime() > 0);
            try {
                ContextCompat.startForegroundService(context, serviceIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
