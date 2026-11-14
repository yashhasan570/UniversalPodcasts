package com.universalpodcast.app.ui.screen.playback.audio;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.media3.session.MediaController;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import com.universalpodcast.app.model.feed.Feed;
import com.universalpodcast.app.playback.service.PlaybackController;
import com.universalpodcast.app.playback.service.PlaybackServiceInterface;
import com.universalpodcast.app.playback.service.PlaybackServiceStarter;
import com.universalpodcast.app.storage.database.DBReader;
import com.universalpodcast.app.storage.database.DBWriter;
import com.universalpodcast.app.storage.preferences.PlaybackPreferences;
import com.universalpodcast.app.ui.appstartintent.MainActivityStarter;
import com.universalpodcast.app.ui.appstartintent.MediaButtonStarter;
import com.universalpodcast.app.ui.appstartintent.OnlineFeedviewActivityStarter;
import com.universalpodcast.app.ui.chapters.ChapterUtils;
import com.universalpodcast.app.ui.episodes.PlaybackSpeedUtils;
import com.universalpodcast.app.ui.episodes.TimeSpeedConverter;
import com.universalpodcast.app.ui.screen.playback.MediaPlayerErrorDialog;
import com.universalpodcast.app.ui.screen.playback.PlayButton;
import com.universalpodcast.app.ui.screen.playback.SleepTimerDialog;
import com.universalpodcast.app.ui.screen.playback.TranscriptDialogFragment;
import com.universalpodcast.app.ui.screen.playback.VariableSpeedDialog;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;

import com.universalpodcast.app.BuildConfig;
import com.universalpodcast.app.R;
import com.universalpodcast.app.activity.MainActivity;
import com.universalpodcast.app.ui.common.Converter;
import com.universalpodcast.app.ui.screen.feed.preferences.SkipPreferenceDialog;
import com.universalpodcast.app.event.FeedItemEvent;
import com.universalpodcast.app.event.PlayerErrorEvent;
import com.universalpodcast.app.event.PlayerStatusEvent;
import com.universalpodcast.app.event.playback.BufferUpdateEvent;
import com.universalpodcast.app.event.playback.PlaybackPositionEvent;
import com.universalpodcast.app.event.playback.PlaybackServiceEvent;
import com.universalpodcast.app.event.playback.SleepTimerUpdatedEvent;
import com.universalpodcast.app.event.playback.SpeedChangedEvent;
import com.universalpodcast.app.ui.episodeslist.FeedItemMenuHandler;
import com.universalpodcast.app.model.feed.Chapter;
import com.universalpodcast.app.model.feed.FeedItem;
import com.universalpodcast.app.model.feed.FeedMedia;
import com.universalpodcast.app.model.playback.Playable;
import com.universalpodcast.app.playback.cast.CastEnabledActivity;
import com.universalpodcast.app.storage.preferences.UserPreferences;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Shows the audio player.
 */
