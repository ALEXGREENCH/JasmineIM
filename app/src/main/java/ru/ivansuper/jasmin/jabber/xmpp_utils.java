package ru.ivansuper.jasmin.jabber;

public class xmpp_utils {
    public static final boolean isJIDSymbolAllowed(char c) {
        return (c == ' ' || c == '\"' || c == '\'' || c == '/' || c == ':' || c == '<' || c == '>' || c == '@' || c == '\\') ? false : true;
    }

    public static final boolean isJIDSymbolAllowedWODog(char c) {
        return (c == ' ' || c == '\"' || c == '\'' || c == '/' || c == ':' || c == '<' || c == '>' || c == '\\') ? false : true;
    }
}
