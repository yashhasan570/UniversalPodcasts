package com.universalpodcast.app.actionbutton;

import android.content.Context;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import com.universalpodcast.app.R;
import com.universalpodcast.app.model.feed.FeedItem;
import com.universalpodcast.app.model.feed.FeedMedia;
import com.universalpodcast.app.playback.service.PlaybackController;
import com.universalpodcast.app.playback.service.PlaybackStatus;

public class PauseActionButton extends ItemActionButton {

    public PauseActionButton(FeedItem item) {
        super(item);
    }

    @Override
    @StringRes
    public int getLabel() {
        return R.string.pause_label;
    }

    @Override
    @DrawableRes
    public int getDrawable() {
        return R.drawable.ic_pause;
    }

    @Override
    public void onClick(Context context) {
        FeedMedia media = item.getMedia();
        if (media == null) {
            return;
        }

        if (!PlaybackStatus.isCurrentlyPlaying(media)) {
            return;
        }

        PlaybackController.bindToMedia3Service(context, controller -> controller.pause());
    }
}
