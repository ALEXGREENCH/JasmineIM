package ru.ivansuper.jasmin.jabber.FileTransfer;

import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Vector;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.jabber.JProtocol;
import ru.ivansuper.jasmin.jabber.XML_ENGINE.Node;
import ru.ivansuper.jasmin.jabber.bytestreams.IBBController;
import ru.ivansuper.jasmin.jabber.bytestreams.IOController;
import ru.ivansuper.jasmin.jabber.bytestreams.SOCKS5Controller;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

public class SIFileReceiver extends FileTransfer {
    private String channel_namespace;
    private String last_id;
    private OutputStream output;

    public SIFileReceiver(JProfile jProfile, Node node) {
        this.direction = 0;
        this.profile = jProfile;
        this.partner_jid = node.getParameter("from");
        this.last_id = node.getParameter("id");
        Node nodeFindFirstLocalNodeByNameAndNamespace = node.findFirstLocalNodeByNameAndNamespace("si", "http://jabber.org/protocol/si");
        this.ID = nodeFindFirstLocalNodeByNameAndNamespace.getParameter("id");
        Node nodeFindFirstLocalNodeByNameAndNamespace2 = nodeFindFirstLocalNodeByNameAndNamespace.findFirstLocalNodeByNameAndNamespace("file", "http://jabber.org/protocol/si/profile/file-transfer");
        this.file_name = nodeFindFirstLocalNodeByNameAndNamespace2.getParameter("name");
        this.size = Long.parseLong(nodeFindFirstLocalNodeByNameAndNamespace2.getParameter("size"));
        Log.e(getClass().getSimpleName(), "File: " + this.file_name + "     Size: " + this.size + " bytes");
        Node nodeFindFirstLocalNodeByName = nodeFindFirstLocalNodeByNameAndNamespace.findFirstLocalNodeByNameAndNamespace("feature", "http://jabber.org/protocol/feature-neg").findFirstLocalNodeByNameAndNamespace("x", "jabber:x:data").findFirstLocalNodeByName("field");
        int size = nodeFindFirstLocalNodeByName.childs.size();
        String[] strArr = new String[size];
        Vector<Node> vectorFindLocalNodesByName = nodeFindFirstLocalNodeByName.findLocalNodesByName("option");
        Log.e(getClass().getSimpleName(), "Channels:");
        for (int i = 0; i < size; i++) {
            strArr[i] = vectorFindLocalNodesByName.get(i).findFirstLocalNodeByName("value").getValue();
            Log.e(getClass().getSimpleName(), "-> " + strArr[i]);
        }
        checkAvailableChannels(strArr);
        this.processing_path = String.valueOf(resources.JASMINE_INCOMING_FILES_PATH) + jProfile.getFullJID() + "/from_" + JProtocol.getJIDFromFullID(this.partner_jid) + "/";
        File file = new File(this.processing_path);
        if (!file.exists()) {
            try {
                file.mkdirs();
            } catch (Exception e) {
            }
        }
        this.state = FileTransfer.State.WAIT;
    }

    private final void checkAvailableChannels(String[] strArr) {
        for (String str : strArr) {
            if (str.trim().toLowerCase().equals(SOCKS5Controller.NAMESPACE)) {
                this.io = new SOCKS5Controller(this.profile, SOCKS5Controller.Mode.IN, this.partner_jid, this.ID, false);
                this.channel_namespace = SOCKS5Controller.NAMESPACE;
                break;
            } else {
                if (str.trim().toLowerCase().equals(IBBController.NAMESPACE)) {
                    this.io = new IBBController(IBBController.Mode.IN, this.partner_jid, 2048, "iq", this.ID, this.profile);
                    this.channel_namespace = IBBController.NAMESPACE;
                    break;
                }
            }
        }
        prepareIO();
    }

