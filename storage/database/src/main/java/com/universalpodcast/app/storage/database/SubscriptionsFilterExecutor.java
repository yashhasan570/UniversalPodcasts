package com.universalpodcast.app.storage.database;

import com.universalpodcast.app.model.feed.Feed;
import com.universalpodcast.app.model.feed.FeedPreferences;
import com.universalpodcast.app.model.feed.SubscriptionsFilter;
import com.universalpodcast.app.storage.preferences.UserPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class SubscriptionsFilterExecutor {
    public static List<Feed> filter(List<Feed> items, Map<Long, Integer> feedCounters, SubscriptionsFilter filter) {
        List<Feed> result = new ArrayList<>();

        for (Feed item : items) {
            FeedPreferences itemPreferences = item.getPreferences();

            boolean globalAutodownload = UserPreferences.isEnableAutodownloadGlobal();
            boolean shouldItemAutoDownload = itemPreferences.isAutoDownload(globalAutodownload);
            // If the item does not meet a requirement, skip it.
            if (filter.showAutoDownloadEnabled && !shouldItemAutoDownload) {
                continue;
            } else if (filter.showAutoDownloadDisabled && shouldItemAutoDownload) {
                continue;
            }

            if (filter.showUpdatedEnabled && !itemPreferences.getKeepUpdated()) {
                continue;
            } else if (filter.showUpdatedDisabled && itemPreferences.getKeepUpdated()) {
                continue;
            }

            if (filter.showEpisodeNotificationEnabled && !itemPreferences.getShowEpisodeNotification()) {
                continue;
            } else if (filter.showEpisodeNotificationDisabled && itemPreferences.getShowEpisodeNotification()) {
                continue;
            }

            if (filter.hideNonSubscribedFeeds && item.getState() == Feed.STATE_NOT_SUBSCRIBED) {
                continue;
            }

            // If the item reaches here, it meets all criteria (except counter > 0)
            result.add(item);
        }

        if (filter.showIfCounterGreaterZero) {
            for (int i = result.size() - 1; i >= 0; i--) {
                if (!feedCounters.containsKey(result.get(i).getId()) || feedCounters.get(result.get(i).getId()) <= 0) {
                    result.remove(i);
                }
            }
        }

        return result;
    }
}
