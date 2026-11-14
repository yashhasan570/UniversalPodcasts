package com.universalpodcast.app.playback.service;

import android.content.Context;
import android.content.Intent;
import com.universalpodcast.app.model.playback.MediaType;
import com.universalpodcast.app.model.playback.Playable;
import com.universalpodcast.app.ui.appstartintent.MainActivityStarter;
import com.universalpodcast.app.ui.appstartintent.VideoPlayerActivityStarter;

public abstract class PlaybackServiceInterface {
    public static final String EXTRA_PLAYABLE = "PlaybackService.PlayableExtra";
    public static final String EXTRA_ALLOW_STREAM_THIS_TIME = "extra.com.universalpodcast.app.core.service.allowStream";
    public static final String EXTRA_ALLOW_STREAM_ALWAYS = "extra.com.universalpodcast.app.core.service.allowStreamAlways";

    public static final String ACTION_PLAYER_NOTIFICATION
            = "action.com.universalpodcast.app.core.service.playerNotification";
    public static final String EXTRA_NOTIFICATION_CODE = "extra.com.universalpodcast.app.core.service.notificationCode";
    public static final String EXTRA_NOTIFICATION_TYPE = "extra.com.universalpodcast.app.core.service.notificationType";
    public static final int NOTIFICATION_TYPE_PLAYBACK_END = 7;
    public static final int NOTIFICATION_TYPE_RELOAD = 3;
    public static final int EXTRA_CODE_AUDIO = 1; // Used in NOTIFICATION_TYPE_RELOAD
    public static final int EXTRA_CODE_VIDEO = 2;
    public static final int EXTRA_CODE_CAST = 3;

    public static final String ACTION_SHUTDOWN_PLAYBACK_SERVICE
            = "action.com.universalpodcast.app.core.service.actionShutdownPlaybackService";

    /**
     * Is true if playback is active or about to start.
     */
    public static boolean isPlaying = false;

    public static Intent getPlayerActivityIntent(Context context, Playable media) {
        if (media.getMediaType() == MediaType.VIDEO) {
            return new VideoPlayerActivityStarter(context).getIntent();
        } else {
            return new MainActivityStarter(context).withClearBackStack().withOpenPlayer().getIntent();
        }
    }
}
