package ru.ivansuper.jasmin.jabber.vcard;

import java.util.Iterator;
import java.util.Vector;
import ru.ivansuper.jasmin.jabber.XML_ENGINE.Node;
import ru.ivansuper.jasmin.jabber.vcard.VCard.Entry.Type;
import ru.ivansuper.jasmin.locale.Locale;

public class VCardDecoder {
   public static synchronized String decode(Node var0) {
      StringBuilder var1 = new StringBuilder();
      String var9;
      if (!var0.hasChilds()) {
         var9 = var1.toString();
      } else {
         read(var0, "JABBERID", var1, "JID");
         read(var0, "NICKNAME", var1, Locale.getString("s_jabber_vcard_nick"));
         var0.findFirstLocalNodeByName("FN");
         read(var0, "FN", var1, Locale.getString("s_jabber_vcard_fullname"));
         Node var2 = var0.findFirstLocalNodeByName("N");
         if (var2 != null) {
            read(var2, "FAMILY", var1, Locale.getString("s_jabber_vcard_family"));
            read(var2, "GIVEN", var1, Locale.getString("s_jabber_vcard_name"));
            read(var2, "MIDDLE", var1, Locale.getString("s_jabber_vcard_surname"));
            if (var2.hasChilds()) {
               var1.append("\n");
            }
         }

         read(var0, "BDAY", var1, Locale.getString("s_jabber_vcard_birthday"));
         Node var3 = var0.findFirstLocalNodeByName("ORG");
         if (var3 != null) {
            StringBuilder var4 = new StringBuilder();
            StringBuilder var10 = new StringBuilder();
            if (var3.hasChilds()) {
               var4.append("\n");
               var4.append(Locale.getString("s_jabber_vcard_org"));
               var4.append(":\n");
            }

            readSimple(var3, "ORGNAME", var10, true);
            readSimple(var3, "ORGUNIT", var10, true);
            var4.append(var10);
            var4.append("\n");
            if (var10.toString().trim().length() > 0) {
               var1.append(var4);
            }
         }

         read(var0, "TITLE", var1, Locale.getString("s_jabber_vcard_title"));
         read(var0, "ROLE", var1, Locale.getString("s_jabber_vcard_role"));
         Vector<Node> var19 = var0.findLocalNodesByName("TEL");
         if (var19.size() > 0) {
            StringBuilder var26 = new StringBuilder();
            StringBuilder var5 = new StringBuilder();
            StringBuilder var6 = new StringBuilder();
            StringBuilder var11 = new StringBuilder("\n");
            var26.append(var11.append(Locale.getString("s_jabber_vcard_telephones")).append(":\n").toString());

            for (Node var8 : var19) {
               var6.setLength(0);
               String var12 = "";
               if (var8.findFirstLocalNodeByName("WORK") != null) {
                  StringBuilder var13 = new StringBuilder(String.valueOf(""));
                  var12 = var13.append("(").append(Locale.getString("s_jabber_vcard_work")).append(")").toString();
               } else if (var8.findFirstLocalNodeByName("HOME") != null) {
                  StringBuilder var14 = new StringBuilder(String.valueOf(""));
                  var12 = var14.append("(").append(Locale.getString("s_jabber_vcard_home")).append(")").toString();
               }

               String var21;
               if (var8.findFirstLocalNodeByName("VOICE") != null) {
                  StringBuilder var20 = new StringBuilder(String.valueOf(var12));
                  var21 = var20.append("(").append(Locale.getString("s_jabber_vcard_telephone_voice")).append(")").toString();
               } else if (var8.findFirstLocalNodeByName("FAX") != null) {
                  StringBuilder var22 = new StringBuilder(String.valueOf(var12));
                  var21 = var22.append("(").append(Locale.getString("s_jabber_vcard_telephone_fax")).append(")").toString();
               } else {
                  var21 = var12;
                  if (var8.findFirstLocalNodeByName("MSG") != null) {
                     StringBuilder var23 = new StringBuilder(String.valueOf(var12));
                     var21 = var23.append("(").append(Locale.getString("s_jabber_vcard_telephone_msg")).append(")").toString();
                  }
               }

               readSimple(var8, "NUMBER", var6, true);
               String var15 = var6.toString();
               if (var15.length() > 0) {
                  var5.append(var21);
                  if (var21.length() > 0) {
                     var5.append(":\n");
                  }

                  var5.append(var15);
               }
            }

            String var16 = var5.toString();
            if (var16.trim().length() > 0) {
               var26.append(var16);
               var1.append(var26);
            }
         }

         Vector<Node> var27 = var0.findLocalNodesByName("ADR");
         if (var27.size() > 0) {
            StringBuilder var24 = new StringBuilder();
            StringBuilder var17 = new StringBuilder();

            for (Node var33 : var27) {
               var24.setLength(0);
               var17.setLength(0);
               if (var33.findFirstLocalNodeByName("WORK") != null) {
                  StringBuilder var31 = new StringBuilder("--(");
                  var24.append(var31.append(Locale.getString("s_jabber_vcard_work")).append(")--\n").toString());
               } else if (var33.findFirstLocalNodeByName("HOME") != null) {
                  StringBuilder var32 = new StringBuilder("--(");
                  var24.append(var32.append(Locale.getString("s_jabber_vcard_home")).append(")--\n").toString());
               } else {
                  var24.append("----------\n");
               }

               readSimple(var33, "EXTADD", var17, true);
               readSimple(var33, "STREET", var17, true);
               readSimple(var33, "LOCALITY", var17, true);
               readSimple(var33, "REGION", var17, true);
               readSimple(var33, "PCODE", var17, true);
               readSimple(var33, "CTRY", var17, true);
               var24.append(var17);
               var24.append("----------\n");
               if (var17.toString().trim().length() > 0) {
                  var1.append(var24);
               }
            }
         }

         Vector<Node> var34 = var0.findLocalNodesByName("EMAIL");
         if (var34.size() > 0) {
            StringBuilder var18 = new StringBuilder();
            StringBuilder var25 = new StringBuilder();
            StringBuilder var29 = new StringBuilder("\n");
            var18.append(var29.append(Locale.getString("s_jabber_vcard_emails")).append(":\n").toString());
            Iterator var30 = var34.iterator();

            while (var30.hasNext()) {
               readSimple((Node)var30.next(), "USERID", var25, true);
            }

            var18.append(var25);
            var18.append("\n");
            if (var25.toString().trim().length() > 0) {
               var1.append(var18);
            }
         }

         read(var0, "TZ", var1, Locale.getString("s_jabber_vcard_tz"));
         read(var0, "URL", var1, Locale.getString("s_jabber_vcard_webpage"));
         var1.append("\n");
         read(var0, "DESC", var1, Locale.getString("s_jabber_vcard_description"));
         var9 = var1.toString().trim();
      }

      return var9;
   }

