package ru.ivansuper.jasmin.jabber.FileTransfer;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLConnection;
import ru.ivansuper.jasmin.PB;
import ru.ivansuper.jasmin.jabber.JProfile;
import ru.ivansuper.jasmin.jabber.bytestreams.IOController;
import ru.ivansuper.jasmin.locale.Locale;

public class FileTransfer {
   public static final int DIRECTION_IN = 0;
   public static final int DIRECTION_OUT = 1;
   public static int id_seq;
   protected String ID;
   private OnClickListener accept_listener = new OnClickListener() {
      public void onClick(View var1) {
         FileTransfer.this.start();
         FileTransfer.this.updateDisplay();
      }
   };
   private OnClickListener cancel_listener;
   public int direction;
   private FileTransfer.Display display;
   public String file_name;
   protected IOController io;
   private FileTransfer.OnStateListener listener;
   private OnClickListener open_listener = new OnClickListener() {
      public void onClick(View var1) {
         Uri var2 = Uri.fromFile(new File(FileTransfer.this.processing_path + FileTransfer.this.file_name));
         String var3 = URLConnection.guessContentTypeFromName(var2.toString());
         String var4 = var3;
         if (var3 == null) {
            var4 = "*/*";
         }

         try {
            Intent var6 = new Intent("android.intent.action.VIEW");
            var6.setFlags(268435456);
            var6.setDataAndType(var2, var4);
            var1.getContext().startActivity(var6);
         } catch (Exception var5) {
            var5.printStackTrace();
         }

         FileTransfer.this.updateDisplay();
      }
   };
   public String partner_jid;
   public long processed;
   public String processing_path;
   public JProfile profile;
   public long size;
   public FileTransfer.State state;

   public FileTransfer() {
      this.cancel_listener = new OnClickListener() {
         public void onClick(View var1) {
            Log.e(this.getClass().getSimpleName(), "Cancel called");
            if (FileTransfer.this.state == FileTransfer.State.TRANSFERING) {
               FileTransfer.this.io.cancel();
            }

            FileTransfer.this.state = FileTransfer.State.CANCELED;
            FileTransfer.this.updateDisplay();
         }
      };
   }

   private final void generateID() {
      synchronized (this) {
         this.ID = String.valueOf(id_seq);
         id_seq++;
      }
   }

   public static String getSizeLabel(long var0) {
      String var2 = "[]";
      double var3 = var0;
      if (var3 < 1024.0) {
         var2 = var3 + " b";
      } else if (var0 >= 1024L && var0 < 1048576L) {
         var3 = new BigDecimal(var3 / 1024.0).setScale(2, RoundingMode.UP).doubleValue();
         var2 = var3 + " KB";
      } else if (var0 >= 1048576L) {
         var3 = new BigDecimal(var3 / 1024.0 / 1024.0).setScale(2, RoundingMode.UP).doubleValue();
         var2 = var3 + " MB";
      }

      return var2;
   }

   public final void clearDisplay() {
      this.display = null;
   }

   public final int getDisplayHash() {
      int var1;
      if (this.display != null) {
         var1 = this.display.getHash();
      } else {
         var1 = 0;
      }

      return var1;
   }

   public final String getID() {
      return this.ID;
   }

   public int getPercentage() {
      return 0;
   }

   public String getStatusString() {
      String var1;
      if (this.display == null) {
         var1 = "null";
      } else {
         var1 = this.display.getLabelText();
      }

      return var1;
   }

   protected final void notifyListenerState() {
      if (this.listener != null) {
         this.listener.OnState(this.state);
      }
   }

   public void removeOnStateListener() {
      this.listener = null;
   }

   public void setDisplay(ViewGroup var1) {
      this.display = new FileTransfer.Display(var1);
      this.updateDisplayFromUI();
   }

   public void setOnStateListener(FileTransfer.OnStateListener var1) {
      this.listener = var1;
   }

   public void start() {
   }

   public void stop() {
   }

   public final void updateDisplay() {
      if (this.display != null) {
         this.display.update();
      }
   }

   public final void updateDisplayFromUI() {
      if (this.display != null) {
         this.display.updateUIThread();
      }
   }

   private class Display {
      private Button accept;
      private Button cancel;
      private View container;
      private TextView label;
      private PB progress;
      private int view_hash;

      public Display(ViewGroup var2) {
         this.container = var2;
         this.view_hash = var2.hashCode();
         this.label = (TextView)var2.findViewById(2131427416);
         this.progress = (PB)var2.findViewById(2131427417);
         this.accept = (Button)var2.findViewById(2131427418);
         this.cancel = (Button)var2.findViewById(2131427419);
         this.cancel.setText(Locale.getString("s_do_cancel"));
      }

      public final int getHash() {
         return this.view_hash;
      }

