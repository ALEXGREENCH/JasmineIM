package ru.ivansuper.jasmin.jabber.bytestreams;

import android.util.Log;
import java.util.Vector;
import ru.ivansuper.jasmin.Base64Coder;
import ru.ivansuper.jasmin.XMPPInterface;
import ru.ivansuper.jasmin.utilities;
import ru.ivansuper.jasmin.XMPPInterface.OnXMLListener;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.jabber.PacketHandler;
import ru.ivansuper.jasmin.jabber.XML_ENGINE.Node;

public class IBBController extends IOController {
   public static final String NAMESPACE = "http://jabber.org/protocol/ibb";
   private int block_size;
   private String last_id;
   private IBBController.Mode mMode;
   private int sequence = -1;
   private String sid;
   private String stanzas;
   private Vector<String> used_ids = new Vector<>();
   private OnXMLListener xmpp_listener;

   public IBBController(IBBController.Mode var1, String var2, int var3, String var4, String var5, JProfile var6) {
      this.mMode = var1;
      this.partner_jid = var2;
      this.block_size = var3;
      this.stanzas = var4;
      this.profile = var6;
      this.sid = var5;
      if (this.sid == null) {
         this.sid = "jsid_" + Math.abs(utilities.getRandom());
      }

      this.state = IOController.State.WAIT;
      this.xmpp_listener = new OnXMLListener() {
         public boolean OnXMLData(JProfile var1, Node var2x) {
            boolean var3x = true;
            String var4x = var2x.getParameter("id");
            if (IBBController.this.used_ids.indexOf(var4x) >= 0) {
               String var5x = var2x.getParameter("type");
               String var8 = var5x;
               if (var5x == null) {
                  var8 = "";
               }

               if (var8.equals("error")) {
                  IBBController.this.close();
                  Log.e(this.getClass().getSimpleName(), "Closing stream");
                  boolean var13 = var3x;
                  return var13;
               }
            }

            boolean var6x;
            if (IBBController.this.checkNode(var2x)) {
               Node var9 = var2x.findFirstLocalNodeByName("open");
               if (var9 != null) {
                  if (IBBController.this.state != IOController.State.WAIT) {
                     var6x = false;
                  } else {
                     IBBController.this.block_size = Integer.parseInt(var9.getParameter("block-size"));
                     IBBController.this.sid = var9.getParameter("sid");
                     IBBController.this.stanzas = var9.getParameter("stanza");
                     if (IBBController.this.stanzas == null) {
                        IBBController.this.stanzas = "iq";
                     }

                     if (IBBController.this.state == IOController.State.CANCELED) {
                        IBBController.this.sendCancel(var4x);
                        var6x = var3x;
                     } else {
                        IBBController.this.state = IOController.State.HANDSHAKE;
                        IBBController.this.notifyListenerState();
                        IBBController.this.sendHandshake(var4x);
                        var6x = var3x;
                     }
                  }
               } else {
                  Node var10 = var2x.findFirstLocalNodeByName("data");
                  if (var10 != null) {
                     if (!var10.getParameter("sid").equals(IBBController.this.sid)) {
                        var6x = false;
                     } else {
                        var6x = var3x;
                        if (IBBController.this.state == IOController.State.WORKING) {
                           IBBController.this.used_ids.add(var4x);
                           int var7 = Integer.parseInt(var10.getParameter("seq"));
                           if (var7 == IBBController.this.sequence + 1) {
                              IBBController.this.sequence = var7;
                              byte[] var11 = Base64Coder.decode(var10.getValue().replaceAll("\n", ""));
                              IBBController.this.notifyListenerData(var11, var11.length);
                              var6x = var3x;
                           } else {
                              IBBController.this.state = IOController.State.ERROR;
                              IBBController.this.notifyListenerState();
                              IBBController.this.sendCancel(var4x);
                              var6x = var3x;
                           }
                        }
                     }
                  } else {
                     Node var12 = var2x.findFirstLocalNodeByName("close");
                     var6x = var3x;
                     if (var12 != null) {
                        if (!var12.getParameter("sid").equals(IBBController.this.sid)) {
                           var6x = false;
                        } else {
                           IBBController.this.state = IOController.State.CLOSED;
                           XMPPInterface.removePacketsListener(IBBController.this.xmpp_listener);
                           IBBController.this.used_ids.clear();
                           IBBController.this.notifyListenerState();
                           Log.e(this.getClass().getSimpleName(), "Handled close request");
                           var6x = var3x;
                        }
                     }
                  }
               }
            } else {
               var6x = false;
            }

            return var6x;
         }
      };
      XMPPInterface.addPacketsListener(this.xmpp_listener);
   }

   private final boolean checkNode(Node var1) {
      boolean var2 = true;
      boolean var3;
      if (var1.findFirstLocalNodeByNameAndNamespace("open", "http://jabber.org/protocol/ibb") != null) {
         var3 = var2;
      } else {
         var3 = var2;
         if (var1.findFirstLocalNodeByNameAndNamespace("data", "http://jabber.org/protocol/ibb") == null) {
            var3 = var2;
            if (var1.findFirstLocalNodeByNameAndNamespace("close", "http://jabber.org/protocol/ibb") == null) {
               var3 = false;
            }
         }
      }

      return var3;
   }

