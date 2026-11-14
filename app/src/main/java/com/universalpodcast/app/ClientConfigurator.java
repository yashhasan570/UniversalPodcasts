package com.universalpodcast.app;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.universalpodcast.app.net.download.service.episode.autodownload.AutoDownloadManagerImpl;
import com.universalpodcast.app.net.download.service.feed.FeedUpdateManagerImpl;
import com.universalpodcast.app.net.download.serviceinterface.AutoDownloadManager;
import com.universalpodcast.app.net.download.serviceinterface.FeedUpdateManager;
import com.universalpodcast.app.net.sync.service.SynchronizationQueueImpl;
import com.universalpodcast.app.net.sync.serviceinterface.SynchronizationQueue;
import com.universalpodcast.app.storage.preferences.SynchronizationSettings;
import com.universalpodcast.app.storage.preferences.SynchronizationCredentials;
import com.universalpodcast.app.storage.preferences.PlaybackPreferences;
import com.universalpodcast.app.storage.preferences.SleepTimerPreferences;
import com.universalpodcast.app.storage.preferences.UsageStatistics;
import com.universalpodcast.app.net.common.UserAgentInterceptor;
import com.universalpodcast.app.storage.preferences.UserPreferences;
import com.universalpodcast.app.net.common.AntennapodHttpClient;
import com.universalpodcast.app.net.download.serviceinterface.DownloadServiceInterface;
import com.universalpodcast.app.net.download.service.feed.DownloadServiceInterfaceImpl;
import com.universalpodcast.app.net.common.NetworkUtils;
import com.universalpodcast.app.net.ssl.SslProviderInstaller;
import com.universalpodcast.app.storage.database.PodDBAdapter;

import com.universalpodcast.app.ui.notifications.NotificationUtils;
import java.io.File;

public class ClientConfigurator {
    private static boolean initialized = false;

    public static synchronized void initialize(Context context) {
        if (initialized) {
            return;
        }
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            UserAgentInterceptor.USER_AGENT = "Universal Podcasts/" + packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        PodDBAdapter.init(context);
        UserPreferences.init(context);
        SynchronizationCredentials.init(context);
        SynchronizationSettings.init(context);
        UsageStatistics.init(context);
        PlaybackPreferences.init(context);
        SslProviderInstaller.install(context);
        NetworkUtils.init(context);
        DownloadServiceInterface.setImpl(new DownloadServiceInterfaceImpl());
        FeedUpdateManager.setInstance(new FeedUpdateManagerImpl());
        AutoDownloadManager.setInstance(new AutoDownloadManagerImpl());
        SynchronizationQueue.setInstance(new SynchronizationQueueImpl(context));
        AntennapodHttpClient.setCacheDirectory(new File(context.getCacheDir(), "okhttp"));
        AntennapodHttpClient.setProxyConfig(UserPreferences.getProxyConfig());
        SleepTimerPreferences.init(context);
        NotificationUtils.createChannels(context);
        initialized = true;
    }
}
