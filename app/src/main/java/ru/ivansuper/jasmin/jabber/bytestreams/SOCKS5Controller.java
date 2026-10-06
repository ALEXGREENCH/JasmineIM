package ru.ivansuper.jasmin.jabber.bytestreams;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;
import ru.ivansuper.jasmin.XMPPInterface;
import ru.ivansuper.jasmin.utilities;
import ru.ivansuper.jasmin.XMPPInterface.OnXMLListener;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.jabber.JProtocol;
import ru.ivansuper.jasmin.jabber.PacketHandler;
import ru.ivansuper.jasmin.jabber.ServerList;
import ru.ivansuper.jasmin.jabber.XML_ENGINE.Node;

public class SOCKS5Controller extends IOController {
   public static final String NAMESPACE = "http://jabber.org/protocol/bytestreams";
   private InputStream input;
   private SOCKS5Controller.Mode mMode;
   private OutputStream output;
   private boolean proxied;
   private Thread reader;
   private String sid;
   private Socket socket;
   private String stanzas_id;
   private String stream_host;
   private String stream_jid;
   private int stream_port;
   private boolean streamhost_list_received = false;
   private OnXMLListener xmpp_listener;

   public SOCKS5Controller(JProfile var1, SOCKS5Controller.Mode var2, String var3, String var4, boolean var5) {
      this.profile = var1;
      this.partner_jid = var3;
      this.mMode = var2;
      this.sid = var4;
      ServerList.Server var6 = var1.server_list.getProxy();
      if (var6 != null) {
         this.stream_jid = var6.jid;
         this.stream_host = var6.proxy_host;
         this.stream_port = var6.proxy_port;
      }

      Log.e("SOCKS5Controller", "Initialized with: " + this.stream_host + ":" + this.stream_port + "     sid: " + this.sid);
      this.xmpp_listener = new OnXMLListener() {
         public boolean OnXMLData(JProfile var1, Node var2x) {
            String var4x = var2x.getParameter("id");
            var2x = var2x.findFirstLocalNodeByNameAndNamespace("query", "http://jabber.org/protocol/bytestreams");
            if (var2x != null) {
               SOCKS5Controller.this.stanzas_id = var4x;
               String var5x = var2x.getParameter("sid");
               if (var5x != null && SOCKS5Controller.this.sid.equals(var5x)) {
                  Vector<Node> var6x = var2x.findLocalNodesByName("streamhost");
                  if (var6x.size() > 0 && SOCKS5Controller.this.mMode == SOCKS5Controller.Mode.IN && !SOCKS5Controller.this.streamhost_list_received) {
                     SOCKS5Controller.this.streamhost_list_received = true;
                     Log.e("SOCKS5Controller", "Streamhost list received");

                     for (Node var9 : var6x) {
                        SOCKS5Controller.this.stream_jid = var9.getParameter("jid");
                        SOCKS5Controller.this.stream_host = var9.getParameter("host");

                        try {
                           SOCKS5Controller.this.stream_port = Integer.parseInt(var9.getParameter("port"));
                        } catch (Exception var3x) {
                        }

                        if (JProtocol.itIsServer(SOCKS5Controller.this.stream_jid)) {
                           SOCKS5Controller.this.proxied = true;
                           break;
                        }
                     }

                     Log.e(this.getClass().getSimpleName(), "connectToStream from server list");
                     SOCKS5Controller.this.connectToStream();
                  }
               }
            }

            return false;
         }
      };
      XMPPInterface.addPacketsListener(this.xmpp_listener);
      this.state = IOController.State.WAIT;
   }

   private final void closeStream() {
      try {
         this.input.close();
      } catch (Exception var4) {
      }

      try {
         this.output.close();
      } catch (Exception var3) {
      }

      try {
         this.socket.close();
      } catch (Exception var2) {
      }
   }

   private final void confirmStream(String var1) {
      Log.e("SOCKS5Controller", "Confirming ...");
      Node var2 = new Node("iq");
      var2.putParameter("to", this.partner_jid)
         .putParameter("from", this.profile.getFullJIDWithResource())
         .putParameter("type", "result")
         .putParameter("id", var1);
      Node var4 = new Node("query", "", "http://jabber.org/protocol/bytestreams");
      Node var3 = new Node("streamhost-used");
      var3.putParameter("jid", this.stream_jid);
      var4.putChild(var3);
      var2.putChild(var4);
      this.profile.stream.write(var2, this.profile);
   }

   private final void connectToStream() {
      Log.e(this.getClass().getSimpleName(), "connectToStream");
      if (this.state == IOController.State.WAIT) {
         this.state = IOController.State.HANDSHAKE;
         String var1;
         if (this.mMode == SOCKS5Controller.Mode.IN) {
            var1 = utilities.getHexSha1Hash(this.sid + this.partner_jid + this.profile.getFullJIDWithResource());
         } else {
            var1 = utilities.getHexSha1Hash(this.sid + this.profile.getFullJIDWithResource() + this.partner_jid);
         }

         Log.e("SOCKS5Controller", "RAW: " + this.stream_host + "@" + this.stream_port);

         try {
            InetAddress[] var2 = InetAddress.getAllByName(this.stream_host);
            StringBuilder var3 = new StringBuilder("NORMAL: ");
            Log.e("SOCKS5Controller", var3.append(var2[0].getHostAddress()).append("@").append(this.stream_port).toString());
            this.socket = Socks5SocketFactory.getSocket(var2[0].getHostAddress(), this.stream_port, var1, this.profile.PASS);
         } catch (UnknownHostException var4) {
            var4.printStackTrace();
         }

         try {
            this.input = this.socket.getInputStream();
            this.output = this.socket.getOutputStream();
         } catch (Exception var5) {
            var5.printStackTrace();
            this.state = IOController.State.ERROR;
            this.notifyListenerState();
            return;
         }

         if (this.socket != null && !this.socket.isClosed()) {
            this.runReader();
            Log.e("SOCKS5Controller", "Socket connected");
            if (this.mMode == SOCKS5Controller.Mode.IN) {
               this.confirmStream(this.stanzas_id);
            } else {
               this.inviteToStream();
            }

            if (this.mMode == SOCKS5Controller.Mode.IN) {
               this.state = IOController.State.WORKING;
               this.notifyListenerState();
            }
         } else {
            this.state = IOController.State.ERROR;
            this.notifyListenerState();
         }
      }
   }

