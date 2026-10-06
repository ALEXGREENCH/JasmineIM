# Android 1.6 (API 4)

The APK now declares `minSdk 4`; compile/target SDK remain 32. This is a backward
compatibility change, not a completed modernization of the application.

## Compatibility changes

- Post-Donut framework calls live in separate `ApiN` classes, entered only on
  supported versions. An SDK check inside the original class is insufficient
  for the strict verifier shipped with Android 1.6.
- API 4 services use `setForeground(boolean)` and NotificationManager, and
  receive start intents via `onStart`. API 5+ keeps start/stopForeground.
- Permissions, notifications/channels, clipboard, vibration, window insets,
  DNS network enumeration and SNI keep newer API references out of base classes.
- String empty checks and encoding use API-4-compatible methods. Preferences
  use commit on API 4–8 and retain apply on API 9+.
- Before getExternalFilesDir (API 8), mounted external storage uses the same
  Android/data/<package>/files directory shape. Internal files remain the
  fallback when external storage is absent or unavailable.
- The base theme uses Theme.Black.NoTitleBar; API 5+ retains the wallpaper
  theme. Custom Back handling also works with API 4's key callback.
- The custom contact list uses a Canvas scrollbar fallback on API 4: Donut
  crashes when a programmatically enabled native scrollbar has no initialized
  drawable. The fallback follows the same scroll metrics, uses the theme accent,
  scales with density and fades after scrolling. API 5+ retains the native path.
- Section titles use filled, pixel-aligned text and a small dark offset shadow,
  replacing the bright blurred shadow and thick colored outline. All tab
  highlights draw below text. Titles respect font scaling and ellipsize within
  their slot; an empty pager also renders safely. These helpers use API 4
  primitives and add no AndroidX/AppCompat dependency.

## Resource packaging

Modern AAPT2 emits UTF-8 string pools that Donut cannot read. Installation alone
does not catch this: the application failed while loading its first drawable.
`gradle/legacy-resources.gradle` converts pools in linked binary XML and ARSC to
UTF-16 **before** APK packaging, alignment and signing. String indices, resource
IDs, XML nodes and style spans stay unchanged. Release optimization can recreate
UTF-8 pools, so its output is converted too.

The transform preserves each ZIP entry's storage method and stores
`resources.arsc` without compression. `androidResources.noCompress` also pins
`arsc` in the final APK: with this old minSdk, the packager otherwise compresses
the table again. For target SDK 30+, Android 11 and newer require the table to
be uncompressed and aligned to four bytes. A previous build violated this rule
while still installing on Donut and passing semantic resource comparisons.
`check_resources.py` now checks the final ZIP entry and its actual local-header
offset for both variants; mutation checks reject compressed and misaligned
tables. UTF-16 conversion, minimum SDK and signing identity are unchanged.

