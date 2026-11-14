package com.universalpodcast.app.net.download.service.feed.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.universalpodcast.app.model.download.DownloadRequest;

public interface DownloaderFactory {
    @Nullable
    Downloader create(@NonNull DownloadRequest request);
}