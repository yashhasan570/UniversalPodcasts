package com.universalpodcast.app.ui.echo.screen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import com.universalpodcast.app.storage.database.DBReader;
import com.universalpodcast.app.storage.database.StatisticsItem;
import com.universalpodcast.app.ui.echo.R;
import com.universalpodcast.app.ui.echo.background.WaveformBackground;
import com.universalpodcast.app.ui.echo.databinding.SimpleEchoScreenBinding;

public class HoursPlayedScreen extends EchoScreen {
    private final SimpleEchoScreenBinding viewBinding;

    public HoursPlayedScreen(Context context, LayoutInflater layoutInflater) {
        super(context);
        viewBinding = SimpleEchoScreenBinding.inflate(layoutInflater);
        viewBinding.aboveLabel.setText(R.string.echo_hours_this_year);
        viewBinding.backgroundImage.setImageDrawable(new WaveformBackground(context));
    }

    private void display(long totalTime, int playedPodcasts) {
        viewBinding.largeLabel.setText(String.format(getEchoLanguage(), "%d", totalTime / 3600));
        viewBinding.belowLabel.setText(context.getResources()
                .getQuantityString(R.plurals.echo_hours_podcasts, playedPodcasts, playedPodcasts));
    }

    @Override
    public View getView() {
        return viewBinding.getRoot();
    }

    @Override
    public void postInvalidate() {
        viewBinding.backgroundImage.postInvalidate();
    }

    @Override
    public void startLoading(DBReader.StatisticsResult statisticsResult) {
        int playedPodcasts = 0;
        long totalTime = 0;
        for (StatisticsItem item : statisticsResult.feedTime) {
            totalTime += item.timePlayed;
            if (item.timePlayed > 0) {
                playedPodcasts++;
            }
        }
        display(totalTime, playedPodcasts);
    }
}