Reference: [Android 11 resource packaging requirements](https://developer.android.com/about/versions/11/behavior-changes-11#resources-arsc).

The packaging fix passed clean Debug/Release builds, both final-APK resource
checks, signature verification for API 4 and API 30–36, and another installation/
startup on the Donut emulator. The previous APK fails the new compression check.
The debug signing certificate is unchanged. A read-only API 34 emulator launch
was attempted, but the emulator refused to start due to insufficient host disk
space; installation on Android 16 has not been verified locally. Play Protect's
separate warning/reputation decision is not tested by these packaging checks.

This integration uses AGP 8.7.3 internal artifacts. Recheck it when upgrading AGP;
the resource comparison and final APK checks must continue to pass. It requires
no old SDK tool or Python installation to build the application.

## Repeatable checks

With JDK 17+, Python 3.9+, Android platform 32 and build-tools 34.0.0:

```sh
./gradlew :app:assembleDebug :app:assembleRelease :app:lintDebug -PlegacyApiCheck
python compatibility/check_android_api.py --self-test
python compatibility/check_resources.py
python compatibility/check_api.py
python compatibility/run.py --extended --self-test
python compatibility/check_ui.py
```

On Windows use `gradlew.bat` and set JAVA_HOME/ANDROID_HOME as needed.
`legacyApiCheck` restricts this lint invocation to NewApi. Ordinary lint still
reports the existing unrelated resource-type, constant and layout findings;
these have not been hidden in a baseline.

The API audit examines compiled classes against the SDK API database, including
references behind SDK guards. Its mutation check rejects a guarded String.isEmpty
call and accepts an isolated Api9 helper. It also checks the final Debug APK's
minSdk, single DEX version 035 and signature verification for API 4.
The independent resource check compares all 93 binary resources in each APK
with the pre-conversion AAPT2 output, including strings, IDs, XML and styles.
The original-JAR differential tests remain unchanged.

## Device validation and limits

Local validation uses the official Google Android 1.6 r03 Windows platform image
and SDK tools r10 emulator, in an isolated directory under ignored `build/android4`.
System properties confirm Android 1.6 / API 4. No user device or existing AVD is
used. The device has no accounts or external network test server configured.

On 2026-10-06 the Debug APK installed successfully and completed first launch:
the contact list and initial profile prompt rendered, the profiles screen and
add-profile menu opened, and Back returned to the contact list. The service was
reported as `foregroundServices=true`. These application paths produced no
uncaught application exception or verifier error after the fixes. Logs and
framebuffer captures are under local `build/android4` (not committed).

The Debug/Release build, NewApi lint, 990-class reference audit,
both 93-resource comparisons, 82-class signature check and all 69,974 original-JAR
observations passed. CI includes these build/static/differential checks; it does
not download or run the historical emulator.

## Section titles and scroll indicator

The supplied `jasmine_5.5.2_without_popups.apk` was read without modification.
All three section-label entries match its RU/EN/UA locale assets, including
their assignment to chats, contacts and conferences. The visual regression was
in rendering: a bright shadow and four-pixel outline blurred the letters; setting
the outline color reset its intended alpha, and neighboring highlights could
overpaint already drawn text.

`compatibility/check_ui.py` runs 1,172 JVM checks for thumb sizing, endpoints,
clamping, monotonic position, overflow-safe arithmetic and fading. CI runs it
on JDK 11 and 17.

The debug variant contains `UiRenderProbe`, a framework instrumentation with
no AndroidX dependency. On a disposable emulator with the Debug APK installed:

```sh
adb shell am instrument -w ru.ivansuper.jasmin/ru.ivansuper.jasmin.compat.UiRenderProbe
adb pull /data/data/ru.ivansuper.jasmin/files/ui-probes ./ui-probes
```

Pulling this private directory requires a root-capable emulator, as used here.
The instrumentation creates offline views in its own process; it does not need
accounts or a server. It changes in-memory display metrics for software Canvas
renders at 240x320/ldpi, 320x480/mdpi, 480x800/hdpi and 240x320 with 150% font
scale. These are four rendering scenarios on one Android 1.6 system image,
not four separately configured devices. Each scenario renders all three pages,
checks that the actual contact-list fallback indicator appears, then draws an
empty pager. It is excluded from release builds.

On 2026-10-06 all scenarios passed on API 4. Inspection of the saved PNGs
confirmed clear small-screen headings, ellipsis for long large-font headings,
and a visible scrollbar beside the contact rows. Local before/after images are
in ignored `build/ui-before` and `build/ui-after`. This is a software-rendering
check; modern Android hardware rendering still needs device validation.

## Page animation smoke checks

`PageAnimationProbe` is another debug-only framework instrumentation. Run it on
the disposable emulator with the Debug APK installed:

```sh
adb shell am instrument -w ru.ivansuper.jasmin/ru.ivansuper.jasmin.compat.PageAnimationProbe
adb pull /data/data/ru.ivansuper.jasmin/files/animation-probes ./animation-probes
```

It renders 165 frames covering all 11 effects: cube, flip 1/2, classic shift,
rotation 1/2/3, ICS, snake, rotation 4 and ICS 2. Each effect is sampled at five
positions for an ordinary transition and wrapping across either end. Endpoint
pixels must show the correct page. The probe also checks actual Scroller
completion, left/right keys, long and short swipes, cancellation, locked/empty/
single-page states, invalid selection, parent layout offsets, and a seeded
random picker that must reach all 11 effects.

The initial run reproduced double transformation on software Canvas, missing
wrapped pages, a cancelled swipe changing selection, and rejected drag events.
The pager now applies each transformation once in the child's coordinates,
consumes its drag events, recycles synthetic cancellation events and snaps back
on cancellation. The random picker previously selected only effects 0 through 7;
it now covers 0 through 10. These are UI fixes outside the recovered JAR.

On 2026-10-06 the corrected build passed all 486 animation/navigation assertions
on API 4; the 165 saved frames were inspected as contact sheets. The four title/
scrollbar render scenarios also passed again. First launch, the profiles screen,
the add-profile menu and Back navigation were exercised without application
exception or verifier error in logcat. Local results are in ignored
`build/animation-after`. Debug/Release builds, NewApi lint and the resource/API
audits passed; both instrumentation classes are absent from the release APK.

This intentionally remains a small offline smoke suite. It does not validate
GPU rendering, animation frame rate, live account login, reconnects or a real
server's end-to-end message/file delivery. The separate 69,974-observation
original-JAR suite covers XML, compression, authentication calculations,
history, roster/form/vCard parsing and scripted local transfer interactions.

These are still smoke checks, not full acceptance testing. Real account login,
server TLS compatibility, message delivery, transfers, lifecycle stress and
modern Android device behavior need additional validation. Android 1.6's TLS
provider cannot gain modern protocol/cipher support just from lowering minSdk;
certificate verification has not been weakened for this change.

Reference: [AOSP Dalvik verifier notes](https://android.googlesource.com/platform/dalvik/+/refs/heads/main/docs/verifier.html)
and [Donut resource parser](https://android.googlesource.com/platform/frameworks/base/+/android-1.6_r2/libs/utils/ResourceTypes.cpp).
