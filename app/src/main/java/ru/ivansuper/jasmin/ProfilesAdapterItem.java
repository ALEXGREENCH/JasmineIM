package ru.ivansuper.jasmin;

/**
 * Represents an item in the profiles adapter.
 * This class stores the configuration details for a single profile,
 * such as connection parameters and authentication settings.
 */
public class ProfilesAdapterItem {
    public int port;
    public int profile_type = -1;
    public String id = "";
    public String host = "";
    public String pass = "";
    public String server = "";
    public boolean tls = false;
    public boolean sasl = false;
    public boolean compression = false;
    public boolean autoconnect = false;
    public boolean enabled = false;
    public int proto_type = -1;
}
