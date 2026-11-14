package de.test.universalpodcast.service.playback;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.universalpodcast.app.model.playback.MediaType;
import com.universalpodcast.app.model.playback.Playable;
import com.universalpodcast.app.playback.base.PlaybackServiceMediaPlayer;

public class DefaultPSMPCallback implements PlaybackServiceMediaPlayer.PSMPCallback {
    @Override
    public void statusChanged(PlaybackServiceMediaPlayer.PSMPInfo newInfo) {

    }

    @Override
    public void shouldStop() {

    }

    @Override
    public void onMediaChanged(boolean reloadUI) {

    }

    @Override
    public void onPostPlayback(@NonNull Playable media, boolean ended, boolean skipped, boolean playingNext) {

    }

    @Override
    public void onPlaybackStart(@NonNull Playable playable, int position) {

    }

    @Override
    public void onPlaybackPause(Playable playable, int position) {

    }

    @Override
    public Playable getNextInQueue(Playable currentMedia) {
        return null;
    }

    @Nullable
    @Override
    public Playable findMedia(@NonNull String url) {
        return null;
    }

    @Override
    public void onPlaybackEnded(MediaType mediaType, boolean stopPlaying) {

    }

    @Override
    public void ensureMediaInfoLoaded(@NonNull Playable media) {
    }

    @Override
    public void episodeFinishedPlayback() {
    }

    @Override
    public boolean shouldContinueToNextEpisode() {
        return false;
    }
}