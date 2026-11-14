package com.universalpodcast.app.net.download.service.episode.autodownload;

import com.universalpodcast.app.storage.preferences.UserPreferences;

public abstract class EpisodeCleanupAlgorithmFactory {
    public static EpisodeCleanupAlgorithm build() {
        int cleanupValue = UserPreferences.getEpisodeCleanupValue();
        switch (cleanupValue) {
            case UserPreferences.EPISODE_CLEANUP_EXCEPT_FAVORITE:
                return new ExceptFavoriteCleanupAlgorithm();
            case UserPreferences.EPISODE_CLEANUP_QUEUE:
                return new APQueueCleanupAlgorithm();
            case UserPreferences.EPISODE_CLEANUP_NULL:
                return new APNullCleanupAlgorithm();
            default:
                return new APCleanupAlgorithm(cleanupValue);
        }
    }
}
