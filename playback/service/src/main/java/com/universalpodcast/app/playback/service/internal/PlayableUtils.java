package com.universalpodcast.app.playback.service.internal;

import java.util.Collections;
import java.util.Date;

import com.universalpodcast.app.storage.database.DBWriter;
import com.universalpodcast.app.model.feed.FeedItem;
import com.universalpodcast.app.model.feed.FeedMedia;
import com.universalpodcast.app.model.playback.Playable;

/**
 * Provides utility methods for Playable objects.
 */
public abstract class PlayableUtils {
    /**
     * Saves the current position of this object.
     *
     * @param newPosition  new playback position in ms
     * @param timestamp  current time in ms
     */
    public static void saveCurrentPosition(Playable playable, int newPosition, long timestamp) {
        playable.setPosition(newPosition);
        playable.setLastPlayedTimeStatistics(timestamp);

        if (playable instanceof FeedMedia) {
            FeedMedia media = (FeedMedia) playable;
            media.setLastPlayedTimeHistory(new Date(timestamp));
            FeedItem item = media.getItem();
            if (item != null && item.isNew()) {
                DBWriter.markItemsPlayed(FeedItem.UNPLAYED, false, Collections.singletonList(item));
            }
            DBWriter.setFeedMediaPlaybackInformation(media);
        }
    }
}
