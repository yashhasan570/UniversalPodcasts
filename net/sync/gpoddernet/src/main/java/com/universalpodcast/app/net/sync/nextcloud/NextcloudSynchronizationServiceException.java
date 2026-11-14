package com.universalpodcast.app.net.sync.nextcloud;

import com.universalpodcast.app.net.sync.serviceinterface.SyncServiceException;

public class NextcloudSynchronizationServiceException extends SyncServiceException {
    public NextcloudSynchronizationServiceException(Throwable e) {
        super(e);
    }
}
