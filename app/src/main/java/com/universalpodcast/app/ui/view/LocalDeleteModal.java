package com.universalpodcast.app.ui.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.universalpodcast.app.ui.i18n.R;
import com.universalpodcast.app.model.feed.FeedItem;

public class LocalDeleteModal {
    public static void showLocalFeedDeleteWarningIfNecessary(Context context, Iterable<FeedItem> items,
                                                             Runnable deleteCommand) {
        boolean anyLocalFeed = false;
        for (FeedItem item : items) {
            if (item.getFeed().isLocalFeed()) {
                anyLocalFeed = true;
                break;
            }
        }

        if (!anyLocalFeed) {
            deleteCommand.run();
            return;
        }

        new Handler(Looper.getMainLooper()).post(() -> new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.delete_label)
                .setMessage(R.string.delete_local_feed_confirmation_dialog_message)
                .setPositiveButton(R.string.delete_label, (dialog, which) -> deleteCommand.run())
                .setNegativeButton(R.string.cancel_label, null)
                .show());
    }
}
