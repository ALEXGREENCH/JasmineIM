# Jasmine_BLOB recovery and compatibility checks

The application's former `app/libs/Jasmine_BLOB.jar` is replaced by 60 Java
source files under `app/src/main/java/ru/ivansuper/jasmin/jabber`. All 82 named
application classes (including named nested classes) are present. Anonymous
classes are expressed as Java anonymous classes; their compiler-assigned names
need not match. The two bundled `android.annotation` classes are supplied by
the Android SDK instead of being packaged as application classes.

`fixtures/Jasmine_BLOB.jar` is an **unchanged test oracle**, never an application
dependency. Do not replace this fixture with a jar built from the recovered
sources: that would make differential testing meaningless.

- Origin: ALEXGREENCH/JasmineIM, commit `ef08dbbdd38e94c19f203e37d0f9576a49c76285`.
- Original path: `app/libs/Jasmine_BLOB.jar`.
- SHA-256: `8be9a7514ff6a3c0c334088a3aac4ed121ff00231ffa5cad74ea0784745e3927`.
- Existing compression-table assets are used unchanged by both implementations.
- The blob recovery kept the original SDK settings. The subsequent
  [Android 1.6 compatibility change](ANDROID_1_6.md) lowers minSdk from 10 to 4;
  compileSdk 32, targetSdk 32, application ID and branding are unchanged.

## Run

With Python 3.9+ and a JDK 11+ on PATH (or `JAVA_HOME`):

```sh
python compatibility/run.py --core-only --self-test
```

This compiles the recovered core directly from source without Gradle or an
Android SDK. The reference and recovered implementations run in separate JVMs.
The source implementation's classpath contains no reference jar. The fixture's
checksum is verified before every run. JVM locale, timezone and encoding are
fixed. Pseudorandom inputs use fixed seeds. A timeout makes hangs fail the run.

For the complete application/model checks, use JDK 17+ to run Gradle and set
`ANDROID_HOME` to an SDK containing platform 32:

```sh
./gradlew :app:assembleDebug :app:assembleRelease
python compatibility/check_api.py
python compatibility/run.py --extended --self-test
```

On Windows use `gradlew.bat`. `--classes DIRECTORY` accepts an alternative
compiled debug class directory. `--baseline-only` executes just the original
fixture (add `--extended` to include application models after compiling the app).
No Python packages or test framework downloads are needed. CI runs the core on
JDKs 11 and 17 and the complete build/model checks on JDK 17.

The probes record outputs, exception types, serialized bytes and state/event
sequences. Results and stderr are written to `build/compatibility`; a mismatch
produces `difference.diff` and a nonzero exit status. `--self-test` builds a
deliberately broken XML escaper in that temporary directory and verifies that
the same suite detects it, without changing application sources.

## Coverage

The current suite records 69,974 observations (65,536 are the exhaustive Java
`char` domain for the two JID predicates; this is not a code coverage percentage).

- XML escaping, headers, parameters, invalid input and legacy parser behavior;
  node construction, lookups, mutation, serialization and incremental parser
  buffer consumption, including generated nested stanzas.
- Compression at levels 0/1/6/9, raw/wrapped streams, chunked I/O, empty/random/
  repetitive data, decompression of JDK-generated streams and invalid input.
- Packet IDs, proxy selection, DNS query encoding and response parsing.
- SOCKS5 against a local scripted server, including split responses, address
  types and rejected negotiation. No external servers are contacted.
- JID/status/date helpers, SASL PLAIN/DIGEST and deterministic SCRAM responses.
- vCard text and field decoding, telephone flag combinations, bookmark lookup
  and ordering, contact/resource ordering and state.
- History writing and last-ten-message cache bytes, Unicode reading, legacy
  reading/conversion and truncated conversion input, using disposable files.
- IBB open/data/out-of-sequence/close handling through the actual XMPP listener,
  with captured outgoing stanzas and listener state/data events.
- Form type and field parsing, and Gmail notification parsing.
- `check_api.py` compares member JVM descriptors and visibility for all named
  blob classes without loading them. Synthetic compiler helpers and the
  representation of synchronization are excluded from this structural check.

Only platform boundaries used by these probes are substituted: logging,
filesystem-backed assets, application resource globals and translation keys.
The extended probes use the real application's existing supporting classes,
SDK type definitions, and constructor-free allocation for profiles/streams to
avoid starting UI or network services. They do **not** emulate Android views,
graphics or lifecycle behavior. The captured stream substitutes transport, not
the transfer state machine.

## Recovery notes and limits

Recovery used CFR 0.152, Vineflower 1.12.0 and JADX 1.5.6 as cross-checks, plus
`javap -c -p` against the original. The original bytecode has fragmented exception
tables and expanded monitor blocks. Temporary normalization for decompiler input
and the original APK's DEX helped recover structured control flow. No normalized
jar or decompiler is used by the application or the tests. Sources were repaired
against the original behavior, not accepted solely because they compiled.

The initial characterizations were run against the original before replacing
the application dependency. Subsequent regression failures caught lost XML
closing tags, compression differences, dropped lookup returns, a resource-sort
loop, incorrect history-conversion success and transfer control-flow artifacts.

Historical quirks are intentionally preserved. For example, `getRawMD5Hash`
returns UTF-8 bytes, some namespace searches have surprising results, and some
long repetitive compression cases fail even in the original. Fix such behavior
separately with explicit new expectations; do not silently redefine the oracle.
Clock-dependent SCRAM nonces are controlled through the stored first-message
value. Time-dependent timestamp parsing is not asserted as a deterministic
return value.

Passing these tests is evidence for the covered behavior, **not proof of total
equivalence**. Android UI rendering, lifecycle, real account login, TLS, complete
file transfers, image decoding and thread scheduling still need device testing.
See [Android 1.6 compatibility](ANDROID_1_6.md) for the subsequent API 4 smoke
checks. The latest Android and full end-to-end protocol behavior remain unverified.

Before release, compare the original and recovered APKs on API 10 and a current
Android device/emulator with a controlled XMPP server: login/reconnect,
roster/presence, messages/history, forms/vCards, bookmarks and IBB/SOCKS5 file
transfers. Keep Android modernization and intentional bug fixes separate from
this recovery so behavioral changes remain reviewable.
