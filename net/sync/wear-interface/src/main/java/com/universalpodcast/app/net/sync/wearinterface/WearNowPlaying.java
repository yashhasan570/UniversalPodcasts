package com.universalpodcast.app.net.sync.wearinterface;

import com.universalpodcast.app.model.feed.FeedItem;

public class WearNowPlaying {
    public final FeedItem item;
    public final boolean isPlaying;

    public WearNowPlaying(FeedItem item, boolean isPlaying) {
        this.item = item;
        this.isPlaying = isPlaying;
    }
}
