package com.universalpodcast.app.storage.preferences;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages preferences for accessing gpodder.net service and other sync providers
 */
public abstract class SynchronizationCredentials {
    private static final String PREF_NAME = "gpodder.net";
    private static final String PREF_USERNAME = "com.universalpodcast.app.preferences.gpoddernet.username";
    private static final String PREF_PASSWORD = "com.universalpodcast.app.preferences.gpoddernet.password";
    private static final String PREF_DEVICEID = "com.universalpodcast.app.preferences.gpoddernet.deviceID";
    private static final String PREF_HOSTNAME = "prefGpodnetHostname";

    private static SharedPreferences prefs;

    public static void init(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static String getUsername() {
        return prefs.getString(PREF_USERNAME, null);
    }

    public static void setUsername(String username) {
        prefs.edit().putString(PREF_USERNAME, username).apply();
    }

    public static String getPassword() {
        return prefs.getString(PREF_PASSWORD, null);
    }

    public static void setPassword(String password) {
        prefs.edit().putString(PREF_PASSWORD, password).apply();
    }

    public static String getDeviceId() {
        return prefs.getString(PREF_DEVICEID, null);
    }

    public static void setDeviceId(String deviceId) {
        prefs.edit().putString(PREF_DEVICEID, deviceId).apply();
    }

    public static String getHosturl() {
        return prefs.getString(PREF_HOSTNAME, null);
    }

    public static void setHosturl(String value) {
        prefs.edit().putString(PREF_HOSTNAME, value).apply();
    }

    public static synchronized void clear() {
        setUsername(null);
        setPassword(null);
        setDeviceId(null);
        UserPreferences.setGpodnetNotificationsEnabled();
    }
}
