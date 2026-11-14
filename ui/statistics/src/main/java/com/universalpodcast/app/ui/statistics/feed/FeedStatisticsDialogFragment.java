package com.universalpodcast.app.ui.statistics.feed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import com.universalpodcast.app.ui.appstartintent.MainActivityStarter;
import com.universalpodcast.app.ui.statistics.R;
import com.universalpodcast.app.ui.statistics.databinding.FeedStatisticsDialogBinding;

public class FeedStatisticsDialogFragment extends BottomSheetDialogFragment {
    private static final String EXTRA_FEED_ID = "com.universalpodcast.app.extra.feedId";
    private static final String EXTRA_FEED_TITLE = "com.universalpodcast.app.extra.feedTitle";

    public static FeedStatisticsDialogFragment newInstance(long feedId, String feedTitle) {
        FeedStatisticsDialogFragment fragment = new FeedStatisticsDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putLong(EXTRA_FEED_ID, feedId);
        arguments.putString(EXTRA_FEED_TITLE, feedTitle);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FeedStatisticsDialogBinding binding = FeedStatisticsDialogBinding.inflate(inflater, container, false);
        binding.title.setText(getArguments().getString(EXTRA_FEED_TITLE));
        binding.openPodcastButton.setOnClickListener(v -> {
            long feedId = getArguments().getLong(EXTRA_FEED_ID);
            new MainActivityStarter(getContext()).withOpenFeed(feedId).start();
            dismiss();
        });
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        long feedId = getArguments().getLong(EXTRA_FEED_ID);
        getChildFragmentManager().beginTransaction().replace(R.id.statisticsContainer,
                        FeedStatisticsFragment.newInstance(feedId, true), "feed_statistics_fragment")
                .commitAllowingStateLoss();
    }
}
