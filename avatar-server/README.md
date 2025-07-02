# Avatar Server

This is a small Go server used for testing avatar uploads and downloads.
It exposes two HTTP endpoints:

- `POST /upload?uin=<uin>&check=<xor>` — upload an avatar. The `check` value
  must equal the XOR of all characters in the UIN. The request body should be
  the raw image data (PNG or JPEG). Images larger than 1024×1024 pixels are
  resized to fit within that limit.
- `GET /avatar/<uin>?hq=1` — download the original avatar. Without `hq=1` a
  64×64 thumbnail is returned.

Uploaded images are stored in the `data` directory next to the server binary as
`<uin>.png`.

## Building

```
go build
```

## Running

Run the server on port 80 (requires root privileges or port forwarding):

```
./avatarserver
```

The Android client expects it to be available at `http://45.144.154.209`.
