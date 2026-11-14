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
import com.universalpodcast.app.ui.episodeslist.HorizontalItemListAdapter;
import com.universalpodcast.app.ui.MenuItemUtils;
import com.universalpodcast.app.storage.database.DBReader;
import com.universalpodcast.app.event.EpisodeDownloadEvent;
import com.universalpodcast.app.event.FeedItemEvent;
import com.universalpodcast.app.event.PlayerStatusEvent;
import com.universalpodcast.app.event.QueueEvent;
import com.universalpodcast.app.event.playback.PlaybackPositionEvent;
import com.universalpodcast.app.ui.screen.queue.QueueFragment;
import com.universalpodcast.app.model.feed.FeedItem;
import com.universalpodcast.app.ui.screen.home.HomeSection;
import com.universalpodcast.app.ui.episodeslist.HorizontalItemViewHolder;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class QueueSection extends HomeSection {
    public static final String TAG = "QueueSection";
    private static final int NUM_EPISODES = 8;
    private HorizontalItemListAdapter listAdapter;
    private Disposable disposable;
    private List<FeedItem> queue = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        listAdapter = new HorizontalItemListAdapter((MainActivity) getActivity()) {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                super.onCreateContextMenu(menu, v, menuInfo);
                MenuItemUtils.setOnClickListeners(menu, QueueSection.this::onContextItemSelected);
            }
        };
        listAdapter.setDummyViews(NUM_EPISODES);
        viewBinding.recyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        viewBinding.recyclerView.setAdapter(listAdapter);
        int paddingHorizontal = (int) (12 * getResources().getDisplayMetrics().density);
        viewBinding.recyclerView.setPadding(paddingHorizontal, 0, paddingHorizontal, 0);
        viewBinding.emptyLabel.setText(R.string.home_continue_empty_text);
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
        ((MainActivity) requireActivity()).loadChildFragment(new QueueFragment());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onQueueChanged(QueueEvent event) {
        loadItems();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayerStatusChanged(PlayerStatusEvent event) {
        loadItems();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FeedItemEvent event) {
        Log.d(TAG, "onEventMainThread() called with: " + "event = [" + event + "]");
        if (queue == null) {
            return;
        }
        for (int i = 0, size = event.items.size(); i < size; i++) {
            FeedItem item = event.items.get(i);
            int pos = FeedItemEvent.indexOfItemWithId(queue, item.getId());
            if (pos >= 0) {
                queue.remove(pos);
                queue.add(pos, item);
                listAdapter.notifyItemChangedCompat(pos);
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EpisodeDownloadEvent event) {
        for (String downloadUrl : event.getUrls()) {
            int pos = EpisodeDownloadEvent.indexOfItemWithDownloadUrl(queue, downloadUrl);
            if (pos >= 0) {
                listAdapter.notifyItemChangedCompat(pos);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PlaybackPositionEvent event) {
        if (listAdapter == null) {
            return;
        }
        boolean foundCurrentlyPlayingItem = false;
        boolean currentlyPlayingItemIsFirst = true;
        for (int i = 0; i < listAdapter.getItemCount(); i++) {
            HorizontalItemViewHolder holder = (HorizontalItemViewHolder)
                    viewBinding.recyclerView.findViewHolderForAdapterPosition(i);
            if (holder == null) {
                continue;
            }
            if (holder.isCurrentlyPlayingItem()) {
                holder.notifyPlaybackPositionUpdated(event);
                foundCurrentlyPlayingItem = true;
                currentlyPlayingItemIsFirst = (i == 0);
                break;
            }
        }
        if (!foundCurrentlyPlayingItem || !currentlyPlayingItemIsFirst) {
            loadItems();
        }
    }

    @Override
    protected String getSectionTitle() {
        return getString(R.string.home_continue_title);
    }

    @Override
    protected String getMoreLinkTitle() {
        return getString(R.string.queue_label);
    }

    private void loadItems() {
        if (disposable != null) {
            disposable.dispose();
        }
        disposable = Observable.fromCallable(() -> DBReader.getPausedQueue(NUM_EPISODES))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(queue -> {
                    this.queue = queue;
                    listAdapter.setDummyViews(0);
                    listAdapter.updateData(queue);
                    viewBinding.emptyLabel.setVisibility(queue.isEmpty() ? View.VISIBLE : View.GONE);
                }, error -> Log.e(TAG, Log.getStackTraceString(error)));

    }
}
