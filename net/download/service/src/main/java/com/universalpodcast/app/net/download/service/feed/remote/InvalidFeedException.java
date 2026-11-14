package com.universalpodcast.app.net.download.service.feed.remote;

/**
 * Thrown if a feed has invalid attribute values.
 */
public class InvalidFeedException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidFeedException(String message) {
        super(message);
    }
}
