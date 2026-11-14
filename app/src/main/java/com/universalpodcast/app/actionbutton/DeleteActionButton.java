package com.universalpodcast.app.actionbutton;

import android.content.Context;
import android.view.View;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import java.util.Collections;

import com.universalpodcast.app.R;
import com.universalpodcast.app.model.feed.FeedItem;
import com.universalpodcast.app.model.feed.FeedMedia;
import com.universalpodcast.app.storage.database.DBWriter;
import com.universalpodcast.app.ui.view.LocalDeleteModal;

public class DeleteActionButton extends ItemActionButton {

    public DeleteActionButton(FeedItem item) {
        super(item);
    }

    @Override
    @StringRes
    public int getLabel() {
        return R.string.delete_label;
    }

    @Override
    @DrawableRes
    public int getDrawable() {
        return R.drawable.ic_delete;
    }

    @Override
    public void onClick(Context context) {
        final FeedMedia media = item.getMedia();
        if (media == null) {
            return;
        }

        LocalDeleteModal.showLocalFeedDeleteWarningIfNecessary(context, Collections.singletonList(item),
                () -> DBWriter.deleteFeedMediaOfItem(context, media));
    }

    @Override
    public int getVisibility() {
        if (item.getMedia() != null && item.getMedia().isDownloaded()) {
            return View.VISIBLE;
        }

        return View.INVISIBLE;
    }
}
