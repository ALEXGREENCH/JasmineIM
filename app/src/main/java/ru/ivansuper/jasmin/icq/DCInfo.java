package ru.ivansuper.jasmin.icq;

/**
 * Represents information about a direct connection (DC).
 * This class stores details about the connection type, IP address, port, and other parameters.
 */
public class DCInfo {
    public static final int DC_DISABLED = 0;
    public static final int DC_HTTPS = 1;
    public static final int DC_NORMAL = 3;
    public static final int DC_SOCKS = 2;
    public static final int DC_WEB = 4;
    public int dc1;
    public int dc2;
    public int dc3;
    public int dc_type;
    public int futures;
    public byte[] ip;
    public int port;

    public final void reset() {
        this.ip = new byte[4];
        this.port = 0;
        this.futures = 0;
        this.dc_type = 0;
        this.dc1 = 0;
        this.dc2 = 0;
        this.dc3 = 0;
    }

    public final String getDCType() {
        switch (this.dc_type) {
            case DC_DISABLED:
                return "DC_DISABLED";
            case DC_HTTPS:
                return "DC_HTTPS";
            case DC_SOCKS:
                return "DC_SOCKS";
            case DC_NORMAL:
                return "DC_NORMAL";
            case DC_WEB:
                return "DC_WEB";
            default:
                return "UNKNOWN";
        }
    }
}