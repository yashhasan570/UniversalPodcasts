package com.universalpodcast.app.net.download.service.feed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.universalpodcast.app.net.download.serviceinterface.FeedUpdateManager;

/**
 * Refreshes all feeds when it receives an intent
 */
public class FeedUpdateReceiver extends BroadcastReceiver {

    private static final String TAG = "FeedUpdateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received intent");
        FeedUpdateManager.getInstance().runOnce(context);
    }

}
