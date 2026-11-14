package com.universalpodcast.app.ui.swipeactions;

import android.content.Context;

import androidx.fragment.app.Fragment;

import com.universalpodcast.app.R;
import com.universalpodcast.app.storage.database.DBWriter;
import com.universalpodcast.app.model.feed.FeedItem;
import com.universalpodcast.app.model.feed.FeedItemFilter;

public class RemoveFromFavoritesSwipeAction implements SwipeAction {

    @Override
    public String getId() {
        return REMOVE_FROM_FAVORITES;
    }

    @Override
    public int getActionIcon() {
        return R.drawable.ic_star_border;
    }

    @Override
    public int getActionColor() {
        return R.attr.icon_yellow;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.remove_from_favorite_label);
    }

    @Override
    public void performAction(FeedItem item, Fragment fragment, FeedItemFilter filter) {
        DBWriter.toggleFavoriteItem(item);
    }

    @Override
    public boolean willRemove(FeedItemFilter filter, FeedItem item) {
        return filter.showIsFavorite;
    }
}
