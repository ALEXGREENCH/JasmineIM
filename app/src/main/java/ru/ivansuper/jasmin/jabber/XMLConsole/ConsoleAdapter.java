package ru.ivansuper.jasmin.jabber.XMLConsole;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.Vector;
import ru.ivansuper.jasmin.resources;

public class ConsoleAdapter extends BaseAdapter {
   private Vector<Stanzas> list;

   public ConsoleAdapter(Vector<Stanzas> var1) {
      this.list = var1;
      this.notifyDataSetChanged();
   }

   public int getCount() {
      return this.list.size();
   }

   public Stanzas getItem(int var1) {
      return this.list.get(var1);
   }

   public long getItemId(int var1) {
      return var1;
   }

   public View getView(int var1, View var2, ViewGroup var3) {
      TextView var4;
      if (var2 == null) {
         var4 = new TextView(resources.ctx);
         var4.setTextColor(-16777216);
         var4.setTextSize(12.0F);
      } else {
         var4 = (TextView)var2;
      }

      Stanzas var5 = this.getItem(var1);
      if (var5.direction == 0) {
         var4.setBackgroundColor(-4063295);
      } else if (var5.direction == 1) {
         var4.setBackgroundColor(-3135);
      }

      var4.setText(var5.xml);
      return var4;
   }
}
