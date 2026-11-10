package ru.ivansuper.jasmin.jabber.bytestreams;

import android.util.Log;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Socks5SocketFactory {
   private static final void fill(InputStream var0, byte[] var1, int var2) throws IOException {
      int var3 = 0;

      while (var3 < var2) {
         int var4 = var0.read(var1, var3, var2 - var3);
         if (var4 < 0) {
            throw new RuntimeException("Stream is closed");
         }

         var3 += var4;
      }
   }

   public static final synchronized Socket getSocket(String host, int port, String destination, String ignored) {
      try {
         Socket socket = new Socket();
         socket.setSoTimeout(3000);
         socket.setTcpNoDelay(true);
         socket.connect(new InetSocketAddress(host, port));
         socket.setSoTimeout(0);
         DataInputStream input = new DataInputStream(socket.getInputStream());
         DataOutputStream output = new DataOutputStream(socket.getOutputStream());
         byte[] buffer = new byte[1024];
         buffer[0] = 5; buffer[1] = 1; buffer[2] = 0;
         output.write(buffer, 0, 3);
         fill(input, buffer, 2);
         if ((buffer[1] & 255) != 0) {
            try { socket.close(); } catch (Exception e) { }
            throw new Exception("Fail in SOCKS5 proxy");
         }
         Log.e("Factory", "1 -- success");
         buffer[0] = 5; buffer[1] = 1; buffer[2] = 0; buffer[3] = 3;
         byte[] address = destination.getBytes();
         buffer[4] = (byte) address.length;
         System.arraycopy(address, 0, buffer, 5, address.length);
         buffer[address.length + 5] = 0; buffer[address.length + 6] = 0;
         output.write(buffer, 0, address.length + 7);
         fill(input, buffer, 4);
         if (buffer[1] != 0) {
            try { socket.close(); } catch (Exception e) { }
            throw new Exception("Server returns " + buffer[1]);
         }
         Log.e("Factory", "2 -- success");
         switch (buffer[3] & 255) {
            case 1: fill(input, buffer, 6); break;
            case 3: fill(input, buffer, 1); fill(input, buffer, (buffer[0] & 255) + 2); break;
            case 4: fill(input, buffer, 18); break;
         }
         return socket;
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }
}
