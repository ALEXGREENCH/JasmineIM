package ru.ivansuper.jasmin.jabber;

import java.util.Iterator;
import java.util.Vector;

public class ServerList {
   private final Vector<ServerList.Server> mList = new Vector<>();

   public final synchronized void clear() {
      this.mList.clear();
   }

   public final synchronized ServerList.Server getProxy() {
      Iterator var1 = this.mList.iterator();

      ServerList.Server var3;
      ServerList.Type var4;
      ServerList.Type var5;
      do {
         boolean var2 = var1.hasNext();
         if (!var2) {
            var3 = null;
            break;
         }

         var3 = (ServerList.Server)var1.next();
         var4 = var3.type;
         var5 = ServerList.Type.PROXY;
      } while (var4 != var5);

      return var3;
   }

   public final synchronized void put(String var1, ServerList.Type var2) {
      Vector var3 = this.mList;
      ServerList.Server var4 = new ServerList.Server(var1, var2);
      var3.add(var4);
   }

   public final synchronized void put(String var1, ServerList.Type var2, String var3, int var4) {
      Vector var5 = this.mList;
      ServerList.Server var6 = new ServerList.Server(var1, var2, var3, var4);
      var5.add(var6);
   }

   public static class Server {
      public String jid;
      public String proxy_host;
      public int proxy_port;
      public ServerList.Type type;

      public Server(String var1, ServerList.Type var2) {
         this.jid = var1;
         this.type = var2;
      }

      public Server(String var1, ServerList.Type var2, String var3, int var4) {
         this.jid = var1;
         this.type = var2;
         this.proxy_host = var3;
         this.proxy_port = var4;
      }
   }

   public enum Type {
      OTHER,
      PROXY;
   }
}
