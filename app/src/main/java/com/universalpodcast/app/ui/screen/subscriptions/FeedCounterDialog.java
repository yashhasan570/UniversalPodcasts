package com.universalpodcast.app.ui.screen.subscriptions;

import android.content.Context;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.universalpodcast.app.R;
import com.universalpodcast.app.event.FeedItemEvent;
import com.universalpodcast.app.model.feed.FeedCounter;
import com.universalpodcast.app.storage.preferences.UserPreferences;
import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FeedCounterDialog {
    public static void showDialog(Context context) {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(context);
        dialog.setTitle(context.getString(R.string.pref_nav_drawer_feed_counter_title));
        dialog.setNegativeButton(android.R.string.cancel, (d, listener) -> d.dismiss());

        int selected = UserPreferences.getFeedCounterSetting().id;
        List<String> entryValues =
                Arrays.asList(context.getResources().getStringArray(R.array.nav_drawer_feed_counter_values));
        final int selectedIndex = entryValues.indexOf("" + selected);

        String[] items = context.getResources().getStringArray(R.array.nav_drawer_feed_counter_options);
        dialog.setSingleChoiceItems(items, selectedIndex, (d, which) -> {
            if (selectedIndex != which) {
                UserPreferences.setFeedCounterSetting(
                        FeedCounter.fromOrdinal(Integer.parseInt(entryValues.get(which))));
                //Update subscriptions
                EventBus.getDefault().post(new FeedItemEvent(Collections.emptyList(), true));
            }
            d.dismiss();
        });
        dialog.show();
    }
}
