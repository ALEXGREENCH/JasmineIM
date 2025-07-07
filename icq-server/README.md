# ICQ Test Server

This module provides a minimal TCP server that mimics a tiny subset of the ICQ protocol.
It is intended for local testing of client connectivity.

The server listens on port **5190** by default and follows a tiny subset of the
OSCAR protocol. Upon connection the client sends the FLAP version (`0x00000001`)
which the server echoes back. Login then happens through a `CLI_IDENT` packet on
channel `0x01` containing TLVs. Type `0x01` carries the screen name and type
`0x02` contains the password encoded with the standard "roasting" algorithm from
the OSCAR documentation. The server decodes it and registers unknown users
automatically.

After successful login the server responds on channel `0x04` with a
`SRV_COOKIE` TLV bundle that includes a dummy BOS address and cookie. For
testing we keep using the same connection as the BOS server.

Immediately after the cookie the client receives its stored contact list on
channel `0x03`. The payload looks like `LIST:name1,name2`. Contacts can be
managed with `ADD_CONTACT:<name>` and `REMOVE_CONTACT:<name>` messages on the
same channel. Searching for users is done with `SEARCH:<query>` which returns
`RESULTS:comma,separated,names`.

Clients announce presence on login and logout with `ADD:<name>` and
`REMOVE:<name>` broadcasts. Status changes are sent as `STATUS:<user>:<text>`
and extended statuses with `XSTATUS:<user>:<text>`.

Messages are exchanged on channel `0x02` and are broadcast in plain text to all
other connected clients.

The implementation lives in `main.go` and can be built with `go build`.

```bash
cd icq-server
go build
./icqserver
```

This is **not** a full implementation of the ICQ OSCAR protocol. It only
serves as a simple local testing tool and a foundation for future features
(like expanding to a mesh network).
