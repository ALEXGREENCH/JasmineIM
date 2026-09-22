# Jasmine IM end-to-end message encryption (ICQ)

Jasmine IM can encrypt the text of an ICQ conversation **end-to-end**, so an OSCAR server that
speaks plain TCP (no SSL) relays only ciphertext and never sees the plaintext. It is **opt-in
and per contact**: you and the other person set the same shared passphrase for that chat, and
from then on outgoing text is encrypted on the device and incoming text is decrypted on the
device.

The scheme is the one used by **SimpleOKM** (Android) and **Komet**, so the three are
wire-compatible: a message encrypted by one decrypts on the others given the same passphrase.

```
passphrase ──Argon2id──▶ 32-byte key
plaintext  ──ChaCha20-Poly1305(key, nonce, aad=[0x53,0x02])──▶ ciphertext+tag
blob       =  nonce(12) ‖ ciphertext ‖ tag(16)
on the wire=  blob, Base32-packed, each 5-bit group mapped to a Russian letter,
              grouped into decorative "words":   шгхв сфущлж збшыкь …
```

- **KDF:** Argon2id v0x13, 64 MiB, 3 iterations, 1 lane, salt = `SHA-256("komet-enc-v1" ‖ passphrase)[:16]`
- **Cipher:** ChaCha20-Poly1305 (IETF, 96-bit random nonce, 128-bit tag); the 2-byte scheme
  constant is bound as associated data and never sent, so the wire form has no fixed prefix
- **Transport encoding:** RFC 4648 Base32 without padding, re-alphabeted to the 32 Russian
  lowercase letters (ё removed); whitespace carries no information and is ignored on decode

Because there is no marker, "is this one of ours?" *is* the decryption attempt: a valid tag
means yes, anything else is shown as ordinary text.

## Using it

1. Open an ICQ chat → menu → **Encryption: off**.
2. Enter the shared passphrase (there is a **Show passphrase** toggle) → **Enable**. The key
   derivation runs in the background ("Deriving encryption key…"); the header shows
   ⌛🔒 until it is ready, then 🔒.
3. Have the other person set the **same** passphrase for that chat.
4. Send messages as usual. Your own bubble shows the plaintext; each encrypted message carries a
   🔒 next to its time. To change or drop the passphrase: menu → **Encryption: on**.

The app never sends plaintext while encryption is on: if the key is not ready yet the send is
refused (text stays in the input box) until the derivation finishes.

## Notes and limitations

- The passphrase is stored in plaintext in the app's private `icq_e2e` SharedPreferences, so
  the key can be re-derived after a restart. Not a hardened secret store.
- No forward secrecy, no ratchet: one passphrase → one key for the whole conversation.
- The salt is passphrase-derived, not random; pick a strong, unique passphrase.
- Metadata (sender, recipient, timing, rough size, the fact that it is encrypted) is visible to
  the server. Only the text is protected. The login itself is still whatever OSCAR does.
- Encrypted messages are split into 256-character parts (ciphertext is ~2x longer and travels
  as UCS-2), instead of the usual 1024.
- Local history stores the **plaintext** (as before), with a flag bit so the 🔒 survives a restart.
  Messages received before a passphrase was set stay as ciphertext in history.
- File transfers are not affected (the server does not support them anyway).

## Where it lives in the code

| File | Role |
|------|------|
| `security/Blake2b.java`, `security/Argon2.java`, `security/ChaCha20Poly1305.java` | Pure-Java primitives (verified against the RFC 7693 / 9106 / 8439 test vectors). No BouncyCastle: its `org.bouncycastle` package collides with the platform copy on old Android. |
| `security/E2ECrypto.java` | The scheme: `deriveKey`, `encrypt`, `tryDecrypt`, `looksEncrypted`, Russian-letter Base32. Port of SimpleOKM's `OkCrypto.cs`. |
| `security/ChatCrypto.java` | Per-contact passphrase storage, in-memory key cache, serialized background derivation, `encryptOutgoing` / `decryptIncoming`. |
| `icq/ICQProfile.java` | Hooks: `sendMessage` encrypts the wire copy; `handleMessage` decrypts before previews, notifications and history see the text. |
| `chats/ICQChatActivity.java` | Menu entry, passphrase dialog, header badge, send guard, key pre-derivation on chat open. |
| `HistoryItem.encrypted`, `icq/ICQContact.java` | Flag persisted in the previously reserved int of the UNI16 history record. |

---

# Jabber transport TLS (STARTTLS / port 5223)

The XMPP socket layer used to ship only as compiled classes in `app/libs/Jasmine_BLOB.jar` and
could not reach servers that require TLS 1.2/1.3: it took the platform's default protocol set
(TLS 1.0 only before Android 5), sent no usable SNI, never verified the certificate name, and
its direct-TLS path opened an unconnected SSL socket. `XMLStream` was removed from the jar and
re-created in source ([jabber/XMLStream.java](app/src/main/java/ru/ivansuper/jasmin/jabber/XMLStream.java),
same API) with the TLS work in [security/TlsSupport.java](app/src/main/java/ru/ivansuper/jasmin/security/TlsSupport.java):

- every TLS version the device supports is enabled (TLS 1.2 on Android 4.1+, TLS 1.3 on 10+;
  Android ≤ 4.0 has no TLS 1.2 in the platform at all - that needs a bundled stack such as Conscrypt);
- SNI carries the XMPP domain; the handshake completes eagerly, so failures are reported at once;
- the chain is checked against the system store and, failing that, the roots in
  `assets/tls_roots.pem` (ISRG Root X1/X2 for Let's Encrypt, absent before Android 7.1.1);
- the certificate name is verified (SAN dNSName, one-label wildcards) against the XMPP domain,
  or the server host the user typed explicitly. The "check TLS certificate" preference turns
  both checks off, as before.

Verified from the desktop (same `TlsSupport` code, Android stubs) against jabber.org,
conversations.im, xmpp.is (TLS 1.3) and jabber.ru (TLS 1.2), STARTTLS and direct 5223, plus a
name-mismatch rejection.
