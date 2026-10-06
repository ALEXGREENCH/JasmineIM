"""Compare JVM descriptors and visibility against the original, without loading classes."""
import io
import re
import struct
import zipfile
from pathlib import Path


def members(data):
    stream = io.BytesIO(data)
    def u2(): return struct.unpack(">H", stream.read(2))[0]
    def u4(): return struct.unpack(">I", stream.read(4))[0]
    assert u4() == 0xCAFEBABE
    u2(); u2()
    pool = [None] * u2()
    i = 1
    while i < len(pool):
        tag = stream.read(1)[0]
        if tag == 1:
            pool[i] = stream.read(u2()).decode("utf-8", errors="replace")
        elif tag in (3, 4): stream.read(4)
        elif tag in (5, 6): stream.read(8); i += 1
        elif tag in (7, 8, 16, 19, 20): stream.read(2)
        elif tag in (9, 10, 11, 12, 17, 18): stream.read(4)
        elif tag == 15: stream.read(3)
        else: raise ValueError(tag)
        i += 1
    u2(); u2(); u2()
    stream.read(2 * u2())
    result = {}
    for kind in ("field", "method"):
        for _ in range(u2()):
            access, name, descriptor = u2(), pool[u2()], pool[u2()]
            for _ in range(u2()):
                u2(); stream.read(u4())
            if access & 0x1000 or name == "<clinit>": continue
            # Visibility, static, final, abstract; monitor placement may change representation.
            result[(kind, name, descriptor)] = access & (1 | 2 | 4 | 8 | 16 | 1024)
    return result


def check(root):
    classes = root / "app/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes"
    errors = []
    total = 0
    with zipfile.ZipFile(root / "compatibility/fixtures/Jasmine_BLOB.jar") as jar:
        for name in jar.namelist():
            if not name.endswith(".class") or name.startswith("android/") or re.search(r"\$\d", name): continue
            total += 1
            path = classes / name
            if not path.exists(): errors.append("Missing class: " + name); continue
            old, new = members(jar.read(name)), members(path.read_bytes())
            for member, flags in old.items():
                if new.get(member) != flags:
                    errors.append(f"{name}: {member} expected flags {flags}, got {new.get(member)}")
    if errors: raise SystemExit("\n".join(errors))
    print(f"PASS: descriptors and visibility preserved for {total} named application classes")


if __name__ == "__main__":
    check(Path(__file__).resolve().parents[1])