public class AudioPlayerFragment extends Fragment implements
        ChapterSeekBar.OnSeekBarChangeListener, MaterialToolbar.OnMenuItemClickListener {
    public static final String TAG = "AudioPlayerFragment";
    public static final int POS_COVER = 0;
    public static final int POS_DESCRIPTION = 1;
    private static final int NUM_CONTENT_FRAGMENTS = 2;

    private TextView txtvPlaybackSpeed;
    private ViewPager2 pager;
    private TextView txtvPosition;
    private TextView txtvLength;
    private ChapterSeekBar sbPosition;
    private ImageButton butRev;
    private TextView txtvRev;
    private PlayButton butPlay;
    private ImageButton butFF;
    private TextView txtvFF;
    private ImageButton butSkip;
    private MaterialToolbar toolbar;
    private ProgressBar progressIndicator;
    private CardView cardViewSeek;
    private TextView txtvSeek;

    private FeedMedia currentMedia;
    private Disposable disposable;
    private boolean showTimeLeft;
    private boolean seekedToChapterStart = false;
    private int currentChapterIndex = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.audioplayer_fragment, container, false);
        root.setOnTouchListener((v, event) -> true); // Avoid clicks going through player to fragments below
        toolbar = root.findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setNavigationOnClickListener(v ->
                ((MainActivity) getActivity()).getBottomSheet().setState(BottomSheetBehavior.STATE_COLLAPSED));
        toolbar.setOnMenuItemClickListener(this);
        toolbar.inflateMenu(R.menu.mediaplayer);

        ExternalPlayerFragment externalPlayerFragment = new ExternalPlayerFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.playerFragment, externalPlayerFragment, ExternalPlayerFragment.TAG)
                .commit();

        txtvPlaybackSpeed = root.findViewById(R.id.txtvPlaybackSpeed);
        sbPosition = root.findViewById(R.id.sbPosition);
        txtvPosition = root.findViewById(R.id.txtvPosition);
        txtvLength = root.findViewById(R.id.txtvLength);
        butRev = root.findViewById(R.id.butRev);
        txtvRev = root.findViewById(R.id.txtvRev);
        butPlay = root.findViewById(R.id.butPlay);
        butFF = root.findViewById(R.id.butFF);
        txtvFF = root.findViewById(R.id.txtvFF);
        butSkip = root.findViewById(R.id.butSkip);
        progressIndicator = root.findViewById(R.id.progLoading);
        cardViewSeek = root.findViewById(R.id.cardViewSeek);
        txtvSeek = root.findViewById(R.id.txtvSeek);

        setupLengthTextView();
        setupControlButtons();
        final ImageButton butPlaybackSpeed = root.findViewById(R.id.butPlaybackSpeed);
        butPlaybackSpeed.setOnClickListener(v -> new VariableSpeedDialog().show(getChildFragmentManager(), null));
        sbPosition.setOnSeekBarChangeListener(this);

        pager = root.findViewById(R.id.pager);
        pager.setAdapter(new AudioPlayerPagerAdapter(this));
        //noinspection WrongConstant
        pager.setOffscreenPageLimit(NUM_CONTENT_FRAGMENTS);
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                pager.post(() -> {
                    if (getActivity() != null) {
                        // By the time this is posted, the activity might be closed again.
                        ((MainActivity) getActivity()).getBottomSheet().updateScrollingChild();
                    }
                });
            }
        });

        return root;
    }

    private void setChapterDividers() {
        if (currentMedia == null) {
            return;
        }

        float[] dividerPos = null;

        if (currentMedia.getChapters() != null && !currentMedia.getChapters().isEmpty()) {
            List<Chapter> chapters = currentMedia.getChapters();
            int duration = currentMedia.getDuration();
            if (duration > 0) {
                dividerPos = new float[chapters.size()];
                for (int i = 0; i < chapters.size(); i++) {
                    dividerPos[i] = chapters.get(i).getStart() / (float) duration;
                }
            }
        }

        sbPosition.setDividerPos(dividerPos);
    }

    private void setupControlButtons() {
        butRev.setOnClickListener(v -> {
            if (BuildConfig.USE_MEDIA3_PLAYBACK_SERVICE) {
                final FeedMedia media = currentMedia;
                PlaybackController.bindToMedia3Service(getContext(), controller -> {
                    if (controller.getCurrentMediaItem() != null) {
                        controller.seekBack();
                    } else if (media != null) {
                        seekWithoutPlaybackService(media,
                                Math.max(0, media.getPosition() - UserPreferences.getRewindSecs() * 1000));
                    }
                });
            } else {
                PlaybackController.bindToService(getActivity(), playbackService ->
                        playbackService.seekTo(playbackService.getCurrentPosition()
                                - UserPreferences.getRewindSecs() * 1000));
            }
        });
        butRev.setOnLongClickListener(v -> {
            SkipPreferenceDialog.showSkipPreference(getContext(),
                    SkipPreferenceDialog.SkipDirection.SKIP_REWIND, txtvRev);
            return true;
        });
        butPlay.setOnClickListener(v -> {
            if (PlaybackServiceInterface.isPlaying
                    && PlaybackPreferences.getCurrentPlayerStatus() == PlaybackPreferences.PLAYER_STATUS_PLAYING) {
                if (BuildConfig.USE_MEDIA3_PLAYBACK_SERVICE) {
                    PlaybackController.bindToMedia3Service(getContext(), MediaController::pause);
                } else {
                    getActivity().sendBroadcast(
                            MediaButtonStarter.createIntent(getContext(), KeyEvent.KEYCODE_MEDIA_PAUSE));
                }
            } else {
                new PlaybackServiceStarter(getContext(), currentMedia)
                        .callEvenIfRunning(true)
                        .start();
            }
        });
        butFF.setOnClickListener(v -> {
            if (BuildConfig.USE_MEDIA3_PLAYBACK_SERVICE) {
                final FeedMedia media = currentMedia;
                PlaybackController.bindToMedia3Service(getContext(), controller -> {
                    if (controller.getCurrentMediaItem() != null) {
                        controller.seekForward();
                    } else if (media != null) {
                        seekWithoutPlaybackService(media, Math.min(media.getDuration(),
                                media.getPosition() + UserPreferences.getFastForwardSecs() * 1000));
                    }
                });
            } else {
                PlaybackController.bindToService(getActivity(), playbackService ->
                        playbackService.seekTo(playbackService.getCurrentPosition()
                                + UserPreferences.getFastForwardSecs() * 1000));
            }
        });
        butFF.setOnLongClickListener(v -> {
            SkipPreferenceDialog.showSkipPreference(getContext(),
                    SkipPreferenceDialog.SkipDirection.SKIP_FORWARD, txtvFF);
            return false;
        });
        butSkip.setOnClickListener(v -> {
            if (BuildConfig.USE_MEDIA3_PLAYBACK_SERVICE) {
                PlaybackController.bindToMedia3Service(getContext(), MediaController::seekToNextMediaItem);
            } else {
                getActivity().sendBroadcast(
                        MediaButtonStarter.createIntent(getContext(), KeyEvent.KEYCODE_MEDIA_NEXT));
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onItemsUpdate(FeedItemEvent event) {
        if (currentMedia == null) {
            return;
        }
        if (FeedItemEvent.indexOfItemWithId(event.items, currentMedia.getItemId()) != -1) {
            AudioPlayerFragment.this.loadMediaInfo(false);
        }
        if (event.items.isEmpty()) {
            // The unread update event is sometimes abused to trigger UI updates
            updatePosition(new PlaybackPositionEvent(currentMedia.getPosition(),
                    currentMedia.getDuration()));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlaybackServiceChanged(PlaybackServiceEvent event) {
        if (event.action == PlaybackServiceEvent.Action.SERVICE_SHUT_DOWN) {
            ((MainActivity) getActivity()).getBottomSheet().setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    private void setupLengthTextView() {
        showTimeLeft = UserPreferences.shouldShowRemainingTime();
        txtvLength.setOnClickListener(v -> {
            if (currentMedia == null) {
                return;
            }
            showTimeLeft = !showTimeLeft;
            UserPreferences.setShowRemainTimeSetting(showTimeLeft);
            updatePosition(new PlaybackPositionEvent(currentMedia.getPosition(),
                    currentMedia.getDuration()));
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updatePlaybackSpeedButton(SpeedChangedEvent event) {
        String speedStr = new DecimalFormat("0.00").format(event.getNewSpeed());
        txtvPlaybackSpeed.setText(speedStr);
    }

    private void loadMediaInfo(boolean includingChapters) {
        if (disposable != null) {
            disposable.dispose();
        }
        disposable = Maybe.<FeedMedia>create(emitter -> {
            FeedMedia media = DBReader.getFeedMedia(PlaybackPreferences.getCurrentlyPlayingFeedMediaId());
            if (media != null) {
                if (includingChapters) {
                    ChapterUtils.loadChapters(media, getContext(), false);
                }
                emitter.onSuccess(media);
            } else {
                emitter.onComplete();
            }
        })
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(media -> {
            currentMedia = media;
            updateUi();
            if (media.getChapters() == null && !includingChapters) {
                loadMediaInfo(true);
            }
        }, error -> Log.e(TAG, Log.getStackTraceString(error)));
    }

    private void updateUi() {
        if (currentMedia == null) {
            return;
        }
        updatePosition(new PlaybackPositionEvent(currentMedia.getPosition(), currentMedia.getDuration()));
        updatePlaybackSpeedButton(new SpeedChangedEvent(PlaybackSpeedUtils.getCurrentPlaybackSpeed(currentMedia)));
        setChapterDividers();
        setupOptionsMenu();
        boolean isPlaying = PlaybackServiceInterface.isPlaying
                && PlaybackPreferences.getCurrentPlayerStatus() == PlaybackPreferences.PLAYER_STATUS_PLAYING;
        butPlay.setIsShowPlay(!isPlaying);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayerStatusEvent(PlayerStatusEvent event) {
        loadMediaInfo(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void sleepTimerUpdate(SleepTimerUpdatedEvent event) {
        boolean timerActive = !event.isCancelled() && !event.isOver();
        toolbar.getMenu().findItem(R.id.set_sleeptimer_item).setVisible(!timerActive);
        toolbar.getMenu().findItem(R.id.disable_sleeptimer_item).setVisible(timerActive);
        if (event.isCancelled() || event.wasJustEnabled() || event.isOver()) {
            AudioPlayerFragment.this.loadMediaInfo(false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        loadMediaInfo(false);
        EventBus.getDefault().register(this);
        txtvRev.setText(NumberFormat.getInstance().format(UserPreferences.getRewindSecs()));
        txtvFF.setText(NumberFormat.getInstance().format(UserPreferences.getFastForwardSecs()));
    }

    @Override
    public void onStop() {
        super.onStop();
        progressIndicator.setVisibility(View.GONE);
        EventBus.getDefault().unregister(this);
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void bufferUpdate(BufferUpdateEvent event) {
        if (event.hasStarted()) {
            progressIndicator.setVisibility(View.VISIBLE);
        } else if (event.hasEnded()) {
            progressIndicator.setVisibility(View.GONE);
        } else if (currentMedia != null && !currentMedia.localFileAvailable()) {
            sbPosition.setSecondaryProgress((int) (event.getProgress() * sbPosition.getMax()));
        } else {
            sbPosition.setSecondaryProgress(0);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updatePosition(PlaybackPositionEvent event) {
        if (txtvPosition == null || txtvLength == null || sbPosition == null) {
            return;
        }

        float playbackSpeed = currentMedia != null ? PlaybackSpeedUtils.getCurrentPlaybackSpeed(currentMedia) : 1.0f;
        TimeSpeedConverter converter = new TimeSpeedConverter(playbackSpeed);
        int convertedPosition = converter.convert(event.getPosition());
        int convertedDuration = converter.convert(event.getDuration());
        int remainingTime = converter.convert(Math.max(event.getDuration() - event.getPosition(), 0));
        if (currentMedia != null) {
            currentChapterIndex = Chapter.getAfterPosition(currentMedia.getChapters(), convertedPosition);
        }
        Log.d(TAG, "currentPosition " + Converter.getDurationStringLong(convertedPosition));
        if (convertedPosition == Playable.INVALID_TIME || convertedDuration == Playable.INVALID_TIME) {
            Log.w(TAG, "Could not react to position observer update because of invalid time");
            return;
        }
        txtvPosition.setText(Converter.getDurationStringLong(convertedPosition));
        txtvPosition.setContentDescription(getString(R.string.position,
                Converter.getDurationStringLocalized(getContext(), convertedPosition)));
        showTimeLeft = UserPreferences.shouldShowRemainingTime();
        if (showTimeLeft) {
            txtvLength.setContentDescription(getString(R.string.remaining_time,
                    Converter.getDurationStringLocalized(getContext(), remainingTime)));
            txtvLength.setText(((remainingTime > 0) ? "-" : "") + Converter.getDurationStringLong(remainingTime));
        } else {
            txtvLength.setContentDescription(getString(R.string.chapter_duration,
                    Converter.getDurationStringLocalized(getContext(), convertedDuration)));
            txtvLength.setText(Converter.getDurationStringLong(convertedDuration));
        }

        if (!sbPosition.isPressed()) {
            float progress = ((float) event.getPosition()) / event.getDuration();
            sbPosition.setProgress((int) (progress * sbPosition.getMax()));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void mediaPlayerError(PlayerErrorEvent event) {
        MediaPlayerErrorDialog.show(getActivity(), event);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (currentMedia == null || txtvLength == null) {
            return;
        }

        if (fromUser) {
            float prog = progress / ((float) seekBar.getMax());
            float playbackSpeed = PlaybackSpeedUtils.getCurrentPlaybackSpeed(currentMedia);
            TimeSpeedConverter converter = new TimeSpeedConverter(playbackSpeed);
            int duration = currentMedia.getDuration();
            int position = converter.convert((int) (prog * duration));
            int newChapterIndex = Chapter.getAfterPosition(currentMedia.getChapters(), position);
            if (newChapterIndex > -1) {
                if (!sbPosition.isPressed() && currentChapterIndex != newChapterIndex) {
                    currentChapterIndex = newChapterIndex;
                    position = (int) currentMedia.getChapters().get(currentChapterIndex).getStart();
                    seekedToChapterStart = true;
                    final int positionFinal = position;
                    if (BuildConfig.USE_MEDIA3_PLAYBACK_SERVICE) {
                        PlaybackController.bindToMedia3Service(getContext(), controller ->
                                controller.seekTo(positionFinal));
                    } else {
                        PlaybackController.bindToService(getActivity(), playbackService ->
                                playbackService.seekTo(positionFinal));
                    }
                    sbPosition.highlightCurrentChapter();
                }
                txtvSeek.setText(currentMedia.getChapters().get(newChapterIndex).getTitle()
                                + "\n" + Converter.getDurationStringLong(position));
            } else {
                txtvSeek.setText(Converter.getDurationStringLong(position));
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // interrupt position Observer, restart later
        cardViewSeek.setScaleX(.8f);
        cardViewSeek.setScaleY(.8f);
        cardViewSeek.animate()
                .setInterpolator(new FastOutSlowInInterpolator())
                .alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(200)
                .start();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekedToChapterStart) {
            seekedToChapterStart = false;
        } else if (currentMedia != null) {
            final float prog = seekBar.getProgress() / ((float) seekBar.getMax());
            if (BuildConfig.USE_MEDIA3_PLAYBACK_SERVICE) {
                final FeedMedia media = currentMedia;
                PlaybackController.bindToMedia3Service(getContext(), controller -> {
                    if (controller.getCurrentMediaItem() != null) {
                        controller.seekTo((long) (controller.getDuration() * prog));
                    } else {
                        seekWithoutPlaybackService(media, (int) (media.getDuration() * prog));
                    }
                });
            } else {
                PlaybackController.bindToService(getActivity(), playbackService ->
                        playbackService.seekTo((int) (playbackService.getDuration() * prog)));
            }
        }
        cardViewSeek.setScaleX(1f);
        cardViewSeek.setScaleY(1f);
        cardViewSeek.animate()
                .setInterpolator(new FastOutSlowInInterpolator())
                .alpha(0f).scaleX(.8f).scaleY(.8f)
                .setDuration(200)
                .start();
    }

    private void seekWithoutPlaybackService(FeedMedia media, int position) {
        media.setPosition(position);
        DBWriter.setFeedMediaPlaybackInformation(media);
        EventBus.getDefault().post(new PlaybackPositionEvent(media.getPosition(), media.getDuration()));
    }

    public void setupOptionsMenu() {
        toolbar.getMenu().findItem(R.id.open_feed_item).setVisible(true);
        FeedItemMenuHandler.onPrepareMenu(toolbar.getMenu(),
                Collections.singletonList(currentMedia.getItem()));
        ((CastEnabledActivity) getActivity()).requestCastButton(toolbar.getMenu());
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (currentMedia == null) {
            return false;
        }

        final @Nullable FeedItem feedItem = currentMedia.getItem();
        if (feedItem != null && FeedItemMenuHandler.onMenuItemClicked(this, item.getItemId(), feedItem)) {
            return true;
        }

        final int itemId = item.getItemId();
        if (itemId == R.id.disable_sleeptimer_item || itemId == R.id.set_sleeptimer_item) {
            new SleepTimerDialog().show(getChildFragmentManager(), "SleepTimerDialog");
            return true;
        } else if (itemId == R.id.transcript_item) {
            new TranscriptDialogFragment().show(
                    getActivity().getSupportFragmentManager(), TranscriptDialogFragment.TAG);
            return true;
        } else if (itemId == R.id.open_feed_item) {
            if (feedItem != null) {
                openFeed(feedItem.getFeed());
            }
            return true;
        }
        return false;
    }

    private void openFeed(Feed feed) {
        if (feed == null) {
            return;
        }
        if (feed.getState() == Feed.STATE_NOT_SUBSCRIBED) {
            startActivity(new OnlineFeedviewActivityStarter(getContext(), feed.getDownloadUrl()).getIntent());
        } else {
            new MainActivityStarter(getContext()).withOpenFeed(feed.getId()).withClearTop().start();
        }
    }

    public void fadePlayerToToolbar(float slideOffset) {
        float playerFadeProgress = Math.max(0.0f, Math.min(0.2f, slideOffset - 0.2f)) / 0.2f;
        View player = getView().findViewById(R.id.playerFragment);
        player.setAlpha(1 - playerFadeProgress);
        player.setVisibility(playerFadeProgress > 0.99f ? View.INVISIBLE : View.VISIBLE);
        float toolbarFadeProgress = Math.max(0.0f, Math.min(0.2f, slideOffset - 0.6f)) / 0.2f;
        toolbar.setAlpha(toolbarFadeProgress);
        toolbar.setVisibility(toolbarFadeProgress < 0.01f ? View.INVISIBLE : View.VISIBLE);
    }

    private static class AudioPlayerPagerAdapter extends FragmentStateAdapter {
        private static final String TAG = "AudioPlayerPagerAdapter";

        public AudioPlayerPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Log.d(TAG, "getItem(" + position + ")");

            switch (position) {
                case POS_COVER:
                    return new CoverFragment();
                default:
                case POS_DESCRIPTION:
                    return new ItemDescriptionFragment();
            }
        }

        @Override
        public int getItemCount() {
            return NUM_CONTENT_FRAGMENTS;
        }
    }

    public void scrollToPage(int page, boolean smoothScroll) {
        if (pager == null) {
            return;
        }

        pager.setCurrentItem(page, smoothScroll);

        Fragment visibleChild = getChildFragmentManager().findFragmentByTag("f" + POS_DESCRIPTION);
        if (visibleChild instanceof ItemDescriptionFragment) {
            ((ItemDescriptionFragment) visibleChild).scrollToTop();
        }
    }

    public void scrollToPage(int page) {
        scrollToPage(page, false);
    }
}
