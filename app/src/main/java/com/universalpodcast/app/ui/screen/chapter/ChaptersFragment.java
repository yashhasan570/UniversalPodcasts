package com.universalpodcast.app.ui.screen.chapter;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.universalpodcast.app.R;
import com.universalpodcast.app.event.PlayerStatusEvent;
import com.universalpodcast.app.event.playback.PlaybackPositionEvent;
import com.universalpodcast.app.model.feed.Chapter;
import com.universalpodcast.app.model.feed.FeedMedia;
import com.universalpodcast.app.model.playback.Playable;
import com.universalpodcast.app.playback.service.PlaybackController;
import com.universalpodcast.app.storage.database.DBReader;
import com.universalpodcast.app.storage.preferences.PlaybackPreferences;
import com.universalpodcast.app.ui.chapters.ChapterUtils;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ChaptersFragment extends AppCompatDialogFragment {
    public static final String TAG = "ChaptersFragment";
    private ChaptersListAdapter adapter;
    private Disposable disposable;
    private int focusedChapter = -1;
    private Playable media;
    private LinearLayoutManager layoutManager;
    private ProgressBar progressBar;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.chapters_label))
                .setView(onCreateView(getLayoutInflater()))
                .setPositiveButton(getString(R.string.close_label), null) //dismisses
                .setNeutralButton(getString(R.string.refresh_label), null)
                .create();
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setVisibility(View.INVISIBLE);
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            loadMediaInfo(true);
        });

        return dialog;
    }


    public View onCreateView(@NonNull LayoutInflater inflater) {
        View root = inflater.inflate(R.layout.simple_list_fragment, null, false);
        root.findViewById(R.id.toolbar).setVisibility(View.GONE);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        progressBar = root.findViewById(R.id.progLoading);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation()));

        adapter = new ChaptersListAdapter(getActivity(), pos -> {
            Chapter chapter = adapter.getItem(pos);
            PlaybackController.bindToMedia3Service(getActivity(), controller -> {
                if (!controller.isPlaying()) {
                    controller.play();
                }
                controller.seekTo(chapter.getStart());
            });
            updateChapterSelection(pos, true);
        });
        recyclerView.setAdapter(adapter);

        progressBar.setVisibility(View.VISIBLE);

        CoordinatorLayout.LayoutParams wrapHeight = new CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.MATCH_PARENT, CoordinatorLayout.LayoutParams.WRAP_CONTENT);
        recyclerView.setLayoutParams(wrapHeight);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        loadMediaInfo(false);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (disposable != null) {
            disposable.dispose();
        }
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayerStatusEvent(PlayerStatusEvent event) {
        loadMediaInfo(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PlaybackPositionEvent event) {
        updateChapterSelection(getCurrentChapter(media, event.getPosition()), false);
        adapter.notifyTimeChanged(event.getPosition());
    }

    private int getCurrentChapter(Playable media, int position) {
        if (media == null) {
            return -1;
        }
        return Chapter.getAfterPosition(media.getChapters(), position);
    }

    private void loadMediaInfo(boolean forceRefresh) {
        if (disposable != null) {
            disposable.dispose();
        }
        disposable = Maybe.create(emitter -> {
            Playable media = DBReader.getFeedMedia(PlaybackPreferences.getCurrentlyPlayingFeedMediaId());
            if (media != null) {
                ChapterUtils.loadChapters(media, getContext(), forceRefresh);
                emitter.onSuccess(media);
            } else {
                emitter.onComplete();
            }
        })
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(media -> onMediaChanged((Playable) media),
                error -> Log.e(TAG, Log.getStackTraceString(error)));
    }

    private void onMediaChanged(Playable media) {
        this.media = media;
        focusedChapter = -1;
        if (adapter == null) {
            return;
        }
        if (media.getChapters() != null && media.getChapters().size() == 0) {
            dismiss();
            Toast.makeText(getContext(), R.string.no_chapters_label, Toast.LENGTH_LONG).show();
        } else {
            progressBar.setVisibility(View.GONE);
        }
        adapter.setMedia(media);
        ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_NEUTRAL).setVisibility(View.INVISIBLE);
        if (media instanceof FeedMedia && ((FeedMedia) media).getItem() != null
                && !TextUtils.isEmpty(((FeedMedia) media).getItem().getPodcastIndexChapterUrl())) {
            ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_NEUTRAL).setVisibility(View.VISIBLE);
        }
        int positionOfCurrentChapter = getCurrentChapter(media, media.getPosition());
        updateChapterSelection(positionOfCurrentChapter, true);
    }

    private void updateChapterSelection(int position, boolean scrollTo) {
        if (adapter == null) {
            return;
        }

        if (position != -1 && focusedChapter != position) {
            focusedChapter = position;
            adapter.notifyChapterChanged(focusedChapter);
            if (scrollTo && (layoutManager.findFirstCompletelyVisibleItemPosition() >= position
                    || layoutManager.findLastCompletelyVisibleItemPosition() <= position)) {
                layoutManager.scrollToPositionWithOffset(position, 100);
            }
        }
    }
}
