"""Run scrollbar behavior checks without an Android SDK or external libraries."""
import os
from pathlib import Path
import shutil
import subprocess

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "build/compatibility/ui"


def java_tool(name):
    if os.environ.get("JAVA_HOME"):
        candidate = Path(os.environ["JAVA_HOME"]) / "bin" / (name + (".exe" if os.name == "nt" else ""))
        if candidate.is_file():
            return str(candidate)
    found = shutil.which(name)
    if not found:
        raise SystemExit(f"Missing {name}; install a JDK or set JAVA_HOME")
    return found


if __name__ == "__main__":
    OUTPUT.mkdir(parents=True, exist_ok=True)
    subprocess.run([java_tool("javac"), "-encoding", "UTF-8", "-d", str(OUTPUT),
                    str(ROOT / "app/src/main/java/ru/ivansuper/jasmin/compat/ScrollIndicatorGeometry.java"),
                    str(ROOT / "compatibility/ui/ScrollIndicatorTest.java")], check=True)
    subprocess.run([java_tool("java"), "-cp", str(OUTPUT), "ScrollIndicatorTest"], check=True)
