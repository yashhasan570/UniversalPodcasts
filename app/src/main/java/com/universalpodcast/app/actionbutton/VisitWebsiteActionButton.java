package com.universalpodcast.app.actionbutton;

import android.content.Context;
import android.view.View;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import com.universalpodcast.app.R;
import com.universalpodcast.app.model.feed.FeedItem;
import com.universalpodcast.app.ui.common.IntentUtils;

public class VisitWebsiteActionButton extends ItemActionButton {

    public VisitWebsiteActionButton(FeedItem item) {
        super(item);
    }

    @Override
    @StringRes
    public int getLabel() {
        return R.string.visit_website_label;
    }

    @Override
    @DrawableRes
    public int getDrawable() {
        return R.drawable.ic_web;
    }

    @Override
    public void onClick(Context context) {
        IntentUtils.openInBrowser(context, item.getLink());
    }

    @Override
    public int getVisibility() {
        return (item.getLink() == null) ? View.INVISIBLE : View.VISIBLE;
    }
}
