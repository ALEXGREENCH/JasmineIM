package ru.ivansuper.jasmin.jabber.FileTransfer;

import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;
import ru.ivansuper.jasmin.jabber.JContact;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.jabber.PacketHandler;
import ru.ivansuper.jasmin.jabber.XML_ENGINE.Node;
import ru.ivansuper.jasmin.jabber.bytestreams.IBBController;
import ru.ivansuper.jasmin.jabber.bytestreams.IOController;
import ru.ivansuper.jasmin.jabber.bytestreams.SOCKS5Controller;

public class SIFileSender extends FileTransfer {
   private int block_size;
   private InputStream input;
   private long interval;
   private Thread sender;
   private String si_id;

   public SIFileSender(JProfile var1, JContact var2, String var3, File var4) {
      this.direction = 1;
      this.profile = var1;
      this.partner_jid = var2.ID + "/" + var3;
      this.file_name = var4.getName();
      this.processing_path = utilities.normalizePath(var4.getAbsolutePath());
      this.size = var4.length();
      this.block_size = 1024;
      this.si_id = "si-" + Long.toHexString(System.currentTimeMillis());
      this.state = FileTransfer.State.WAIT;
      if (!this.prepareFile(var4)) {
         this.state = FileTransfer.State.ERROR;
      }
   }

   private final boolean prepareFile(File var1) {
      boolean var2 = false;
      boolean var3;
      if (!var1.exists()) {
         var3 = var2;
      } else {
         var3 = var2;
         if (resources.sd_mounted()) {
            try {
               FileInputStream var4 = new FileInputStream(var1);
               this.input = var4;
               Thread var6 = new Thread() {
                  private byte[] buffer;
                  int cycle;
                  int max;
                  private int readed = 0;

                  {
                     this.cycle = 0;
                     this.max = 32768;
                  }


                  @Override
                  public void run() {
                     this.buffer = new byte[SIFileSender.this.block_size];

                     while (true) {
                        try {
                           if (SIFileSender.this.processed >= SIFileSender.this.size || SIFileSender.this.state != FileTransfer.State.TRANSFERING) {
                              return;
                           }
                        } catch (Exception var8) {
                           var8.printStackTrace();
                           if (SIFileSender.this.state != FileTransfer.State.TRANSFERING) {
                              return;
                           }
                           break;
                        }

                        try {
                           this.readed = SIFileSender.this.input.read(this.buffer, 0, this.buffer.length);
                           SIFileSender.this.io.write(this.buffer, this.readed);
                           SIFileSender var1x = SIFileSender.this;
                           var1x.processed = var1x.processed + this.readed;
                           if (SIFileSender.this.processed >= SIFileSender.this.size) {
                              SIFileSender.this.state = FileTransfer.State.FINISHED;
                              SIFileSender.this.updateDisplay();
                              return;
                           }
                        } catch (Exception var9) {
                           var9.printStackTrace();
                           if (SIFileSender.this.state != FileTransfer.State.TRANSFERING) {
                              return;
                           }
                           break;
                        }

                        long var2x;
                        try {
                           var2x = SIFileSender.this.interval;
                        } catch (Exception var7) {
                           var7.printStackTrace();
                           if (SIFileSender.this.state != FileTransfer.State.TRANSFERING) {
                              return;
                           }
                           break;
                        }

                        if (var2x > 0L) {
                           try {
                              Thread.sleep(SIFileSender.this.interval);
                           } catch (Exception var4x) {
                           }
                        }

                        try {
                           this.cycle = this.cycle + this.readed;
                           if (this.cycle > this.max) {
                              SIFileSender.this.updateDisplay();
                              this.cycle = 0;
                           }
                        } catch (Exception var6x) {
                           var6x.printStackTrace();
                           if (SIFileSender.this.state != FileTransfer.State.TRANSFERING) {
                              return;
                           }
                           break;
                        }

                        try {
                           SIFileSender.this.updateDisplay();
                        } catch (Exception var5) {
                           var5.printStackTrace();
                           if (SIFileSender.this.state != FileTransfer.State.TRANSFERING) {
                              return;
                           }
                           break;
                        }
                     }

                     SIFileSender.this.stop();
                     SIFileSender.this.state = FileTransfer.State.ERROR;
                  }
               };
               this.sender = var6;
               Log.e(this.getClass().getSimpleName(), "File prepared");
            } catch (Exception var5) {
               var5.printStackTrace();
               var3 = var2;
               return var3;
            }

            var3 = true;
         }
      }

      return var3;
   }