   private final void inviteToStream() {
      PacketHandler var1 = new PacketHandler(false) {
         @Override
         public void execute() {
            String var1x = this.slot.getParameter("type");
            String var2 = var1x;
            if (var1x == null) {
               var2 = "";
            }

            if (var2.equals("result")) {
               PacketHandler var4 = new PacketHandler(false) {
                  @Override
                  public void execute() {
                     String var1 = this.slot.getParameter("type");
                     String var2 = var1;
                     if (var1 == null) {
                        var2 = "";
                     }

                     if (var2.equals("result")) {
                        SOCKS5Controller.this.state = IOController.State.WORKING;
                        SOCKS5Controller.this.notifyListenerState();
                     } else {
                        SOCKS5Controller.this.closeStream();
                        SOCKS5Controller.this.state = IOController.State.ERROR;
                        SOCKS5Controller.this.notifyListenerState();
                     }
                  }
               };
               SOCKS5Controller.this.profile.putPacketHandler(var4);
               Node var6 = new Node("iq");
               var6.putParameter("from", SOCKS5Controller.this.profile.getFullJIDWithResource())
                  .putParameter("to", SOCKS5Controller.this.stream_jid)
                  .putParameter("type", "set")
                  .putParameter("id", var4.getID());
               Node var5 = new Node("query", "", "http://jabber.org/protocol/bytestreams");
               var5.putParameter("sid", SOCKS5Controller.this.sid);
               Node var3 = new Node("activate");
               var3.setValue(SOCKS5Controller.this.partner_jid);
               var6.putChild(var5.putChild(var3));
               SOCKS5Controller.this.profile.stream.write(var6, SOCKS5Controller.this.profile);
            } else {
               SOCKS5Controller.this.closeStream();
               SOCKS5Controller.this.state = IOController.State.ERROR;
               SOCKS5Controller.this.notifyListenerState();
            }
         }
      };
      this.profile.putPacketHandler(var1);
      Node var2 = new Node("iq");
      var2.putParameter("from", this.profile.getFullJIDWithResource())
         .putParameter("to", this.partner_jid)
         .putParameter("type", "set")
         .putParameter("id", var1.getID());
      Node var3 = new Node("query", "", "http://jabber.org/protocol/bytestreams");
      var3.putParameter("sid", this.sid);
      Node var4 = new Node("streamhost");
      var4.putParameter("jid", this.stream_jid).putParameter("host", this.stream_host).putParameter("port", String.valueOf(this.stream_port));
      var2.putChild(var3.putChild(var4));
      this.profile.stream.write(var2, this.profile);
   }

   private final void runReader() {
      if (this.reader == null) {
         this.reader = new Thread() {
            final byte[] buffer = new byte[32768];
            int readed;

            @Override
            public void run() {
               while (true) {
                  try {
                     this.readed = SOCKS5Controller.this.input.read(this.buffer, 0, this.buffer.length);
                     if (this.readed < 0 || SOCKS5Controller.this.state != IOController.State.WORKING) {
                        SOCKS5Controller.this.cancel();
                        SOCKS5Controller.this.notifyListenerState();
                        break;
                     }

                     SOCKS5Controller.this.notifyListenerData(this.buffer, this.readed);
                  } catch (Exception var2) {
                     if (SOCKS5Controller.this.state == IOController.State.WORKING) {
                        SOCKS5Controller.this.close();
                     }
                     break;
                  }
               }
            }
         };
      }

      this.reader.start();
   }

   @Override
   public void cancel() {
      if (this.state != IOController.State.CANCELED) {
         this.closeStream();
         XMPPInterface.removePacketsListener(this.xmpp_listener);
         this.state = IOController.State.CANCELED;
         this.notifyListenerState();
      }
   }

   @Override
   public void close() {
      if (this.state != IOController.State.CLOSED) {
         this.closeStream();
         XMPPInterface.removePacketsListener(this.xmpp_listener);
         this.state = IOController.State.CLOSED;
         this.notifyListenerState();
      }
   }

   public final InputStream getInputStream() {
      return this.input;
   }

   public final OutputStream getOutputStream() {
      return this.output;
   }

   @Override
   public void open() {
      super.open();
      Log.e(this.getClass().getSimpleName(), "connectToStream from Open [" + Integer.toHexString(this.hashCode()) + "]");
      this.connectToStream();
   }

   public final void setReader(Thread var1) {
      this.reader = var1;
   }

   @Override
   public String write(byte[] var1) {
      try {
         this.output.write(var1, 0, var1.length);
      } catch (IOException var2) {
         var2.printStackTrace();
         this.close();
      }

      return null;
   }

   @Override
   public String write(byte[] var1, int var2) {
      try {
         this.output.write(var1, 0, var2);
      } catch (IOException var3) {
         var3.printStackTrace();
         this.close();
      }

      return null;
   }

   public enum Mode {
      IN,
      OUT;
   }
}