      public final String getLabelText() {
         return this.label.getText().toString();
      }

      public final void update() {
         this.container.post(new Runnable() {
            @Override
            public void run() {
               Display.this.updateUIThread();
            }
         });
      }

      public final void updateUIThread() {
         if (FileTransfer.this.state == FileTransfer.State.WAIT) {
            this.container.setVisibility(0);
            this.label.setText(FileTransfer.this.file_name.trim() + "\n" + FileTransfer.getSizeLabel(FileTransfer.this.size));
            Button var1 = this.accept;
            String var2;
            if (FileTransfer.this.direction == 0) {
               var2 = Locale.getString("s_do_receive");
            } else {
               var2 = "...";
            }

            var1.setText(var2);
            this.accept.setVisibility(0);
            this.cancel.setVisibility(0);
            this.progress.setVisibility(8);
            this.accept.setOnClickListener(FileTransfer.this.accept_listener);
            this.cancel.setOnClickListener(FileTransfer.this.cancel_listener);
         } else if (FileTransfer.this.state == FileTransfer.State.CONNECTING) {
            this.container.setVisibility(0);
            this.label.setText(Locale.getString("s_preparing"));
            this.accept.setVisibility(8);
            this.cancel.setVisibility(0);
            this.progress.setVisibility(8);
            this.cancel.setOnClickListener(FileTransfer.this.cancel_listener);
         } else if (FileTransfer.this.state == FileTransfer.State.TRANSFERING) {
            this.container.setVisibility(0);
            String var6;
            if (FileTransfer.this.direction == 0) {
               var6 = Locale.getString("s_receiving1");
            } else {
               var6 = Locale.getString("s_sending1");
            }

            var6 = var6
               + "\n"
               + FileTransfer.this.file_name.trim()
               + "\n"
               + FileTransfer.getSizeLabel(FileTransfer.this.processed)
               + "/"
               + FileTransfer.getSizeLabel(FileTransfer.this.size);
            this.label.setText(var6);
            this.progress.setMax(FileTransfer.this.size);
            this.progress.setProgress(FileTransfer.this.processed);
            this.accept.setVisibility(8);
            this.cancel.setVisibility(0);
            this.progress.setVisibility(0);
            this.cancel.setOnClickListener(FileTransfer.this.cancel_listener);
         } else if (FileTransfer.this.state == FileTransfer.State.FINISHED) {
            this.container.setVisibility(0);
            TextView var4 = this.label;
            String var8;
            if (FileTransfer.this.direction == 0) {
               var8 = Locale.getString("s_file_successful_received");
            } else {
               var8 = Locale.getString("s_file_sended_successful");
            }

            var4.setText(var8 + "\n" + FileTransfer.this.file_name.trim() + "\n" + FileTransfer.getSizeLabel(FileTransfer.this.size));
            this.accept.setText(Locale.getString("s_open"));
            Button var9 = this.accept;
            byte var3;
            if (FileTransfer.this.direction == 0) {
               var3 = 0;
            } else {
               var3 = 8;
            }

            var9.setVisibility(var3);
            this.cancel.setVisibility(8);
            this.progress.setVisibility(8);
            this.accept.setOnClickListener(FileTransfer.this.open_listener);
         } else if (FileTransfer.this.state == FileTransfer.State.CANCELED) {
            this.container.setVisibility(0);
            TextView var5 = this.label;
            String var10;
            if (FileTransfer.this.direction == 0) {
               var10 = Locale.getString("s_recv_canceled");
            } else {
               var10 = Locale.getString("s_send_canceled");
            }

            var5.setText(var10 + ":\n" + FileTransfer.this.file_name.trim() + "\n" + FileTransfer.getSizeLabel(FileTransfer.this.size));
            this.progress.setVisibility(8);
            this.accept.setVisibility(8);
            this.cancel.setVisibility(8);
            this.accept.setOnClickListener(null);
            this.cancel.setOnClickListener(null);
         } else if (FileTransfer.this.state == FileTransfer.State.ERROR) {
            this.container.setVisibility(0);
            this.label
               .setText(Locale.getString("s_error") + ":\n" + FileTransfer.this.file_name.trim() + "\n" + FileTransfer.getSizeLabel(FileTransfer.this.size));
            this.progress.setVisibility(8);
            this.accept.setVisibility(8);
            this.cancel.setVisibility(8);
            this.accept.setOnClickListener(null);
            this.cancel.setOnClickListener(null);
         } else {
            this.container.setVisibility(8);
            this.accept.setOnClickListener(null);
            this.cancel.setOnClickListener(null);
         }
      }
   }

   public interface OnStateListener {
      void OnState(FileTransfer.State var1);
   }

   public enum State {
      CANCELED,
      CONNECTING,
      ERROR,
      FINISHED,
      TRANSFERING,
      WAIT;
   }
}
