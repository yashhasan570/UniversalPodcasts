package com.universalpodcast.app.ui.swipeactions;

import android.content.Context;
import androidx.fragment.app.Fragment;
import com.universalpodcast.app.R;
import com.universalpodcast.app.event.MessageEvent;
import com.universalpodcast.app.storage.database.DBWriter;
import com.universalpodcast.app.model.feed.FeedItem;
import com.universalpodcast.app.model.feed.FeedItemFilter;
import java.util.Collections;
import org.greenrobot.eventbus.EventBus;

public class MoveToBottomSwipeAction implements SwipeAction {

    @Override
    public String getId() {
        return MOVE_TO_BOTTOM;
    }

    @Override
    public int getActionIcon() {
        return R.drawable.ic_arrow_full_down;
    }

    @Override
    public int getActionColor() {
        return R.attr.colorAccent;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.move_to_bottom_label);
    }

    @Override
    public void performAction(FeedItem item, Fragment fragment, FeedItemFilter filter) {
        DBWriter.moveQueueItemsToBottom(Collections.singletonList(item));
        EventBus.getDefault().post(new MessageEvent(
                fragment.getResources().getQuantityString(R.plurals.move_to_bottom_message, 1, 1)));
    }

    @Override
    public boolean willRemove(FeedItemFilter filter, FeedItem item) {
        return false;
    }
}
