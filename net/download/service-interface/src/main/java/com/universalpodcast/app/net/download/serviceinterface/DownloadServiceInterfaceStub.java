package com.universalpodcast.app.net.download.serviceinterface;

import android.content.Context;
import com.universalpodcast.app.model.feed.FeedItem;
import com.universalpodcast.app.model.feed.FeedMedia;

public class DownloadServiceInterfaceStub extends DownloadServiceInterface {

    @Override
    public void downloadNow(Context context, FeedItem item, boolean ignoreConstraints) {
    }

    @Override
    public void download(Context context, FeedItem item) {
    }

    @Override
    public void cancel(Context context, FeedMedia media) {
    }

    @Override
    public void cancelAll(Context context) {
    }

    @Override
    public int getNumberOfActiveDownloads(Context context) {
        return 0;
    }
}
