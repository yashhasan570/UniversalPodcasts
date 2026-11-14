package com.universalpodcast.app.ui.screen.home.sections;

import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.universalpodcast.app.R;
import com.universalpodcast.app.activity.MainActivity;
import com.universalpodcast.app.event.DownloadLogEvent;
import com.universalpodcast.app.event.FeedItemEvent;
import com.universalpodcast.app.event.PlayerStatusEvent;
import com.universalpodcast.app.event.playback.PlaybackPositionEvent;
import com.universalpodcast.app.model.feed.FeedItem;
import com.universalpodcast.app.model.feed.FeedItemFilter;
import com.universalpodcast.app.model.feed.SortOrder;
import com.universalpodcast.app.storage.database.DBReader;
import com.universalpodcast.app.storage.preferences.UserPreferences;
import com.universalpodcast.app.ui.MenuItemUtils;
import com.universalpodcast.app.ui.episodeslist.EpisodeItemListAdapter;
import com.universalpodcast.app.ui.episodeslist.EpisodeItemViewHolder;
import com.universalpodcast.app.ui.screen.download.CompletedDownloadsFragment;
import com.universalpodcast.app.ui.screen.home.HomeSection;
import com.universalpodcast.app.ui.swipeactions.SwipeActions;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class DownloadsSection extends HomeSection {
    public static final String TAG = "DownloadsSection";
    private static final int NUM_EPISODES = 2;
    private static final FeedItemFilter FILTER_DOWNLOADED = new FeedItemFilter(
            FeedItemFilter.DOWNLOADED, FeedItemFilter.INCLUDE_ALL_FEED_STATES);
    private EpisodeItemListAdapter adapter;
    private List<FeedItem> items;
    private Disposable disposable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        viewBinding.recyclerView.setPadding(0, 0, 0, 0);
        viewBinding.recyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        viewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        adapter = new EpisodeItemListAdapter(requireActivity()) {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                super.onCreateContextMenu(menu, v, menuInfo);
                MenuItemUtils.setOnClickListeners(menu, DownloadsSection.this::onContextItemSelected);
            }
        };
        adapter.setDummyViews(NUM_EPISODES);
        viewBinding.recyclerView.setAdapter(adapter);
        viewBinding.emptyLabel.setText(R.string.home_downloads_empty_text);

        SwipeActions swipeActions = new SwipeActions(this, CompletedDownloadsFragment.TAG);
        swipeActions.attachTo(viewBinding.recyclerView);
        swipeActions.setFilter(new FeedItemFilter(FeedItemFilter.DOWNLOADED));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadItems();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Override
    protected void handleMoreClick() {
        ((MainActivity) requireActivity()).loadChildFragment(new CompletedDownloadsFragment());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FeedItemEvent event) {
        loadItems();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PlaybackPositionEvent event) {
        if (adapter == null) {
            return;
        }
        for (int i = 0; i < adapter.getItemCount(); i++) {
            EpisodeItemViewHolder holder = (EpisodeItemViewHolder)
                    viewBinding.recyclerView.findViewHolderForAdapterPosition(i);
            if (holder != null && holder.isPlayingItem()) {
                holder.notifyPlaybackPositionUpdated(event);
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadLogChanged(DownloadLogEvent event) {
        loadItems();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayerStatusChanged(PlayerStatusEvent event) {
        loadItems();
    }

    @Override
    protected String getSectionTitle() {
        return getString(R.string.home_downloads_title);
    }

    @Override
    protected String getMoreLinkTitle() {
        return getString(R.string.downloads_label);
    }

    private void loadItems() {
        if (disposable != null) {
            disposable.dispose();
        }
        SortOrder sortOrder = UserPreferences.getDownloadsSortedOrder();
        disposable = Observable.fromCallable(() -> DBReader.getEpisodes(0, NUM_EPISODES, FILTER_DOWNLOADED, sortOrder))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(downloads -> {
                    items = downloads;
                    adapter.setDummyViews(0);
                    adapter.updateItems(items);
                    viewBinding.emptyLabel.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                }, error -> Log.e(TAG, Log.getStackTraceString(error)));
    }
}
