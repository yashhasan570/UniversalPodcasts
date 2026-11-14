package com.universalpodcast.app.ui.screen.preferences;

import android.os.Bundle;
import com.universalpodcast.app.R;
import com.universalpodcast.app.ui.preferences.screen.AnimatedPreferenceFragment;
import com.universalpodcast.app.ui.screen.FavoritesFragment;
import com.universalpodcast.app.ui.swipeactions.SwipeActionsDialog;
import com.universalpodcast.app.ui.screen.AllEpisodesFragment;
import com.universalpodcast.app.ui.screen.download.CompletedDownloadsFragment;
import com.universalpodcast.app.ui.screen.feed.FeedItemlistFragment;
import com.universalpodcast.app.ui.screen.InboxFragment;
import com.universalpodcast.app.ui.screen.PlaybackHistoryFragment;
import com.universalpodcast.app.ui.screen.queue.QueueFragment;

public class SwipePreferencesFragment extends AnimatedPreferenceFragment {
    private static final String PREF_SWIPE_QUEUE = "prefSwipeQueue";
    private static final String PREF_SWIPE_INBOX = "prefSwipeInbox";
    private static final String PREF_SWIPE_EPISODES = "prefSwipeEpisodes";
    private static final String PREF_SWIPE_DOWNLOADS = "prefSwipeDownloads";
    private static final String PREF_SWIPE_FEED = "prefSwipeFeed";
    private static final String PREF_SWIPE_HISTORY = "prefSwipeHistory";
    private static final String PREF_SWIPE_FAVORITES = "prefSwipeFavorites";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_swipe);

        findPreference(PREF_SWIPE_QUEUE).setOnPreferenceClickListener(preference -> {
            new SwipeActionsDialog(requireContext(), QueueFragment.TAG).show(() -> { });
            return true;
        });
        findPreference(PREF_SWIPE_INBOX).setOnPreferenceClickListener(preference -> {
            new SwipeActionsDialog(requireContext(), InboxFragment.TAG).show(() -> { });
            return true;
        });
        findPreference(PREF_SWIPE_EPISODES).setOnPreferenceClickListener(preference -> {
            new SwipeActionsDialog(requireContext(), AllEpisodesFragment.TAG).show(() -> { });
            return true;
        });
        findPreference(PREF_SWIPE_DOWNLOADS).setOnPreferenceClickListener(preference -> {
            new SwipeActionsDialog(requireContext(), CompletedDownloadsFragment.TAG).show(() -> { });
            return true;
        });
        findPreference(PREF_SWIPE_FEED).setOnPreferenceClickListener(preference -> {
            new SwipeActionsDialog(requireContext(), FeedItemlistFragment.TAG).show(() -> { });
            return true;
        });
        findPreference(PREF_SWIPE_HISTORY).setOnPreferenceClickListener(preference -> {
            new SwipeActionsDialog(requireContext(), PlaybackHistoryFragment.TAG).show(() -> { });
            return true;
        });
        findPreference(PREF_SWIPE_FAVORITES).setOnPreferenceClickListener(preference -> {
            new SwipeActionsDialog(requireContext(), FavoritesFragment.TAG).show(() -> { });
            return true;
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        ((PreferenceActivity) getActivity()).getSupportActionBar().setTitle(R.string.swipeactions_label);
    }

}
