package ru.ivansuper.jasmin.irc;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;

import ru.ivansuper.jasmin.protocols.IMProfile;

/**
 * Minimal stub profile for IRC protocol support.
 */
public class IRCProfile extends IMProfile {

    public String server;
    public int port = 6667;
    /** Preferred character set for communication */
    public String charset = "UTF-8";
    /** Username for USER command */
    public String username = "android";
    /** Real name for USER command */
    public String realName = "JasmineIRC";

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private Thread listenThread;

    public IRCProfile(String server, int port, String nickname) {
        this.server = server;
        this.port = port;
        this.nickname = nickname;
        this.profile_type = IRC;
    }

    @Override
    public void startConnecting() {
        if (connecting || connected) return;
        Log.d("IRCProfile", "startConnecting to " + server + ":" + port);
        connecting = true;
        listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                runConnection();
            }
        }, "irc-connect-" + ID);
        listenThread.start();
    }


    private void runConnection() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(server, port), 15000);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName(charset)));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName(charset)));

            sendRaw("NICK " + nickname);
            sendRaw("USER " + username + " 0 * :" + realName);

            connected = true;
            connecting = false;

            String line;
            while ((line = reader.readLine()) != null) {
                handleServerLine(line);
            }
        } catch (IOException e) {
            Log.e("IRCProfile", "Connection error", e);
        } finally {
            connected = false;
            connecting = false;
            closeResources();
        }
    }

    @Override
    public void disconnect() {
        Log.d("IRCProfile", "disconnect");
        connected = false;
        connecting = false;
        closeResources();
    }

    /** Close socket and streams safely */
    private void closeResources() {
        try {
            if (reader != null) reader.close();
        } catch (IOException ignored) {}
        try {
            if (writer != null) writer.close();
        } catch (IOException ignored) {}
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
        reader = null;
        writer = null;
        socket = null;
    }

    @Override
    public void closeAllChats() {
        // Chats are not implemented yet
    }

    /** Send a raw IRC command */
    public synchronized void sendRaw(String line) throws IOException {
        if (writer != null) {
            writer.write(line + "\r\n");
            writer.flush();
            Log.d("IRCProfile", ">> " + line);
        }
    }

    /** Join an IRC channel */
    public void joinChannel(String channel) {
        try {
            sendRaw("JOIN " + channel);
        } catch (IOException e) {
            Log.e("IRCProfile", "JOIN failed", e);
        }
    }

    /** Send a message to a target */
    public void sendMessage(String target, String message) {
        try {
            sendRaw("PRIVMSG " + target + " :" + message);
        } catch (IOException e) {
            Log.e("IRCProfile", "sendMessage failed", e);
        }
    }

    @Override
    public void handleScreenTurnedOn() {
        // no-op for now
    }

    @Override
    public void handleScreenTurnedOff() {
        // no-op for now
    }

    /** Handle a single line from server */
    private void handleServerLine(String line) {
        Log.d("IRCProfile", "<< " + line);
        if (line.startsWith("PING")) {
            String ping = line.substring(5);
            try {
                sendRaw("PONG " + ping);
            } catch (IOException e) {
                Log.e("IRCProfile", "PONG failed", e);
            }
        }
    }

    @Override
    public String getStatusText() {
        return "";
    }

    @Override
    public void setStatusText(String str) {
        // IRC has no status text
    }

    /**
     * Update profile parameters from adapter data
     */
    public void reinitParams(ru.ivansuper.jasmin.ProfilesAdapterItem pdata) {
        this.server = pdata.server;
        this.port = pdata.port;
        this.nickname = pdata.id;
        this.autoconnect = pdata.autoconnect;
        this.enabled = pdata.enabled;
        if (!this.enabled && this.connected) {
            disconnect();
        }
    }
}