   public static synchronized String decode(Node var0, VCard var1) {
      String var10;
      if (var1 == null) {
         var10 = "";
      } else {
         StringBuilder var2 = new StringBuilder();
         if (!var0.hasChilds()) {
            var10 = var2.toString();
         } else {
            var1.putEntry(Type.NICKNAME, read(var0, "NICKNAME", var2, Locale.getString("s_jabber_vcard_nick")));
            var0.findFirstLocalNodeByName("FN");
            var1.putEntry(Type.FN, read(var0, "FN", var2, Locale.getString("s_jabber_vcard_fullname")));
            Node var3 = var0.findFirstLocalNodeByName("N");
            if (var3 != null) {
               var1.putEntry(Type.N_FAMILY, read(var3, "FAMILY", var2, Locale.getString("s_jabber_vcard_family")));
               var1.putEntry(Type.N_GIVEN, read(var3, "GIVEN", var2, Locale.getString("s_jabber_vcard_name")));
               var1.putEntry(Type.N_MIDDLE, read(var3, "MIDDLE", var2, Locale.getString("s_jabber_vcard_surname")));
               if (var3.hasChilds()) {
                  var2.append("\n");
               }
            }

            var1.putEntry(Type.BDAY, read(var0, "BDAY", var2, Locale.getString("s_jabber_vcard_birthday")));
            Node var4 = var0.findFirstLocalNodeByName("ORG");
            if (var4 != null) {
               StringBuilder var11 = new StringBuilder();
               StringBuilder var5 = new StringBuilder();
               if (var4.hasChilds()) {
                  var11.append("\n");
                  var11.append(Locale.getString("s_jabber_vcard_org"));
                  var11.append(":\n");
               }

               var1.putEntry(Type.ORGNAME, readSimple(var4, "ORGNAME", var5, true));
               var1.putEntry(Type.ORGUNIT, readSimple(var4, "ORGUNIT", var5, true));
               var11.append(var5);
               var11.append("\n");
               if (var5.toString().trim().length() > 0) {
                  var2.append(var11);
               }
            }

            var1.putEntry(Type.TITLE, read(var0, "TITLE", var2, Locale.getString("s_jabber_vcard_title")));
            var1.putEntry(Type.ROLE, read(var0, "ROLE", var2, Locale.getString("s_jabber_vcard_role")));
            Vector<Node> var30 = var0.findLocalNodesByName("TEL");
            if (var30.size() > 0) {
               StringBuilder var25 = new StringBuilder();
               StringBuilder var6 = new StringBuilder();
               StringBuilder var7 = new StringBuilder();
               StringBuilder var12 = new StringBuilder("\n");
               var25.append(var12.append(Locale.getString("s_jabber_vcard_telephones")).append(":\n").toString());

               for (Node var9 : var30) {
                  String var13 = "";
                  if (var9.findFirstLocalNodeByName("WORK") != null) {
                     StringBuilder var14 = new StringBuilder(String.valueOf(""));
                     String var31 = var14.append("(").append(Locale.getString("s_jabber_vcard_work")).append(")").toString();
                     if (var9.findFirstLocalNodeByName("VOICE") != null) {
                        StringBuilder var15 = new StringBuilder(String.valueOf(var31));
                        var13 = var15.append("(").append(Locale.getString("s_jabber_vcard_telephone_voice")).append(")").toString();
                        var1.putEntry(Type.TEL_W_PHONE, readSimple(var9, "NUMBER", var7, true));
                     } else if (var9.findFirstLocalNodeByName("FAX") != null) {
                        StringBuilder var16 = new StringBuilder(String.valueOf(var31));
                        var13 = var16.append("(").append(Locale.getString("s_jabber_vcard_telephone_fax")).append(")").toString();
                        var1.putEntry(Type.TEL_W_FAX, readSimple(var9, "NUMBER", var7, true));
                     } else {
                        var13 = var31;
                        if (var9.findFirstLocalNodeByName("MSG") != null) {
                           StringBuilder var17 = new StringBuilder(String.valueOf(var31));
                           var13 = var17.append("(").append(Locale.getString("s_jabber_vcard_telephone_msg")).append(")").toString();
                           var1.putEntry(Type.TEL_W_MSG, readSimple(var9, "NUMBER", var7, true));
                        }
                     }
                  } else if (var9.findFirstLocalNodeByName("HOME") != null) {
                     StringBuilder var18 = new StringBuilder(String.valueOf(""));
                     String var32 = var18.append("(").append(Locale.getString("s_jabber_vcard_home")).append(")").toString();
                     if (var9.findFirstLocalNodeByName("VOICE") != null) {
                        StringBuilder var19 = new StringBuilder(String.valueOf(var32));
                        var13 = var19.append("(").append(Locale.getString("s_jabber_vcard_telephone_voice")).append(")").toString();
                        var1.putEntry(Type.TEL_H_PHONE, readSimple(var9, "NUMBER", var7, true));
                     } else if (var9.findFirstLocalNodeByName("FAX") != null) {
                        StringBuilder var20 = new StringBuilder(String.valueOf(var32));
                        var13 = var20.append("(").append(Locale.getString("s_jabber_vcard_telephone_fax")).append(")").toString();
                        var1.putEntry(Type.TEL_H_FAX, readSimple(var9, "NUMBER", var7, true));
                     } else {
                        var13 = var32;
                        if (var9.findFirstLocalNodeByName("MSG") != null) {
                           StringBuilder var21 = new StringBuilder(String.valueOf(var32));
                           var13 = var21.append("(").append(Locale.getString("s_jabber_vcard_telephone_msg")).append(")").toString();
                           var1.putEntry(Type.TEL_H_MSG, readSimple(var9, "NUMBER", var7, true));
                        }
                     }
                  }

                  String var33 = var7.toString();
                  if (var33.trim().length() > 0) {
                     var6.append(var13);
                     var6.append(var33);
                  }
               }

               String var22 = var6.toString();
               if (var22.trim().length() > 0) {
                  var25.append(var22);
               }
            }

            Vector<Node> var36 = var0.findLocalNodesByName("ADR");
            if (var36.size() > 0) {
               StringBuilder var34 = new StringBuilder();
               StringBuilder var23 = new StringBuilder();
               StringBuilder var26 = new StringBuilder("\n");
               var34.append(var26.append(Locale.getString("s_jabber_vcard_addresses")).append(":\n").toString());

               for (Node var37 : var36) {
                  if (var37.findFirstLocalNodeByName("WORK") != null) {
                     StringBuilder var40 = new StringBuilder("--(");
                     var34.append(var40.append(Locale.getString("s_jabber_vcard_work")).append(")--\n").toString());
                     var1.putEntry(Type.W_ADR_EXTADD, readSimple(var37, "EXTADD", var23, true));
                     var1.putEntry(Type.W_ADR_STREET, readSimple(var37, "STREET", var23, true));
                     var1.putEntry(Type.W_ADR_LOCALITY, readSimple(var37, "LOCALITY", var23, true));
                     var1.putEntry(Type.W_ADR_REGION, readSimple(var37, "REGION", var23, true));
                     var1.putEntry(Type.W_ADR_PCODE, readSimple(var37, "PCODE", var23, true));
                     var1.putEntry(Type.W_ADR_CTRY, readSimple(var37, "CTRY", var23, true));
                  } else if (var37.findFirstLocalNodeByName("HOME") != null) {
                     StringBuilder var41 = new StringBuilder("--(");
                     var34.append(var41.append(Locale.getString("s_jabber_vcard_home")).append(")--\n").toString());
                     var1.putEntry(Type.W_ADR_EXTADD, readSimple(var37, "EXTADD", var23, true));
                     var1.putEntry(Type.W_ADR_STREET, readSimple(var37, "STREET", var23, true));
                     var1.putEntry(Type.W_ADR_LOCALITY, readSimple(var37, "LOCALITY", var23, true));
                     var1.putEntry(Type.W_ADR_REGION, readSimple(var37, "REGION", var23, true));
                     var1.putEntry(Type.W_ADR_PCODE, readSimple(var37, "PCODE", var23, true));
                     var1.putEntry(Type.W_ADR_CTRY, readSimple(var37, "CTRY", var23, true));
                  } else {
                     var34.append("----------\n");
                     var1.putEntry(Type.H_ADR_EXTADD, readSimple(var37, "EXTADD", var23, true));
                     var1.putEntry(Type.H_ADR_STREET, readSimple(var37, "STREET", var23, true));
                     var1.putEntry(Type.H_ADR_LOCALITY, readSimple(var37, "LOCALITY", var23, true));
                     var1.putEntry(Type.H_ADR_REGION, readSimple(var37, "REGION", var23, true));
                     var1.putEntry(Type.H_ADR_PCODE, readSimple(var37, "PCODE", var23, true));
                     var1.putEntry(Type.H_ADR_CTRY, readSimple(var37, "CTRY", var23, true));
                  }

                  var34.append(var23);
                  var34.append("----------\n");
               }

               if (var23.toString().trim().length() > 0) {
                  var2.append(var34);
               }
            }

            var36 = var0.findLocalNodesByName("EMAIL");
            if (var36.size() > 0) {
               StringBuilder var24 = new StringBuilder();
               StringBuilder var35 = new StringBuilder();
               StringBuilder var28 = new StringBuilder("\n");
               var24.append(var28.append(Locale.getString("s_jabber_vcard_emails")).append(":\n").toString());

               for (Node var29 : var36) {
                  var1.putEntry(Type.EMAIL, readSimple(var29, "USERID", var35, true));
               }

               var24.append(var35);
               var24.append("\n");
               if (var35.toString().trim().length() > 0) {
                  var2.append(var24);
               }
            }

            var1.putEntry(Type.TZ, read(var0, "TZ", var2, Locale.getString("s_jabber_vcard_tz")));
            var1.putEntry(Type.URL, read(var0, "URL", var2, Locale.getString("s_jabber_vcard_webpage")));
            var1.putEntry(Type.DESC, read(var0, "DESC", var2, Locale.getString("s_jabber_vcard_description")));
            var10 = var2.toString().trim();
         }
      }

      return var10;
   }

   private static final String read(Node var0, String var1, StringBuilder var2, String var3) {
      var0 = var0.findFirstLocalNodeByName(var1);
      if (var0 != null) {
         String var5 = var0.getValue();
         if (var5 != null && var5.trim().length() > 0) {
            var2.append(var3);
            var2.append(": ");
            var2.append(var5);
            var2.append("\n");
            return var5;
         }
      }

      return "";
   }

   private static final String readSimple(Node var0, String var1, StringBuilder var2, boolean var3) {
      var0 = var0.findFirstLocalNodeByName(var1);
      if (var0 != null) {
         String var5 = var0.getValue();
         if (var5 != null && var5.trim().length() > 0) {
            var2.append(var5);
            if (var3) {
               var2.append("\n");
            }

            return var5;
         }
      }

      return "";
   }
}
