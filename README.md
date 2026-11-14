# Universal Podcasts

A free podcast manager for Android: subscribe, download, and listen to your favorite shows, with offline playback, variable speed, chapters and full device sync.

## Contact

Questions or feedback? Reach us on Telegram: [@johnconstantine570](https://t.me/johnconstantine570)

## Building

Requires the Android SDK and JDK:

```
./gradlew :app:assembleDebug
```

## Sync

`.github/workflows/sync.yml` pulls upstream changes daily, re-applies the Universal Podcasts branding via `scripts/migrate.py`, and pushes.
