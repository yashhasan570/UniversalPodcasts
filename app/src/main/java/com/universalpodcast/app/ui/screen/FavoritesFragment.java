package com.universalpodcast.app.ui.screen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.universalpodcast.app.R;
import com.universalpodcast.app.storage.database.DBReader;
import com.universalpodcast.app.model.feed.FeedItem;
import com.universalpodcast.app.model.feed.FeedItemFilter;
import com.universalpodcast.app.storage.preferences.UserPreferences;
import com.universalpodcast.app.ui.episodeslist.EpisodesListFragment;

import java.util.List;

public class FavoritesFragment extends EpisodesListFragment {
    public static final String TAG = "FavoritesFragment";
    private static final FeedItemFilter FILTER_FAVORITES = new FeedItemFilter(
            FeedItemFilter.IS_FAVORITE, FeedItemFilter.INCLUDE_ALL_FEED_STATES);
    private static Pair<Integer, Integer> scrollPosition = null;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = super.onCreateView(inflater, container, savedInstanceState);
        toolbar.inflateMenu(R.menu.favorites);
        toolbar.setTitle(R.string.favorite_episodes_label);
        updateToolbar();
        emptyView.setIcon(R.drawable.ic_star);
        emptyView.setTitle(R.string.no_fav_episodes_head_label);
        emptyView.setMessage(R.string.no_fav_episodes_label);
        return root;
    }

    @Override
    protected FeedItemFilter getFilter() {
        return FILTER_FAVORITES;
    }

    @Override
    protected String getFragmentTag() {
        return TAG;
    }

    @Override
    public void onPause() {
        super.onPause();
        scrollPosition = recyclerView.getScrollPosition();
    }

    @Override
    protected void onItemsFirstLoaded() {
        recyclerView.restoreScrollPosition(scrollPosition);
    }

    @NonNull
    @Override
    protected List<FeedItem> loadData() {
        return DBReader.getEpisodes(0, page * EPISODES_PER_PAGE, FILTER_FAVORITES,
                UserPreferences.getAllEpisodesSortOrder());
    }

    @NonNull
    @Override
    protected List<FeedItem> loadMoreData(int page) {
        return DBReader.getEpisodes((page - 1) * EPISODES_PER_PAGE, EPISODES_PER_PAGE, FILTER_FAVORITES,
                UserPreferences.getAllEpisodesSortOrder());
    }

    @Override
    protected int loadTotalItemCount() {
        return DBReader.getTotalEpisodeCount(FILTER_FAVORITES);
    }
}