    private final boolean prepareFile() {
        StringBuilder sb;
        File file = null;
        for (int i = 0; i < 9999; i++) {
            String strValueOf = String.valueOf(this.processing_path);
            if (i == 0) {
                sb = new StringBuilder(strValueOf);
                file = new File(sb.append(this.file_name.trim()).toString());
            } else {
                sb = new StringBuilder(strValueOf);
                file = new File(sb.append("(").append(i).append(")").append(this.file_name.trim()).toString());
            }
            if (!file.exists()) {
                break;
            }
        }
        this.file_name = file.getName();
        try {
            file.createNewFile();
            this.output = new FileOutputStream(file);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private final void prepareIO() {
        this.io.setEventListener(new IOController.OnEventListener() {
            int cycle = 0;
            int max = 90112;

            @Override
            public void OnData(byte[] bArr, int i) {
                if (SIFileReceiver.this.state != FileTransfer.State.TRANSFERING) {
                    return;
                }
                Log.e(getClass().getSimpleName(), "Data packet received (" + i + " bytes)");
                try {
                    SIFileReceiver.this.output.write(bArr, 0, i);
                    SIFileReceiver.this.processed += (long) i;
                    if (SIFileReceiver.this.processed >= SIFileReceiver.this.size) {
                        try {
                            SIFileReceiver.this.output.close();
                        } catch (Exception e) {
                        }
                        SIFileReceiver.this.io.close();
                        SIFileReceiver.this.state = FileTransfer.State.FINISHED;
                    }
                    this.cycle += i;
                    if (this.cycle > this.max) {
                        SIFileReceiver.this.updateDisplay();
                        this.cycle = 0;
                    }
                } catch (Exception e2) {
                    SIFileReceiver.this.stop();
                    SIFileReceiver.this.state = FileTransfer.State.ERROR;
                }
            }

            @Override
            public void onStateChanged(IOController.State state) {
                SIFileReceiver sIFileReceiver;
                FileTransfer.State state2;
                if (state != IOController.State.CANCELED && state != IOController.State.CLOSED) {
                    if (state == IOController.State.ERROR) {
                        SIFileReceiver.this.stop();
                        sIFileReceiver = SIFileReceiver.this;
                        state2 = FileTransfer.State.ERROR;
                    }
                    SIFileReceiver.this.updateDisplay();
                }
                SIFileReceiver.this.stop();
                sIFileReceiver = SIFileReceiver.this;
                state2 = FileTransfer.State.CANCELED;
                sIFileReceiver.state = state2;
                SIFileReceiver.this.updateDisplay();
            }
        });
    }

    private final void sendCancel() {
        Node node = new Node("iq");
        node.putParameter("to", this.partner_jid).putParameter("type", "error").putParameter("id", String.valueOf(utilities.getRandom()));
        Node node2 = new Node("error");
        node2.putParameter("type", "cancel");
        node.putChild(node2);
        this.profile.stream.write(node, this.profile);
    }

    private final void sendChannelConfirmation() {
        Node node = new Node("iq");
        node.putParameter("to", this.partner_jid).putParameter("type", "result").putParameter("id", this.last_id);
        Node node2 = new Node("si", "", "http://jabber.org/protocol/si");
        Node node3 = new Node("feature", "", "http://jabber.org/protocol/feature-neg");
        Node node4 = new Node("x", "", "jabber:x:data");
        node4.putParameter("type", "submit");
        Node node5 = new Node("field");
        node5.putParameter("var", "stream-method");
        node5.putChild(new Node("value", this.channel_namespace));
        node4.putChild(node5);
        node3.putChild(node4);
        node2.putChild(node3);
        node.putChild(node2);
        this.profile.stream.write(node, this.profile);
        this.state = FileTransfer.State.TRANSFERING;
    }

    @Override
    public void start() {
        if (prepareFile() && resources.sd_mounted()) {
            sendChannelConfirmation();
        } else {
            Log.e(getClass().getSimpleName(), "Failed to prepare file!");
            this.state = FileTransfer.State.ERROR;
        }
        updateDisplay();
    }

    @Override
    public void stop() {
        if (this.state == FileTransfer.State.TRANSFERING) {
            this.state = FileTransfer.State.CANCELED;
            this.io.cancel();
        }
    }
}
