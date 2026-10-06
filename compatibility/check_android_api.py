"""Check class references against Android's API database, including SDK-guarded code.

Dalvik <= 1.6 can reject an entire class for a reference to an unavailable API.
Unlike lint, an if (SDK_INT >= ...) inside that class is not an exemption here.
Isolated nested ApiN classes (and their anonymous children) have minimum N.
Run after compiling; this supplements lint and is not a device/emulator test.
"""
import argparse
import io
import os
from pathlib import Path
import re
import struct
import subprocess
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[1]


def read_class(data):
    stream = io.BytesIO(data)
    def u2(): return struct.unpack('>H', stream.read(2))[0]
    def u4(): return struct.unpack('>I', stream.read(4))[0]
    assert u4() == 0xcafebabe
    u2(); u2()
    pool = [None] * u2()
    i = 1
    while i < len(pool):
        tag = stream.read(1)[0]
        if tag == 1: value = stream.read(u2()).decode('utf-8', errors='replace')
        elif tag in (7, 8, 16, 19, 20): value = u2()
        elif tag in (9, 10, 11, 12, 17, 18): value = (u2(), u2())
        elif tag in (3, 4): value = stream.read(4)
        elif tag in (5, 6): value = stream.read(8)
        elif tag == 15: value = stream.read(3)
        else: raise ValueError(tag)
        pool[i] = (tag, value)
        i += 2 if tag in (5, 6) else 1
    def utf(index): return pool[index][1]
    def cls(index): return utf(pool[index][1])
    u2()
    name = cls(u2())
    superclass = u2()
    parents = ([cls(superclass)] if superclass else []) + [cls(u2()) for _ in range(u2())]
    types, refs, members = set(), [], set()
    for entry in pool:
        if not entry: continue
        tag, value = entry
        if tag == 7: types.add(utf(value))
        if tag in (9, 10, 11):
            owner = cls(value[0])
            member, descriptor = pool[value[1]][1]
            descriptor = utf(descriptor)
            refs.append(('field' if tag == 9 else 'method', owner, utf(member), descriptor))
            types.update(re.findall(r'L([^;]+);', descriptor))
    for kind in ('field', 'method'):
        for _ in range(u2()):
            u2()
            member, descriptor = utf(u2()), utf(u2())
            members.add((kind, member, descriptor))
            types.update(re.findall(r'L([^;]+);', descriptor))
            for _ in range(u2()):
                u2(); stream.read(u4())
    return name, {'parents': parents, 'types': types, 'refs': refs, 'members': members}


def load_api(path):
    api = {}
    for c in ET.parse(path).getroot():
        since = int(c.get('since', '1'))
        api[c.get('name')] = {
            'since': since,
            'parents': [n.get('name') for n in c if n.tag in ('extends', 'implements')],
            'members': {(n.tag, n.get('name')): int(n.get('since', str(since)))
                        for n in c if n.tag in ('method', 'field')},
        }
    return api


def audit(classes, api):
    def member_api(kind, owner, name, desc, seen=None):
        seen = set() if seen is None else seen
        if owner in seen: return None
        seen.add(owner)
        if owner in classes:
            c = classes[owner]
            if (kind, name, desc) in c['members']: return 1
        elif owner in api:
            c = api[owner]
            version = c['members'].get((kind, name + (desc if kind == 'method' else '')))
            if version is not None: return max(c['since'], version)
        else: return None
        if name == '<init>': return None
        versions = [member_api(kind, p, name, desc, seen) for p in c['parents']]
        return min((v for v in versions if v is not None), default=None)

    errors = []
    for name, c in sorted(classes.items()):
        match = re.search(r'\$Api(\d+)(?:\$|$)', name)
        minimum = int(match.group(1)) if match else 4
        for typ in c['types']:
            typ = typ.lstrip('[').removeprefix('L').removesuffix(';') if typ.startswith('[') else typ
            # Java 8 bootstrap metadata only; D8 desugars lambdas before producing DEX.
            if typ.startswith('java/lang/invoke/'): continue
            if typ not in classes and typ in api and api[typ]['since'] > minimum:
                errors.append(f'{name}: type {typ} needs API {api[typ]["since"]}, class minimum {minimum}')
        for kind, owner, member, desc in c['refs']:
            if owner.startswith('java/lang/invoke/'): continue
            version = member_api(kind, owner, member, desc)
            if version and version > minimum:
                errors.append(f'{name}: {owner}.{member}{desc} needs API {version}, class minimum {minimum}')
    return sorted(set(errors))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--self-test', action='store_true')
    args = parser.parse_args()
    sdk = Path(os.environ.get('ANDROID_HOME', os.environ.get('ANDROID_SDK_ROOT', Path.home() / 'AppData/Local/Android/Sdk')))
    api = load_api(sdk / 'platforms/android-32/data/api-versions.xml')
    directory = ROOT / 'app/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes'
    classes = dict(read_class(p.read_bytes()) for p in directory.rglob('*.class'))
    if not classes: raise SystemExit('Compile :app:assembleDebug first')
    errors = audit(classes, api)
    if errors: raise SystemExit('\n'.join(errors))
    print(f'PASS: {len(classes)} compiled classes checked for isolated post-API-4 references')
    apk = ROOT / 'app/build/outputs/apk/debug/app-debug.apk'
    with zipfile.ZipFile(apk) as archive:
        dex = [name for name in archive.namelist() if name.endswith('.dex')]
        assert dex == ['classes.dex'], 'Donut needs a single DEX without a multidex loader'
        assert archive.read(dex[0])[:8] == b'dex\n035\0', 'Donut needs DEX version 035'
    build_tools = sdk / 'build-tools/34.0.0'
    aapt = build_tools / ('aapt.exe' if os.name == 'nt' else 'aapt')
    signer = build_tools / ('apksigner.bat' if os.name == 'nt' else 'apksigner')
    badging = subprocess.check_output([str(aapt), 'dump', 'badging', str(apk)], text=True, encoding='utf-8')
    assert "sdkVersion:'4'" in badging, 'APK must declare API 4'
    subprocess.run([str(signer), 'verify', '--min-sdk-version', '4', str(apk)], check=True)
    print('PASS: APK declares API 4, contains a single DEX 035, and its signature verifies for API 4')
    if args.self_test:
        work = ROOT / 'build/compatibility/api-audit-self-test'
        work.mkdir(parents=True, exist_ok=True)
        source = work / 'Unsafe.java'
        source.write_text('''class Unsafe {
    boolean empty(String s) { return android.os.Build.VERSION.SDK_INT >= 9 && s.isEmpty(); }
    static class Api9 { boolean empty(String s) { return s.isEmpty(); } }
}''', encoding='utf-8')
        javac = str(Path(os.environ['JAVA_HOME']) / 'bin/javac') if 'JAVA_HOME' in os.environ else 'javac'
        subprocess.run([javac, '--release', '8', '-cp', str(sdk / 'platforms/android-32/android.jar'), str(source)], check=True)
        fixtures = dict(read_class(p.read_bytes()) for p in work.glob('*.class'))
        failures = audit(fixtures, api)
        assert len(failures) == 1 and failures[0].startswith('Unsafe: java/lang/String.isEmpty'), failures
        print('PASS: SDK-guarded unsafe reference detected; isolated API-9 class accepted')


if __name__ == '__main__':
    main()
