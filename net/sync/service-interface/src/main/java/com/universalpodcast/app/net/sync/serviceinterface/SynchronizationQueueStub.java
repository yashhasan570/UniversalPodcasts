package com.universalpodcast.app.net.sync.serviceinterface;

import com.universalpodcast.app.model.feed.FeedMedia;

public class SynchronizationQueueStub extends SynchronizationQueue {
    @Override
    public void sync() {
    }

    @Override
    public void syncImmediately() {
    }

    @Override
    public void fullSync() {
    }

    @Override
    public void syncIfNotSyncedRecently() {
    }

    @Override
    public void clear() {
    }

    @Override
    public void enqueueFeedAdded(String downloadUrl) {
    }

    @Override
    public void enqueueFeedRemoved(String downloadUrl) {
    }

    @Override
    public void enqueueEpisodeAction(EpisodeAction action) {
    }

    @Override
    public void enqueueEpisodePlayed(FeedMedia media, int startPosition, boolean completed) {
    }
}
