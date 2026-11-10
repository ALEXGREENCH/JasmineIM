"""Run deterministic behavior probes in isolated JVMs. Python 3 + JDK 11 or newer."""
import argparse
import difflib
import hashlib
import os
from pathlib import Path
import shutil
import subprocess
import sys

sys.stdout.reconfigure(encoding="utf-8", errors="backslashreplace")

ROOT = Path(__file__).resolve().parents[1]
HERE = ROOT / "compatibility"
BUILD = ROOT / "build/compatibility"
ORACLE = HERE / "fixtures/Jasmine_BLOB.jar"
SHA256 = "8be9a7514ff6a3c0c334088a3aac4ed121ff00231ffa5cad74ea0784745e3927"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--baseline-only", action="store_true")
    parser.add_argument("--core-only", action="store_true", help="Compile the pure Java core without Android SDK")
    parser.add_argument("--extended", action="store_true", help="Also test application models; requires compiled app and Android SDK")
    parser.add_argument("--self-test", action="store_true", help="Verify that an intentional XML regression is detected")
    parser.add_argument("--classes", type=Path, default=ROOT / "app/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes")
    args = parser.parse_args()
    if args.extended and args.core_only:
        parser.error("--extended requires the full application classes")
    assert hashlib.sha256(ORACLE.read_bytes()).hexdigest() == SHA256, "Original JAR checksum mismatch"
    java_home = os.environ.get("JAVA_HOME")
    java = str(Path(java_home) / "bin/java") if java_home else shutil.which("java")
    javac = str(Path(java_home) / "bin/javac") if java_home else shutil.which("javac")
    BUILD.mkdir(parents=True, exist_ok=True)
    runner = BUILD / "runner"
    runner.mkdir(exist_ok=True)
    sources = list((HERE / "stubs").rglob("*.java")) + [HERE / "BehaviorProbe.java"]
    support = []
    if args.extended:
        sdk = Path(os.environ.get("ANDROID_HOME", os.environ.get("ANDROID_SDK_ROOT", Path.home() / "AppData/Local/Android/Sdk")))
        support = [args.classes, sdk / "platforms/android-32/android.jar", sdk / "platforms/android-32/optional/org.apache.http.legacy.jar"]
        sources.append(HERE / "ExtendedProbe.java")
    subprocess.run([javac, "-encoding", "UTF-8", "--release", "8", "-cp", os.pathsep.join(map(str, [ORACLE] + support)), "-d", str(runner), *map(str, sources)], check=True)
    def run(name, path, fallback=None):
        output = BUILD / (name + ".txt")
        data = BUILD / (name + "-data")
        assert data.resolve().parent == BUILD.resolve(), "Test data must stay under the build directory"
        if data.exists():
            shutil.rmtree(data)
        with output.open("w", encoding="utf-8") as stdout, (BUILD / (name + ".stderr")).open("w", encoding="utf-8") as stderr:
            for probe in (["BehaviorProbe", "ExtendedProbe"] if args.extended else ["BehaviorProbe"]):
                subprocess.run([java, "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8", "-Duser.language=en", "-Duser.country=US", "-Duser.timezone=UTC", "-Dprobe.data=" + str(data), "-Dassets=" + str(ROOT / "app/src/main/assets"), "-cp", os.pathsep.join(map(str, [runner, path] + ([fallback] if fallback else []) + support)), probe], stdout=stdout, stderr=stderr, check=True, timeout=30)
        return output.read_text(encoding="utf-8").splitlines()
    original = run("original", ORACLE)
    print("Original observations:", sum(int(line.split("=")[1]) for line in original if line.startswith("CHECKS=")))
    if args.baseline_only:
        return
    if args.core_only:
        args.classes = BUILD / "core"
        args.classes.mkdir(exist_ok=True)
        package = ROOT / "app/src/main/java/ru/ivansuper/jasmin/jabber"
        core = list((package / "XML_ENGINE").glob("*.java")) + list((package / "jzlib").glob("*.java"))
        core += [package / (name + ".java") for name in ["xml_utils", "xmpp_utils", "Parser", "XMLPacket", "PacketHandler", "ServerList", "dns/DnsSrvResolver", "bytestreams/Socks5SocketFactory"]]
        subprocess.run([javac, "-encoding", "UTF-8", "--release", "8", "-cp", str(runner), "-d", str(args.classes), *map(str, core)], check=True)
    if not args.classes.is_dir():
        raise SystemExit("Compile :app:assembleDebug first, or pass --classes DIRECTORY")
    recovered = run("recovered", args.classes)
    if original != recovered:
        diff = list(difflib.unified_diff(original, recovered, fromfile="original", tofile="recovered"))
        (BUILD / "difference.diff").write_text("\n".join(diff), encoding="utf-8")
        print("\n".join(line[:300] for line in diff[:80]))
        raise SystemExit("Behavior changed; full diff in build/compatibility/difference.diff")
    print("PASS: all observations match the original JAR")
    if args.self_test:
        mutant = BUILD / "mutant"
        mutant.mkdir(exist_ok=True)
        source = ROOT / "app/src/main/java/ru/ivansuper/jasmin/jabber/xml_utils.java"
        text = source.read_text(encoding="utf-8")
        changed = text.replace('"&amp;"', '"&broken;"')
        assert changed != text, "Update the mutation after refactoring XML escaping"
        mutation = mutant / "xml_utils.java"
        mutation.write_text(changed, encoding="utf-8")
        subprocess.run([javac, "-encoding", "UTF-8", "--release", "8", "-d", str(mutant), str(mutation)], check=True)
        if original == run("mutated", mutant, args.classes):
            raise SystemExit("Regression suite failed to detect the deliberately broken XML escaping")
        print("PASS: intentional XML regression detected")


if __name__ == "__main__":
    main()
