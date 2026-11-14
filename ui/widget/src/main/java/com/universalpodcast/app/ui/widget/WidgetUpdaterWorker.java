package com.universalpodcast.app.ui.widget;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.universalpodcast.app.storage.database.DBReader;
import com.universalpodcast.app.storage.preferences.PlaybackPreferences;
import com.universalpodcast.app.model.playback.Playable;
import com.universalpodcast.app.playback.base.PlayerStatus;
import com.universalpodcast.app.ui.episodes.PlaybackSpeedUtils;

public class WidgetUpdaterWorker extends Worker {

    private static final String TAG = "WidgetUpdaterWorker";

    public WidgetUpdaterWorker(@NonNull final Context context,
                               @NonNull final WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void enqueueWork(final Context context) {
        final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(WidgetUpdaterWorker.class).build();
        WorkManager.getInstance(context).enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, workRequest);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            updateWidget();
        } catch (final Exception e) {
            Log.d(TAG, "Failed to update Universal Podcasts widget: ", e);
            return Result.failure();
        }
        return Result.success();
    }

    /**
     * Loads the current media from the database and updates the widget in a background job.
     */
    private void updateWidget() {
        final Playable media = DBReader.getFeedMedia(PlaybackPreferences.getCurrentlyPlayingFeedMediaId());
        if (media != null) {
            WidgetUpdater.updateWidget(getApplicationContext(),
                    new WidgetUpdater.WidgetState(media, PlayerStatus.STOPPED,
                            media.getPosition(), media.getDuration(),
                            PlaybackSpeedUtils.getCurrentPlaybackSpeed(media)));
        } else {
            WidgetUpdater.updateWidget(getApplicationContext(),
                    new WidgetUpdater.WidgetState(PlayerStatus.STOPPED));
        }
    }
}
