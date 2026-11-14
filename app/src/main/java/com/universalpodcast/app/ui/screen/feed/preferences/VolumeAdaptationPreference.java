package com.universalpodcast.app.ui.screen.feed.preferences;

import android.content.Context;
import android.util.AttributeSet;

import java.util.Arrays;

import com.universalpodcast.app.model.feed.VolumeAdaptionSetting;
import com.universalpodcast.app.ui.preferences.preference.MaterialListPreference;

public class VolumeAdaptationPreference extends MaterialListPreference {
    public VolumeAdaptationPreference(Context context) {
        super(context);
    }

    public VolumeAdaptationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public CharSequence[] getEntries() {
        if (VolumeAdaptionSetting.isBoostSupported()) {
            return super.getEntries();
        } else {
            return Arrays.copyOfRange(super.getEntries(), 0, 3);
        }
    }
}