   private final void sendCancel(String var1) {
      Node var2 = new Node(this.stanzas);
      var2.putParameter("to", this.partner_jid).putParameter("type", "error").putParameter("id", var1);
      Node var3 = new Node("error");
      var3.putParameter("type", "cancel");
      var2.putChild(var3.putChild(new Node("not-acceptable", "", "urn:ietf:params:xml:ns:xmpp-stanzas")));
      this.profile.stream.write(var2, this.profile);
      this.state = IOController.State.CANCELED;
      this.notifyListenerState();
   }

   private final void sendClose() {
      Node var1 = new Node(this.stanzas);
      String var2 = "ibb" + this.hashCode() + this.sequence;
      var1.putParameter("to", this.partner_jid).putParameter("type", "set").putParameter("id", var2);
      Node var3 = new Node("close", "", "http://jabber.org/protocol/ibb");
      var3.putParameter("sid", this.sid);
      var1.putChild(var3);
      this.profile.stream.write(var1, this.profile);
   }

   private final void sendHandshake(String var1) {
      Node var2 = new Node(this.stanzas);
      var2.putParameter("from", this.profile.getFullJIDWithResource())
         .putParameter("to", this.partner_jid)
         .putParameter("type", "result")
         .putParameter("id", var1);
      this.profile.stream.write(var2, this.profile);
      this.state = IOController.State.WORKING;
      this.notifyListenerState();
   }

   private final void sendInitiateRequest() {
      PacketHandler var1 = new PacketHandler(false) {
         @Override
         public void execute() {
            String var1x = this.slot.getParameter("type");
            String var2 = var1x;
            if (var1x == null) {
               var2 = "";
            }

            Log.e(this.getClass().getSimpleName(), "Result: " + var2);
            if (var2.equals("result")) {
               IBBController.this.state = IOController.State.WORKING;
            } else {
               IBBController.this.state = IOController.State.CANCELED;
            }

            IBBController.this.notifyListenerState();
         }
      };
      this.profile.putPacketHandler(var1);
      Node var2 = new Node(this.stanzas);
      var2.putParameter("type", "set").putParameter("to", this.partner_jid).putParameter("id", var1.getID());
      Node var3 = new Node("open");
      var3.putParameter("sid", this.sid).putParameter("block-size", String.valueOf(this.block_size)).putParameter("xmlns", "http://jabber.org/protocol/ibb");
      var2.putChild(var3);
      this.profile.stream.write(var2, this.profile);
   }

   @Override
   public void cancel() {
      if (this.state != IOController.State.CANCELED) {
         if (this.state != IOController.State.WAIT) {
            this.state = IOController.State.CANCELED;
            this.sendCancel(this.last_id);
         }

         XMPPInterface.removePacketsListener(this.xmpp_listener);
         this.used_ids.clear();
      }
   }

   @Override
   public void close() {
      if (this.state != IOController.State.CLOSED) {
         this.sendClose();
         this.state = IOController.State.CLOSED;
         XMPPInterface.removePacketsListener(this.xmpp_listener);
         this.used_ids.clear();
         this.notifyListenerState();
      }
   }

   @Override
   public void open() {
      if (this.mMode != IBBController.Mode.IN && this.mMode == IBBController.Mode.OUT) {
         this.sequence = 0;
         this.sendInitiateRequest();
      }
   }

   @Override
   public final String write(byte[] var1) {
      synchronized (this) {
         if (var1.length > this.block_size) {
            IllegalArgumentException var6 = new IllegalArgumentException("Block size is too big");
            throw var6;
         }

         StringBuilder var2 = new StringBuilder("ibb");
         String var3 = var2.append(this.hashCode()).append(String.valueOf(this.sequence)).toString();
         Node var7 = new Node(this.stanzas);
         var7.putParameter("to", this.partner_jid).putParameter("type", "set").putParameter("id", var3);
         Node var4 = new Node("data", Base64Coder.encodeLines(var1), "http://jabber.org/protocol/ibb");
         var4.putParameter("seq", String.valueOf(this.sequence)).putParameter("sid", this.sid);
         var7.putChild(var4);
         this.profile.stream.write(var7, this.profile);
         this.sequence++;
         if (this.sequence > 65535) {
            this.sequence = 0;
         }

         this.used_ids.add(var3);
      }

      return "";
   }

   @Override
   public final String write(byte[] var1, int var2) {
      String var4;
      synchronized (this) {
         if (var2 > this.block_size) {
            IllegalArgumentException var8 = new IllegalArgumentException("Block size is too big");
            throw var8;
         }

         StringBuilder var3 = new StringBuilder("ibb");
         var4 = var3.append(this.hashCode()).append(String.valueOf(this.sequence)).toString();
         Node var9 = new Node(this.stanzas);
         var9.putParameter("to", this.partner_jid).putParameter("type", "set").putParameter("id", var4);
         String var6 = new String(Base64Coder.encode(var1, 0, var2));
         Node var5 = new Node("data", var6, "http://jabber.org/protocol/ibb");
         var5.putParameter("seq", String.valueOf(this.sequence)).putParameter("sid", this.sid);
         var9.putChild(var5);
         this.profile.stream.write(var9, this.profile);
         this.sequence++;
         if (this.sequence > 65535) {
            this.sequence = 0;
         }

         this.used_ids.add(var4);
      }

      return var4;
   }

   public enum Mode {
      IN,
      OUT;
   }
}
