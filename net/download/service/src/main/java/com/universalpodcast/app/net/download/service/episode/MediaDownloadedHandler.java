package com.universalpodcast.app.net.download.service.episode;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import androidx.annotation.NonNull;

import com.universalpodcast.app.model.MediaMetadataRetrieverCompat;
import com.universalpodcast.app.model.feed.Feed;
import com.universalpodcast.app.net.sync.serviceinterface.SynchronizationQueue;
import com.universalpodcast.app.ui.chapters.ChapterUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InterruptedIOException;
import java.util.concurrent.ExecutionException;

import com.universalpodcast.app.model.download.DownloadRequest;
import com.universalpodcast.app.model.download.DownloadResult;
import com.universalpodcast.app.storage.database.DBReader;
import com.universalpodcast.app.storage.database.DBWriter;
import com.universalpodcast.app.model.download.DownloadError;
import com.universalpodcast.app.model.feed.FeedItem;
import com.universalpodcast.app.model.feed.FeedMedia;
import com.universalpodcast.app.net.sync.serviceinterface.EpisodeAction;
import com.universalpodcast.app.ui.transcript.TranscriptUtils;

/**
 * Handles a completed media download.
 */
public class MediaDownloadedHandler implements Runnable {
    private static final String TAG = "MediaDownloadedHandler";
    private final DownloadRequest request;
    private final Context context;
    private DownloadResult updatedStatus;

    public MediaDownloadedHandler(@NonNull Context context, @NonNull DownloadResult status,
                                  @NonNull DownloadRequest request) {
        this.request = request;
        this.context = context;
        this.updatedStatus = status;
    }

    @Override
    public void run() {
        FeedMedia media = DBReader.getFeedMedia(request.getFeedfileId());
        if (media == null) {
            Log.e(TAG, "Could not find downloaded media object in database");
            return;
        }
        // media.setDownloaded modifies played state
        boolean broadcastUnreadStateUpdate = media.getItem() != null && media.getItem().isNew();
        media.setDownloaded(true, System.currentTimeMillis());
        media.setLocalFileUrl(request.getDestination());
        media.setSize(new File(request.getDestination()).length());
        media.checkEmbeddedPicture(); // enforce check

        try {
            // Cache chapters if file has them
            if (media.getItem() != null && !media.getItem().hasChapters()) {
                media.setChapters(ChapterUtils.loadChaptersFromMediaFile(media, context));
            }
            if (media.getItem() != null && media.getItem().getPodcastIndexChapterUrl() != null) {
                ChapterUtils.loadChaptersFromUrl(media.getItem().getPodcastIndexChapterUrl(), false);
            }
            FeedItem item = media.getItem();
            if (item != null && item.getTranscriptUrl() != null) {
                String transcript = TranscriptUtils.loadTranscriptFromUrl(item.getTranscriptUrl(), true);
                if (!StringUtils.isEmpty(transcript)) {
                    TranscriptUtils.storeTranscript(media, transcript);
                }
            }
        } catch (InterruptedIOException ignore) {
            // Ignore
        }

        // Get duration
        String durationStr = null;
        try (MediaMetadataRetrieverCompat mmr = new MediaMetadataRetrieverCompat()) {
            mmr.setDataSource(media.getLocalFileUrl());
            durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            media.setDuration(Integer.parseInt(durationStr));
            Log.d(TAG, "Duration of file is " + media.getDuration());
        } catch (NumberFormatException e) {
            Log.d(TAG, "Invalid file duration: " + durationStr);
        } catch (Exception e) {
            Log.e(TAG, "Get duration failed", e);
        }

        final FeedItem item = media.getItem();

        try {
            DBWriter.setFeedMedia(media).get();

            // we've received the media, we don't want to autodownload it again
            if (item != null) {
                item.disableAutoDownload();
                // setFeedItem() signals (via EventBus) that the item has been updated,
                // so we do it after the enclosing media has been updated above,
                // to ensure subscribers will get the updated FeedMedia as well
                DBWriter.setFeedItem(item, broadcastUnreadStateUpdate).get();
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "MediaHandlerThread was interrupted");
        } catch (ExecutionException e) {
            Log.e(TAG, "ExecutionException in MediaHandlerThread: " + e.getMessage());
            updatedStatus = new DownloadResult(media.getEpisodeTitle(), media.getId(),
                    FeedMedia.FEEDFILETYPE_FEEDMEDIA, false, DownloadError.ERROR_DB_ACCESS_ERROR, e.getMessage());
        }

        if (item != null && item.getFeed().getState() != Feed.STATE_NOT_SUBSCRIBED) {
            SynchronizationQueue.getInstance().enqueueEpisodeAction(
                    new EpisodeAction.Builder(item, EpisodeAction.DOWNLOAD)
                        .currentTimestamp()
                        .build());
        }
    }

    @NonNull
    public DownloadResult getUpdatedStatus() {
        return updatedStatus;
    }
}
