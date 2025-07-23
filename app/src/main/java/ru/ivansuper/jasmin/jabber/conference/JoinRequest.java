package ru.ivansuper.jasmin.jabber.conference;

/**
 * Represents a request to join a MUC (Multi-User Chat) room.
 * This class stores the necessary information for a client to attempt
 * to join a conference room on a Jabber/XMPP server.
 */
public class JoinRequest {
    public String id = "";
    public String jid = "";
    public String nick = "";
    public String pass = "";
}
