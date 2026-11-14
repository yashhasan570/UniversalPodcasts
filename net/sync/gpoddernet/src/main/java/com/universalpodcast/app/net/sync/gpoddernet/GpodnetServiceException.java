package com.universalpodcast.app.net.sync.gpoddernet;

import com.universalpodcast.app.net.sync.serviceinterface.SyncServiceException;

public class GpodnetServiceException extends SyncServiceException {
    private static final long serialVersionUID = 1L;

    public GpodnetServiceException(String message) {
        super(message);
    }

    public GpodnetServiceException(Throwable e) {
        super(e);
    }
}
