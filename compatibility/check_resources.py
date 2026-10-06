"""Independently compare decoded AAPT2 resources with the UTF-16 APK resources."""
from pathlib import Path
import struct
import tempfile
import zipfile

ROOT = Path(__file__).resolve().parents[1]


def check_packaging(apk):
    with zipfile.ZipFile(apk) as archive:
        entry = archive.getinfo('resources.arsc')
        assert entry.compress_type == zipfile.ZIP_STORED, 'resources.arsc must be uncompressed for Android 11+ (target >= 30)'
        with open(apk, 'rb') as stream:
            stream.seek(entry.header_offset)
            header = stream.read(30)
        assert header[:4] == b'PK\x03\x04', 'Invalid resource ZIP local header'
        name_length, extra_length = struct.unpack_from('<HH', header, 26)
        data_offset = entry.header_offset + 30 + name_length + extra_length
        assert data_offset % 4 == 0, 'resources.arsc must be 4-byte aligned for Android 11+'


def packaging_self_test():
    with tempfile.TemporaryDirectory(prefix='jasmine-apk-packaging-') as directory:
        for name, method, extra, error in (
            ('valid', zipfile.ZIP_STORED, b'', None),
            ('compressed', zipfile.ZIP_DEFLATED, b'', 'uncompressed'),
            ('unaligned', zipfile.ZIP_STORED, struct.pack('<HHB', 0xcafe, 1, 0), 'aligned'),
        ):
            path = Path(directory) / (name + '.apk')
            entry = zipfile.ZipInfo('resources.arsc')
            entry.compress_type, entry.extra = method, extra
            with zipfile.ZipFile(path, 'w') as archive:
                archive.writestr(entry, b'packaging fixture')
            try:
                check_packaging(path)
            except AssertionError as failure:
                assert error and error in str(failure), (name, failure)
            else:
                assert error is None, f'Packaging check missed {name} resources'
    print('PASS: packaging guard accepts stored/aligned ARSC and rejects compressed/unaligned mutations')


def canonical(data, require_utf16=False):
    kind, header, size = struct.unpack_from('<HHI', data)
    assert size == len(data) and 8 <= header <= size
    u32 = lambda offset: struct.unpack_from('<I', data, offset)[0]
    if kind == 1:
        count, styles, flags, start, style_start = struct.unpack_from('<IIIII', data, 8)
        utf8 = bool(flags & 0x100)
        assert not (require_utf16 and utf8), 'UTF-8 resource pool cannot be read on Android 1.6'
        strings = []
        for index in range(count):
            offset = start + u32(header + index * 4)
            if utf8:
                chars = data[offset]; offset += 1
                if chars & 128:
                    chars = ((chars & 127) << 8) | data[offset]; offset += 1
                length = data[offset]; offset += 1
                if length & 128:
                    length = ((length & 127) << 8) | data[offset]; offset += 1
                assert data[offset + length] == 0
                text = data[offset:offset + length].decode('utf-8')
                assert len(text.encode('utf-16le')) // 2 == chars
            else:
                length = struct.unpack_from('<H', data, offset)[0]; offset += 2
                if length & 0x8000:
                    length = ((length & 0x7fff) << 16) | struct.unpack_from('<H', data, offset)[0]; offset += 2
                assert data[offset + length * 2:offset + length * 2 + 2] == b'\0\0'
                text = data[offset:offset + length * 2].decode('utf-16le')
            strings.append(text)
        style_offsets = data[header + count * 4:header + (count + styles) * 4]
        return kind, flags & ~0x100, strings, style_offsets, data[style_start:] if style_start else b''
    if kind in (2, 3, 0x200):
        prefix = bytearray(data[:header])
        prefix[4:8] = b'\0' * 4
        if kind == 0x200:
            for field in (268, 276):
                offset = u32(field)
                assert struct.unpack_from('<H', data, offset)[0] == 1
                prefix[field:field + 4] = b'\0' * 4
        children = []
        pos = header
        while pos < size:
            length = u32(pos + 4)
            assert length >= 8 and pos + length <= size
            children.append(canonical(data[pos:pos + length], require_utf16))
            pos += length
        return bytes(prefix), children
    return data


def check(variant):
    if variant == 'debug':
        directory = ROOT / 'app/build/intermediates/linked_resources_binary_format/debug/processDebugResources'
        apk = ROOT / 'app/build/outputs/apk/debug/app-debug.apk'
    else:
        directory = ROOT / 'app/build/intermediates/optimized_processed_res/release/optimizeReleaseResources'
        apk = ROOT / 'app/build/outputs/apk/release/app-release-unsigned.apk'
    candidates = list(directory.glob('*.ap_'))
    assert len(candidates) == 1, f'Build {variant} first'
    check_packaging(apk)
    count = 0
    with zipfile.ZipFile(candidates[0]) as original, zipfile.ZipFile(apk) as converted:
        # AAPT2 shortens release resource paths (res/raw/foo.ogg -> res/XX.ogg).
        sounds = [entry for entry in converted.infolist() if entry.filename.startswith('res/') and entry.filename.endswith('.ogg')]
        assert len(sounds) == 9, 'Nine built-in event sounds must be packaged'
        assert all(entry.compress_type == zipfile.ZIP_STORED for entry in sounds), 'Built-in sounds must be uncompressed for openRawResourceFd'
        for name in original.namelist():
            data = original.read(name)
            if name == 'resources.arsc' or name.endswith('.xml'):
                if data[:2] in (b'\x02\0', b'\x03\0'):
                    assert canonical(data) == canonical(converted.read(name), True), name
                    count += 1
                    continue
            assert data == converted.read(name), name
    print(f'PASS: {variant}: ARSC stored/aligned; 9 sounds uncompressed; {count} binary resources use UTF-16 with identical strings, IDs, XML and styles')


if __name__ == '__main__':
    packaging_self_test()
    check('debug')
    check('release')
