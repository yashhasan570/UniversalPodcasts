package com.universalpodcast.app.ui;

import android.os.Bundle;
import com.universalpodcast.app.storage.database.DBWriter;
import com.universalpodcast.app.model.feed.Feed;
import com.universalpodcast.app.ui.screen.feed.ItemFilterDialog;

import java.util.Set;

public class FeedItemFilterDialog extends ItemFilterDialog {
    private static final String ARGUMENT_FEED_ID = "feedId";

    public static FeedItemFilterDialog newInstance(Feed feed) {
        FeedItemFilterDialog dialog = new FeedItemFilterDialog();
        Bundle arguments = new Bundle();
        arguments.putSerializable(ARGUMENT_FILTER, feed.getItemFilter());
        arguments.putLong(ARGUMENT_FEED_ID, feed.getId());
        dialog.setArguments(arguments);
        return dialog;
    }

    @Override
    public void onFilterChanged(Set<String> newFilterValues) {
        long feedId = getArguments().getLong(ARGUMENT_FEED_ID);
        DBWriter.setFeedItemsFilter(feedId, newFilterValues);
    }
}
