#!/bin/bash

################### Create emulator ###################
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk
echo no | $ANDROID_HOME/tools/bin/avdmanager create avd --force --name "UniversalPodcastScreenshots" --abi google_apis/x86_64 --package 'system-images;android-30;google_apis;x86_64'
echo "
disk.dataPartition.size=6G
hw.battery=yes
hw.cpu.ncore=4
hw.lcd.density=420
hw.lcd.width=1080
hw.lcd.height=1920
hw.ramSize=1536
showDeviceFrame=no
" >> $HOME/.android/avd/UniversalPodcastScreenshots.avd/config.ini
nohup $ANDROID_HOME/emulator/emulator -avd UniversalPodcastScreenshots -no-snapshot &
while [ "$(adb shell getprop sys.boot_completed)" != "1" ]
do
    echo "Waiting for emulator"
    sleep 3
done
sleep 10

################### Create screenshots ###################
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
adb root

adb uninstall com.universalpodcast.app.debug
./gradlew :app:installPlayDebug
adb shell am start -n "com.universalpodcast.app.debug/com.universalpodcast.app.activity.MainActivity"
sleep 1
adb shell am force-stop com.universalpodcast.app.debug
version=$(adb shell dumpsys package com.universalpodcast.app.debug | grep versionName | cut -d'=' -f2)
versionMajor=0$(echo $version | cut -d'.' -f1)
versionMinor=0$(echo $version | cut -d'.' -f2)

wget "https://github.com/yashhasan570/UniversalPodcasts/Branding/raw/master/Screenshots/ScreenshotsDatabaseExport.db" -O ScreenshotsDatabaseExport.db

function resetDatabase() {
    theme=$1
    adb shell am force-stop com.universalpodcast.app.debug
    adb shell rm /data/data/com.universalpodcast.app.debug/databases/UniversalPodcast.db-journal
    adb push ScreenshotsDatabaseExport.db /data/data/com.universalpodcast.app.debug/databases/UniversalPodcast.db
    adb shell chmod 777 /data/data/com.universalpodcast.app.debug/databases
    adb shell chmod 777 /data/data/com.universalpodcast.app.debug/databases/UniversalPodcast.db
    echo "<?xml version='1.0' encoding='utf-8' standalone='yes' ?><map><boolean name='prefMainActivityIsFirstLaunch' value='false' /></map>" > tmp
    adb push tmp /data/data/com.universalpodcast.app.debug/shared_prefs/MainActivityPrefs.xml
    echo "<?xml version='1.0' encoding='utf-8' standalone='yes' ?><map>
        <string name='prefTheme'>$theme</string>
        <long name='com.universalpodcast.app.preferences.currentlyPlayingMedia' value='1' />
        <long name='FeedMedia.PrefMediaId' value='2432' />
        </map>" > tmp
    adb push tmp /data/data/com.universalpodcast.app.debug/shared_prefs/com.universalpodcast.app.debug_preferences.xml
    rm tmp
    sleep 1
}

function screenshot() {
    filename=$1
    sleep 6
    adb exec-out screencap -p > $filename
}

function switchLanguage() {
    language=$1
    adb shell "setprop persist.sys.locale $language; setprop ctl.restart zygote"
    sleep 10
    adb shell settings put global sysui_demo_allowed 1
    adb shell am broadcast -a com.android.systemui.demo -e command enter
    adb shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm $versionMajor$versionMinor
    adb shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false
    adb shell am broadcast -a com.android.systemui.demo -e command network -e wifi show --es fully true -e level 4
    adb shell am broadcast -a com.android.systemui.demo -e command network -e mobile show -e datatype lte -e level 4
    adb shell am broadcast -a com.android.systemui.demo -e command battery -e level 100 -e plugged false
    sleep 2
}

function createScreenshots() {
    language=$1
    mkdir -p "screenshots/$language"
    switchLanguage $language

    resetDatabase 0
    adb shell am start -n "com.universalpodcast.app.debug/com.universalpodcast.app.activity.MainActivity" --es "fragment_tag" "SubscriptionFragment"
    screenshot "screenshots/$language/00.png"

    resetDatabase 0
    adb shell am start -n "com.universalpodcast.app.debug/com.universalpodcast.app.activity.MainActivity" --es "fragment_tag" "QueueFragment"
    sleep 1
    adb shell am start -n "com.universalpodcast.app.debug/com.universalpodcast.app.activity.MainActivity" --ez "open_player" "true"
    screenshot "screenshots/$language/01.png"

    resetDatabase 0
    adb shell am start -n "com.universalpodcast.app.debug/com.universalpodcast.app.activity.MainActivity" --es "fragment_tag" "QueueFragment"
    sleep 1
    adb shell am start -n "com.universalpodcast.app.debug/com.universalpodcast.app.activity.MainActivity" --ez "open_drawer" "true"
    screenshot "screenshots/$language/02.png"

    resetDatabase 0
    adb shell am start -n "com.universalpodcast.app.debug/com.universalpodcast.app.activity.MainActivity" --es "fragment_tag" "EpisodesFragment"
    screenshot "screenshots/$language/03a.png"

    resetDatabase 1
    adb shell am start -n "com.universalpodcast.app.debug/com.universalpodcast.app.activity.MainActivity" --es "fragment_tag" "EpisodesFragment"
    screenshot "screenshots/$language/03b.png"

    resetDatabase 0
    adb shell am start -n "com.universalpodcast.app.debug/com.universalpodcast.app.activity.MainActivity" --es "fragment_tag" "QueueFragment"
    screenshot "screenshots/$language/04.png"
}

createScreenshots "en-US"
createScreenshots "de-DE"
createScreenshots "es-ES"
createScreenshots "fr-FR"
createScreenshots "he-IL"
createScreenshots "it-IT"
createScreenshots "nl-NL"

switchLanguage "en-US"
adb shell settings put global sysui_demo_allowed 0

################### Delete emulator ###################
adb devices | grep emulator | cut -f1 | while read line; do adb -s $line emu kill && sleep 10; done
$ANDROID_HOME/tools/bin/avdmanager delete avd -n "UniversalPodcastScreenshots"

