package com.universalpodcast.app.actionbutton;

import android.content.Context;
import android.util.Log;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import com.universalpodcast.app.R;
import com.universalpodcast.app.playback.service.PlaybackServiceInterface;
import com.universalpodcast.app.playback.service.PlaybackServiceStarter;
import com.universalpodcast.app.storage.database.DBWriter;
import com.universalpodcast.app.event.FeedItemEvent;
import com.universalpodcast.app.event.MessageEvent;
import com.universalpodcast.app.model.feed.FeedItem;
import com.universalpodcast.app.model.feed.FeedMedia;
import com.universalpodcast.app.model.playback.MediaType;
import org.greenrobot.eventbus.EventBus;

import java.util.Collections;

public class PlayActionButton extends ItemActionButton {
    private static final String TAG = "PlayActionButton";

    public PlayActionButton(FeedItem item) {
        super(item);
    }

    @Override
    @StringRes
    public int getLabel() {
        return R.string.play_label;
    }

    @Override
    @DrawableRes
    public int getDrawable() {
        return R.drawable.ic_play_24dp;
    }

    @Override
    public void onClick(Context context) {
        FeedMedia media = item.getMedia();
        if (media == null) {
            return;
        }
        if (!media.fileExists()) {
            Log.i(TAG, "Missing episode. Will update the database now.");
            media.setDownloaded(false, 0);
            media.setLocalFileUrl(null);
            DBWriter.setMediaDownloadInformation(media);
            EventBus.getDefault().post(new FeedItemEvent(Collections.singletonList(media.getItem()), false));
            EventBus.getDefault().post(new MessageEvent(context.getString(R.string.error_file_not_found)));
            return;
        }
        new PlaybackServiceStarter(context, media)
                .callEvenIfRunning(true)
                .start();

        if (media.getMediaType() == MediaType.VIDEO) {
            context.startActivity(PlaybackServiceInterface.getPlayerActivityIntent(context, media));
        }
    }
}
