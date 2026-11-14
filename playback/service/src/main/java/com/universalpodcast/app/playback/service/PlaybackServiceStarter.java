package com.universalpodcast.app.playback.service;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import androidx.core.content.ContextCompat;
import androidx.media3.common.DeviceInfo;
import com.universalpodcast.app.model.feed.FeedMedia;
import com.universalpodcast.app.model.playback.Playable;
import com.universalpodcast.app.playback.base.BuildConfig;
import com.universalpodcast.app.playback.base.MediaItemAdapter;

public class PlaybackServiceStarter {
    private final Context context;
    private final Playable media;
    private boolean shouldStreamThisTime = false;
    private boolean callEvenIfRunning = false;

    public PlaybackServiceStarter(Context context, Playable media) {
        this.context = context;
        this.media = media;
    }

    /**
     * Default value: false
     */
    public PlaybackServiceStarter callEvenIfRunning(boolean callEvenIfRunning) {
        this.callEvenIfRunning = callEvenIfRunning;
        return this;
    }

    public PlaybackServiceStarter shouldStreamThisTime(boolean shouldStreamThisTime) {
        this.shouldStreamThisTime = shouldStreamThisTime;
        return this;
    }

    public Intent getIntent() {
        Intent launchIntent = new Intent(context, PlaybackService.class);
        launchIntent.putExtra(PlaybackServiceInterface.EXTRA_PLAYABLE, (Parcelable) media);
        launchIntent.putExtra(PlaybackServiceInterface.EXTRA_ALLOW_STREAM_THIS_TIME, shouldStreamThisTime);
        return launchIntent;
    }

    public void start() {
        if (BuildConfig.USE_MEDIA3_PLAYBACK_SERVICE) {
            PlaybackController.bindToMedia3Service(context, controller -> {
                if (controller.getCurrentMediaItem() != null && media instanceof FeedMedia
                        && ("" + ((FeedMedia) media).getId()).equals(controller.getCurrentMediaItem().mediaId)) {
                    controller.play();
                    return;
                }
                if (!controller.isPlaying() && controller.getDeviceInfo().playbackType
                        == DeviceInfo.PLAYBACK_TYPE_REMOTE) {
                    controller.play(); // Casting somehow does not play when not quickly starting the old episode
                }
                controller.setMediaItem(MediaItemAdapter.fromMediaIdStub(((FeedMedia) media).getId()));
                controller.prepare();
                controller.play();
            });
            return;
        }

        if (PlaybackServiceInterface.isPlaying && !callEvenIfRunning) {
            return;
        }
        ContextCompat.startForegroundService(context, getIntent());
    }
}
