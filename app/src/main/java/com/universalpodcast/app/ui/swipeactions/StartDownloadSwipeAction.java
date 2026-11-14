package com.universalpodcast.app.ui.swipeactions;

import android.content.Context;
import androidx.fragment.app.Fragment;
import com.universalpodcast.app.R;
import com.universalpodcast.app.actionbutton.DownloadActionButton;
import com.universalpodcast.app.event.MessageEvent;
import com.universalpodcast.app.model.feed.FeedItem;
import com.universalpodcast.app.model.feed.FeedItemFilter;
import org.greenrobot.eventbus.EventBus;

public class StartDownloadSwipeAction implements SwipeAction {

    @Override
    public String getId() {
        return START_DOWNLOAD;
    }

    @Override
    public int getActionIcon() {
        return R.drawable.ic_download;
    }

    @Override
    public int getActionColor() {
        return R.attr.icon_green;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.download_label);
    }

    @Override
    public void performAction(FeedItem item, Fragment fragment, FeedItemFilter filter) {
        if (item.getMedia() == null) {
            EventBus.getDefault().post(new MessageEvent(fragment.getString(R.string.no_media_label)));
        } else if (item.isDownloaded()) {
            EventBus.getDefault().post(new MessageEvent(fragment.getString(R.string.already_downloaded)));
        } else {
            new DownloadActionButton(item).onClick(fragment.requireContext());
        }
    }

    @Override
    public boolean willRemove(FeedItemFilter filter, FeedItem item) {
        return false;
    }
}
