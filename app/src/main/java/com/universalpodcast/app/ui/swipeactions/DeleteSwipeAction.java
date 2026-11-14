package com.universalpodcast.app.ui.swipeactions;

import android.content.Context;
import androidx.fragment.app.Fragment;

import java.util.Collections;

import com.universalpodcast.app.R;
import com.universalpodcast.app.storage.database.DBWriter;
import com.universalpodcast.app.model.feed.FeedItem;
import com.universalpodcast.app.model.feed.FeedItemFilter;
import com.universalpodcast.app.ui.view.LocalDeleteModal;

public class DeleteSwipeAction implements SwipeAction {

    @Override
    public String getId() {
        return DELETE;
    }

    @Override
    public int getActionIcon() {
        return R.drawable.ic_delete;
    }

    @Override
    public int getActionColor() {
        return R.attr.icon_red;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.delete_label);
    }

    @Override
    public void performAction(FeedItem item, Fragment fragment, FeedItemFilter filter) {
        if (!item.isDownloaded()) {
            return;
        }
        LocalDeleteModal.showLocalFeedDeleteWarningIfNecessary(
                fragment.requireContext(), Collections.singletonList(item),
                () -> DBWriter.deleteFeedMediaOfItem(fragment.requireContext(), item.getMedia()));
    }

    @Override
    public boolean willRemove(FeedItemFilter filter, FeedItem item) {
        return filter.showDownloaded && item.isDownloaded();
    }
}