   private final void prepareIO() {
      this.io.setEventListener(new IOController.OnEventListener() {
         @Override
         public void OnData(byte[] var1, int var2) {
         }

         @Override
         public void onStateChanged(IOController.State var1) {
            if ((var1 == IOController.State.CANCELED || var1 == IOController.State.CLOSED) && SIFileSender.this.state != FileTransfer.State.FINISHED) {
               SIFileSender.this.stop();
            } else if (var1 == IOController.State.ERROR && SIFileSender.this.state != FileTransfer.State.FINISHED) {
               SIFileSender.this.stop();
               SIFileSender.this.state = FileTransfer.State.ERROR;
            } else if (var1 == IOController.State.WORKING) {
               Log.e(this.getClass().getSimpleName(), "Starting transfer");
               SIFileSender.this.state = FileTransfer.State.TRANSFERING;
               SIFileSender.this.runTransfer();
            }

            SIFileSender.this.notifyListenerState();
            SIFileSender.this.updateDisplay();
         }
      });
      Log.e(this.getClass().getSimpleName(), "Transfer prepared. Waiting");
   }

   private final void runTransfer() {
      this.sender.start();
   }

   private final void sendCancel() {
      Node var1 = new Node("iq");
      var1.putParameter("to", this.partner_jid).putParameter("type", "error").putParameter("id", String.valueOf(utilities.getRandom()));
      Node var2 = new Node("error");
      var2.putParameter("type", "cancel");
      var1.putChild(var2);
      this.profile.stream.write(var1, this.profile);
   }

   private final void sendHandshake() {
      PacketHandler var1 = new PacketHandler(false) {
         @Override
         public void execute() {
            String var1x = this.slot.getParameter("type");
            String var2 = var1x;
            if (var1x == null) {
               var2 = "";
            }

            if (var2.equals("result")) {
               Node var3 = this.slot.findFirstNodeByName("value");
               Log.e("ClientChoise", var3.getValue());
               if (var3.getValue().equals("http://jabber.org/protocol/bytestreams")) {
                  SIFileSender.this.block_size = 16384;
                  SIFileSender.this.interval = 0L;
                  SIFileSender.this.io = new SOCKS5Controller(
                     SIFileSender.this.profile, SOCKS5Controller.Mode.OUT, SIFileSender.this.partner_jid, SIFileSender.this.si_id, false
                  );
               } else {
                  if (!var3.getValue().equals("http://jabber.org/protocol/ibb")) {
                     SIFileSender.this.state = FileTransfer.State.ERROR;
                     SIFileSender.this.updateDisplay();
                     return;
                  }

                  SIFileSender.this.block_size = 1024;
                  SIFileSender.this.interval = 1000L;
                  SIFileSender.this.io = new IBBController(
                     IBBController.Mode.OUT, SIFileSender.this.partner_jid, 2048, "iq", SIFileSender.this.si_id, SIFileSender.this.profile
                  );
               }

               Log.e(this.getClass().getSimpleName(), "Opening channel");
               SIFileSender.this.prepareIO();
               SIFileSender.this.io.open();
            } else {
               SIFileSender.this.state = FileTransfer.State.ERROR;
            }

            SIFileSender.this.updateDisplay();
         }
      };
      this.profile.putPacketHandler(var1);
      Node var2 = new Node("iq");
      var2.putParameter("to", this.partner_jid).putParameter("type", "set").putParameter("id", var1.getID());
      Node var9 = new Node("si", "", "http://jabber.org/protocol/si");
      var9.putParameter("id", this.si_id)
         .putParameter("mime-type", "binary/octet-stream")
         .putParameter("profile", "http://jabber.org/protocol/si/profile/file-transfer");
      Node var3 = new Node("file", "", "http://jabber.org/protocol/si/profile/file-transfer");
      var3.putParameter("name", this.file_name).putParameter("size", String.valueOf(this.size));
      Node var4 = new Node("feature", "", "http://jabber.org/protocol/feature-neg");
      Node var5 = new Node("x", "", "jabber:x:data");
      var5.putParameter("type", "form");
      Node var6 = new Node("field");
      var6.putParameter("var", "stream-method").putParameter("type", "list-single");
      if (this.profile.server_list.getProxy() != null) {
         Node var7 = new Node("option");
         Node var8 = new Node("value");
         var8.setValue("http://jabber.org/protocol/bytestreams");
         var6.putChild(var7.putChild(var8));
      }

      Node var10 = new Node("option");
      Node var11 = new Node("value");
      var11.setValue("http://jabber.org/protocol/ibb");
      var6.putChild(var10.putChild(var11));
      var4.putChild(var5.putChild(var6));
      var9.putChild(var3, var4);
      var2.putChild(var9);
      this.profile.stream.write(var2, this.profile);
   }

   @Override
   public void start() {
      if (this.state == FileTransfer.State.WAIT) {
         this.state = FileTransfer.State.CONNECTING;
         this.sendHandshake();
         this.updateDisplay();
         Log.e(this.getClass().getSimpleName(), "Sending handshake");
      }
   }

   @Override
   public void stop() {
      if ((this.state != FileTransfer.State.FINISHED || this.state != FileTransfer.State.ERROR) && this.state != FileTransfer.State.CANCELED) {
         Log.e("FileSender", "Stopping");
         this.state = FileTransfer.State.CANCELED;
         this.io.cancel();

         try {
            this.input.close();
         } catch (Exception var2) {
         }

         this.updateDisplay();
      }
   }
}
