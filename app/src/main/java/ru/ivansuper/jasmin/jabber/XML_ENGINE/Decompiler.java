package ru.ivansuper.jasmin.jabber.XML_ENGINE;

import android.util.Log;
import java.util.Vector;

public class Decompiler {
   private static final byte IN_TAG = 2;
   private static final byte PARAM_NAME = 3;
   private static final byte PARAM_VALUE = 4;
   private static final byte QUOTE_E = 6;
   private static final byte QUOTE_S = 5;
   private static final byte SKIP = 7;
   private static final byte TAG_NAME = 1;
   private static final byte TEXT = 0;

   public static final Decompiler getInstance() {
      return new Decompiler();
   }



   public final Node Decompile(StringBuffer var1) throws Exception {
      byte var2 = 0;
      Node var3 = null;
      Node var4 = null;
      int var5 = var1.length();
      StringBuffer var6 = new StringBuffer();
      Vector var7 = new Vector();
      StringBuffer var8 = new StringBuffer();
      StringBuffer var9 = new StringBuffer();
      StringBuffer var10 = new StringBuffer();
      boolean var11 = false;
      int var12 = 0;
      int var13 = 0;
      boolean var14 = false;
      int var15 = 0;

      int var106 = 0;
      int var107 = 0;
      int var108 = 0;
      while (var15 < var5) {
         Node var27;
         Node var28;
         int var29;
         int var30;
         int var31;
         boolean var32;
         int var33;
         boolean var34;
         label247: {
            Node var20;
            Node var21;
            int var22;
            int var23;
            boolean var24;
            int var25;
            boolean var26;
            label246: {
               char var19;
               label255: {
                  label244: {
                     label243: {
                        label242: {
                           label241: {
                              label240: {
                                 label239: {
                                    label238: {
                                       label256: {
                                          try {
                                             var106 = var13;
                                             var107 = var15;
                                             var108 = var12;
                                             var19 = var1.charAt(var15);
                                             var20 = var4;
                                             var21 = var3;
                                             var22 = var2;
                                             var23 = var15;
                                             var24 = var11;
                                             var25 = var12;
                                             var26 = var14;
                                             switch (var2) {
                                                case 0:
                                                   var22 = var2;
                                                   if (var19 != '<') {
                                                      break label244;
                                                   }

                                                   var22 = 1;
                                                   if (var4 == null) {
                                                      break label238;
                                                   }

                                                   var106 = var13;
                                                   var107 = var15;
                                                   var108 = var12;
                                                   if (var4.VALUE == null) {
                                                      var106 = var13;
                                                      var107 = var15;
                                                      var108 = var12;
                                                      var4.VALUE = var10.toString();
                                                      break label256;
                                                   }
                                                   break;
                                                case 1:
                                                   break label239;
                                                case 2:
                                                   break label242;
                                                case 3:
                                                   break label255;
                                                case 4:
                                                   break label240;
                                                case 5:
                                                   break label241;
                                                case 6:
                                                   break label246;
                                                case 7:
                                                   break label243;
                                                default:
                                                   var26 = var14;
                                                   var25 = var12;
                                                   var24 = var11;
                                                   var23 = var15;
                                                   var22 = var2;
                                                   var21 = var3;
                                                   var20 = var4;
                                                   break label246;
                                             }
                                          } catch (Exception var50) {
                                             var50.printStackTrace();
                                             StringBuilder var67 = new StringBuilder();
                                             var67.append("Src: ");
                                             var67.append(var1.toString());
                                             var67.append("Pos: ");
                                             var67.append(var107);
                                             var67.append("Opens/Closes: ");
                                             var67.append(var108);
                                             var67.append("/");
                                             var67.append(var106);
                                             String var51 = var67.toString();
                                             Log.e("Description", var51);
                                             Log.e("Pos", "" + var107);
                                             Log.e("Opens/Closes", var108 + "/" + var106);
                                             throw new RuntimeException(var51);
                                          }

                                          var106 = var13;
                                          var107 = var15;
                                          var108 = var12;

                                          try {
                                             String var93 = var4.VALUE;
                                             var106 = var13;
                                             var107 = var15;
                                             var108 = var12;
                                             var106 = var13;
                                             var107 = var15;
                                             var108 = var12;
                                             StringBuilder var98 = new StringBuilder(String.valueOf(var93));
                                             var106 = var13;
                                             var107 = var15;
                                             var108 = var12;
                                             var4.VALUE = var98.append(var10.toString()).toString();
                                          } catch (Exception var42) {
                                             var42.printStackTrace();
                                             StringBuilder var75 = new StringBuilder();
                                             var75.append("Src: ");
                                             var75.append(var1.toString());
                                             var75.append("Pos: ");
                                             var75.append(var107);
                                             var75.append("Opens/Closes: ");
                                             var75.append(var108);
                                             var75.append("/");
                                             var75.append(var106);
                                             String var58 = var75.toString();
                                             Log.e("Description", var58);
                                             Log.e("Pos", "" + var107);
                                             Log.e("Opens/Closes", var108 + "/" + var106);
                                             throw new RuntimeException(var58);
                                          }
                                       }

                                       try {
                                          var106 = var13;
                                          var107 = var15;
                                          var108 = var12;
                                          var10.setLength(0);
                                       } catch (Exception var41) {
                                          var41.printStackTrace();
                                          StringBuilder var76 = new StringBuilder();
                                          var76.append("Src: ");
                                          var76.append(var1.toString());
                                          var76.append("Pos: ");
                                          var76.append(var107);
                                          var76.append("Opens/Closes: ");
                                          var76.append(var108);
                                          var76.append("/");
                                          var76.append(var106);
                                          String var59 = var76.toString();
                                          Log.e("Description", var59);
                                          Log.e("Pos", "" + var107);
                                          Log.e("Opens/Closes", var108 + "/" + var106);
                                          throw new RuntimeException(var59);
                                       }
                                    }

                                    try {
                                       var106 = var13;
                                       var107 = var15;
                                       var108 = var12;
                                       var6.setLength(0);
                                       var106 = var13;
                                       var107 = var15;
                                       var108 = var12;
                                       var7.clear();
                                       var11 = false;
                                       break label244;
                                    } catch (Exception var40) {
                                       var40.printStackTrace();
                                       StringBuilder var77 = new StringBuilder();
                                       var77.append("Src: ");
                                       var77.append(var1.toString());
                                       var77.append("Pos: ");
                                       var77.append(var107);
                                       var77.append("Opens/Closes: ");
                                       var77.append(var108);
                                       var77.append("/");
                                       var77.append(var106);
                                       String var60 = var77.toString();
                                       Log.e("Description", var60);
                                       Log.e("Pos", "" + var107);
                                       Log.e("Opens/Closes", var108 + "/" + var106);
                                       throw new RuntimeException(var60);
                                    }
                                 }

                                 try {
                                    if (var19 == ' ' && var11) {
                                       var22 = 2;
                                       var20 = var4;
                                       var21 = var3;
                                       var23 = var15;
                                       var24 = var11;
                                       var25 = var12;
                                       var26 = var14;
                                    } else {
                                       if (var19 == ' ') {
                                          var20 = var4;
                                          var21 = var3;
                                          var22 = var2;
                                          var23 = var15;
                                          var24 = var11;
                                          var25 = var12;
                                          var26 = var14;
                                          if (!var11) {
                                             break label246;
                                          }
                                       }

                                       if (var19 == '/' && !var11) {
                                          var30 = var13 + 1;
                                          var29 = 7;
                                          var106 = var30;
                                          var107 = var15;
                                          var108 = var12;
                                          var27 = var4.parent;
                                          var28 = var3;
                                          var31 = var15;
                                          var32 = var11;
                                          var33 = var12;
                                          var34 = var14;
                                          break label247;
                                       }

                                       if (var19 == '!' && !var11) {
                                          var29 = 7;
                                          var27 = var4;
                                          var28 = var3;
                                          var30 = var13;
                                          var31 = var15;
                                          var32 = var11;
                                          var33 = var12;
                                          var34 = var14;
                                          break label247;
                                       }

                                       if (var19 == '/' && var11) {
                                          var106 = var13;
                                          var107 = var15;
                                          var108 = var12;
                                          if (var6.toString().equals("stream:stream")) {
                                             var34 = true;
                                             var27 = var4;
                                             var28 = var3;
                                             var29 = var2;
                                             var30 = var13;
                                             var31 = var15;
                                             var32 = var11;
                                             var33 = var12;
                                          } else {
                                             var106 = var13;
                                             var107 = var15;
                                             var108 = var12;
                                             var106 = var13;
                                             var107 = var15;
                                             var108 = var12;
                                             var21 = new Node();
                                             var106 = var13;
                                             var107 = var15;
                                             var108 = var12;
                                             var21.NAME = var6.toString();
                                             var106 = var13;
                                             var107 = var15;
                                             var108 = var12;
                                             var21.parent = var4;
                                             var28 = var3;
                                             if (var3 == null) {
                                                var28 = var21;
                                             }

                                             if (var4 != null) {
                                                var106 = var13;
                                                var107 = var15;
                                                var108 = var12;
                                                var4.childs.add(var21);
                                             }

                                             var106 = var13;
                                             var107 = var15;
                                             var108 = var12;
                                             var10.setLength(0);
                                             var33 = var12 + 1;
                                             var30 = var13 + 1;
                                             var29 = 7;
                                             var27 = var4;
                                             var31 = var15;
                                             var32 = var11;
                                             var34 = var14;
                                          }
                                          break label247;
                                       }

                                       if (var19 == '>') {
                                          var25 = var12 + 1;
                                          var106 = var13;
                                          var107 = var15;
                                          var108 = var25;
                                          var106 = var13;
                                          var107 = var15;
                                          var108 = var25;
                                          var28 = new Node();
                                          var106 = var13;
                                          var107 = var15;
                                          var108 = var25;
                                          var28.NAME = var6.toString();
                                          var106 = var13;
                                          var107 = var15;
                                          var108 = var25;
                                          var28.parent = var4;
                                          var21 = var3;
                                          if (var3 == null) {
                                             var21 = var28;
                                          }

                                          if (var4 != null) {
                                             var106 = var13;
                                             var107 = var15;
                                             var108 = var25;
                                             var4.childs.add(var28);
                                          }

                                          var22 = 0;
                                          var106 = var13;
                                          var107 = var15;
                                          var108 = var25;
                                          var10.setLength(0);
                                          var20 = var28;
                                          var23 = var15;
                                          var24 = var11;
                                          var26 = var14;
                                       } else {
                                          var106 = var13;
                                          var107 = var15;
                                          var108 = var12;
                                          var6.append(var19);
                                          var24 = true;
                                          var20 = var4;
                                          var21 = var3;
                                          var22 = var2;
                                          var23 = var15;
                                          var25 = var12;
                                          var26 = var14;
                                       }
                                    }
                                    break label246;
                                 } catch (Exception var49) {
                                    var49.printStackTrace();
                                    StringBuilder var74 = new StringBuilder();
                                    var74.append("Src: ");
                                    var74.append(var1.toString());
                                    var74.append("Pos: ");
                                    var74.append(var107);
                                    var74.append("Opens/Closes: ");
                                    var74.append(var108);
                                    var74.append("/");
                                    var74.append(var106);
                                    String var57 = var74.toString();
                                    Log.e("Description", var57);
                                    Log.e("Pos", "" + var107);
                                    Log.e("Opens/Closes", var108 + "/" + var106);
                                    throw new RuntimeException(var57);
                                 }
                              }

                              try {
                                 if (var19 != '"') {
                                    var20 = var4;
                                    var21 = var3;
                                    var22 = var2;
                                    var23 = var15;
                                    var24 = var11;
                                    var25 = var12;
                                    var26 = var14;
                                    if (var19 != '\'') {
                                       break label246;
                                    }
                                 }

                                 var22 = 5;
                                 var20 = var4;
                                 var21 = var3;
                                 var23 = var15;
                                 var24 = var11;
                                 var25 = var12;
                                 var26 = var14;
                                 break label246;
                              } catch (Exception var48) {
                                 var48.printStackTrace();
                                 StringBuilder var70 = new StringBuilder();
                                 var70.append("Src: ");
                                 var70.append(var1.toString());
                                 var70.append("Pos: ");
                                 var70.append(var107);
                                 var70.append("Opens/Closes: ");
                                 var70.append(var108);
                                 var70.append("/");
                                 var70.append(var106);
                                 String var54 = var70.toString();
                                 Log.e("Description", var54);
                                 Log.e("Pos", "" + var107);
                                 Log.e("Opens/Closes", var108 + "/" + var106);
                                 throw new RuntimeException(var54);
                              }
                           }

                           try {
                              if (var19 != '"' && var19 != '\'') {
                                 var106 = var13;
                                 var107 = var15;
                                 var108 = var12;
                                 var9.append(var19);
                                 var20 = var4;
                                 var21 = var3;
                                 var22 = var2;
                                 var23 = var15;
                                 var24 = var11;
                                 var25 = var12;
                                 var26 = var14;
                              } else {
                                 var22 = 2;
                                 var106 = var13;
                                 var107 = var15;
                                 var108 = var12;
                                 var106 = var13;
                                 var107 = var15;
                                 var108 = var12;
                                 Parameter var89 = new Parameter();
                                 var106 = var13;
                                 var107 = var15;
                                 var108 = var12;
                                 var89.NAME = var8.toString();
                                 var106 = var13;
                                 var107 = var15;
                                 var108 = var12;
                                 var89.VALUE = var9.toString();
                                 var106 = var13;
                                 var107 = var15;
                                 var108 = var12;
                                 var7.add(var89);
                                 var20 = var4;
                                 var21 = var3;
                                 var23 = var15;
                                 var24 = var11;
                                 var25 = var12;
                                 var26 = var14;
                              }
                              break label246;
                           } catch (Exception var47) {
                              var47.printStackTrace();
                              StringBuilder var69 = new StringBuilder();
                              var69.append("Src: ");
                              var69.append(var1.toString());
                              var69.append("Pos: ");
                              var69.append(var107);
                              var69.append("Opens/Closes: ");
                              var69.append(var108);
                              var69.append("/");
                              var69.append(var106);
                              String var53 = var69.toString();
                              Log.e("Description", var53);
                              Log.e("Pos", "" + var107);
                              Log.e("Opens/Closes", var108 + "/" + var106);
                              throw new RuntimeException(var53);
                           }
                        }

                        try {
                           var20 = var4;
                           var21 = var3;
                           var22 = var2;
                           var23 = var15;
                           var24 = var11;
                           var25 = var12;
                           var26 = var14;
                           if (var19 != ' ') {
                              if (var19 == '/') {
                                 var22 = var12 + 1;
                                 var12 = var13 + 1;
                                 var29 = 7;
                                 var106 = var12;
                                 var107 = var15;
                                 var108 = var22;
                                 var106 = var12;
                                 var107 = var15;
                                 var108 = var22;
                                 var20 = new Node();
                                 var106 = var12;
                                 var107 = var15;
                                 var108 = var22;
                                 var20.NAME = var6.toString();
                                 var106 = var12;
                                 var107 = var15;
                                 var108 = var22;
                                 var20.parent = var4;
                                 var106 = var12;
                                 var107 = var15;
                                 var108 = var22;
                                 var20.params = (Vector<Parameter>)var7.clone();
                                 var21 = var3;
                                 if (var3 == null) {
                                    var21 = var20;
                                 }

                                 if (var4 != null) {
                                    var106 = var12;
                                    var107 = var15;
                                    var108 = var22;
                                    var4.childs.add(var20);
                                 }

                                 var27 = var4;
                                 var28 = var21;
                                 var30 = var12;
                                 var31 = var15;
                                 var32 = var11;
                                 var33 = var22;
                                 var34 = var14;
                                 var106 = var12;
                                 var107 = var15;
                                 var108 = var22;
                                 if (var20.NAME.toString().equals("stream:stream")) {
                                    var34 = true;
                                    var29 = 7;
                                    var27 = var4;
                                    var28 = var21;
                                    var30 = var12;
                                    var31 = var15;
                                    var32 = var11;
                                    var33 = var22;
                                 }
                                 break label247;
                              }

                              if (var19 == '>') {
                                 var29 = var12 + 1;
                                 var106 = var13;
                                 var107 = var15;
                                 var108 = var29;
                                 var106 = var13;
                                 var107 = var15;
                                 var108 = var29;
                                 var27 = new Node();
                                 var106 = var13;
                                 var107 = var15;
                                 var108 = var29;
                                 var27.NAME = var6.toString();
                                 var106 = var13;
                                 var107 = var15;
                                 var108 = var29;
                                 var27.parent = var4;
                                 var106 = var13;
                                 var107 = var15;
                                 var108 = var29;
                                 var27.params = (Vector<Parameter>)var7.clone();
                                 var28 = var3;
                                 if (var3 == null) {
                                    var28 = var27;
                                 }

                                 if (var4 != null) {
                                    var106 = var13;
                                    var107 = var15;
                                    var108 = var29;
                                    var4.childs.add(var27);
                                 }

                                 var3 = var27;
                                 var22 = 0;
                                 var106 = var13;
                                 var107 = var15;
                                 var108 = var29;
                                 var10.setLength(0);
                                 var20 = var3;
                                 var21 = var28;
                                 var23 = var15;
                                 var24 = var11;
                                 var25 = var29;
                                 var26 = var14;
                                 var106 = var13;
                                 var107 = var15;
                                 var108 = var29;
                                 if (var27.NAME.toString().equals("stream:stream")) {
                                    var26 = true;
                                    var22 = 0;
                                    var20 = var3;
                                    var21 = var28;
                                    var23 = var15;
                                    var24 = var11;
                                    var25 = var29;
                                 }
                              } else {
                                 if (var19 == '?') {
                                    var34 = true;
                                    var106 = var13;
                                    var107 = var15;
                                    var108 = var12;
                                    var106 = var13;
                                    var107 = var15;
                                    var108 = var12;
                                    var21 = new Node();
                                    var106 = var13;
                                    var107 = var15;
                                    var108 = var12;
                                    var21.NAME = var6.toString();
                                    var106 = var13;
                                    var107 = var15;
                                    var108 = var12;
                                    var21.parent = var4;
                                    var106 = var13;
                                    var107 = var15;
                                    var108 = var12;
                                    var21.params = (Vector<Parameter>)var7.clone();
                                    var28 = var3;
                                    if (var3 == null) {
                                       var28 = var21;
                                    }

                                    if (var4 != null) {
                                       var106 = var13;
                                       var107 = var15;
                                       var108 = var12;
                                       var4.childs.add(var21);
                                    }

                                    var29 = 7;
                                    var106 = var13;
                                    var107 = var15;
                                    var108 = var12;
                                    var10.setLength(0);
                                    var27 = var21;
                                    var30 = var13;
                                    var31 = var15;
                                    var32 = var11;
                                    var33 = var12;
                                    break label247;
                                 }

                                 var22 = 3;
                                 var106 = var13;
                                 var107 = var15;
                                 var108 = var12;
                                 var8.setLength(0);
                                 var106 = var13;
                                 var107 = var15;
                                 var108 = var12;
                                 var9.setLength(0);
                                 var23 = var15 - 1;
                                 var20 = var4;
                                 var21 = var3;
                                 var24 = var11;
                                 var25 = var12;
                                 var26 = var14;
                              }
                           }
                           break label246;
                        } catch (Exception var46) {
                           var46.printStackTrace();
                           StringBuilder var72 = new StringBuilder();
                           var72.append("Src: ");
                           var72.append(var1.toString());
                           var72.append("Pos: ");
                           var72.append(var107);
                           var72.append("Opens/Closes: ");
                           var72.append(var108);
                           var72.append("/");
                           var72.append(var106);
                           String var56 = var72.toString();
                           Log.e("Description", var56);
                           Log.e("Pos", "" + var107);
                           Log.e("Opens/Closes", var108 + "/" + var106);
                           throw new RuntimeException(var56);
                        }
                     }

                     try {
                        var27 = var4;
                        var28 = var3;
                        var29 = var2;
                        var30 = var13;
                        var31 = var15;
                        var32 = var11;
                        var33 = var12;
                        var34 = var14;
                        if (var19 != '>') {
                           break label247;
                        }

                        var20 = var4;
                        var21 = var3;
                        var22 = var2;
                        var23 = var15;
                        var24 = var11;
                        var25 = var12;
                        var26 = var14;
                        if (!var14) {
                           var22 = 0;
                           var106 = var13;
                           var107 = var15;
                           var108 = var12;
                           var10.setLength(0);
                           var20 = var4;
                           var21 = var3;
                           var23 = var15;
                           var24 = var11;
                           var25 = var12;
                           var26 = var14;
                        }
                        break label246;
                     } catch (Exception var45) {
                        var45.printStackTrace();
                        StringBuilder var68 = new StringBuilder();
                        var68.append("Src: ");
                        var68.append(var1.toString());
                        var68.append("Pos: ");
                        var68.append(var107);
                        var68.append("Opens/Closes: ");
                        var68.append(var108);
                        var68.append("/");
                        var68.append(var106);
                        String var52 = var68.toString();
                        Log.e("Description", var52);
                        Log.e("Pos", "" + var107);
                        Log.e("Opens/Closes", var108 + "/" + var106);
                        throw new RuntimeException(var52);
                     }
                  }

                  try {
                     var106 = var13;
                     var107 = var15;
                     var108 = var12;
                     var10.append(var19);
                  } catch (Exception var39) {
                     var39.printStackTrace();
                     StringBuilder var78 = new StringBuilder();
                     var78.append("Src: ");
                     var78.append(var1.toString());
                     var78.append("Pos: ");
                     var78.append(var107);
                     var78.append("Opens/Closes: ");
                     var78.append(var108);
                     var78.append("/");
                     var78.append(var106);
                     String var61 = var78.toString();
                     Log.e("Description", var61);
                     Log.e("Pos", "" + var107);
                     Log.e("Opens/Closes", var108 + "/" + var106);
                     throw new RuntimeException(var61);
                  }

                  var20 = var4;
                  var21 = var3;
                  var23 = var15;
                  var24 = var11;
                  var25 = var12;
                  var26 = var14;
                  break label246;
               }

               try {
                  if (var19 == '=') {
                     var22 = 4;
                     var20 = var4;
                     var21 = var3;
                     var23 = var15;
                     var24 = var11;
                     var25 = var12;
                     var26 = var14;
                  } else {
                     var106 = var13;
                     var107 = var15;
                     var108 = var12;
                     var8.append(var19);
                     var20 = var4;
                     var21 = var3;
                     var22 = var2;
                     var23 = var15;
                     var24 = var11;
                     var25 = var12;
                     var26 = var14;
                  }
               } catch (Exception var38) {
                  var38.printStackTrace();
                  StringBuilder var71 = new StringBuilder();
                  var71.append("Src: ");
                  var71.append(var1.toString());
                  var71.append("Pos: ");
                  var71.append(var107);
                  var71.append("Opens/Closes: ");
                  var71.append(var108);
                  var71.append("/");
                  var71.append(var106);
                  String var55 = var71.toString();
                  Log.e("Description", var55);
                  Log.e("Pos", "" + var107);
                  Log.e("Opens/Closes", var108 + "/" + var106);
                  throw new RuntimeException(var55);
               }
            }

            try {
               var3 = var21;
               var15 = var23;
               var12 = var25;
               var14 = var26;
               if (var26) {
                  break;
               }

               var27 = var20;
               var28 = var21;
               var29 = var22;
               var30 = var13;
               var31 = var23;
               var32 = var24;
               var33 = var25;
               var34 = var26;
               if (var13 == var25) {
                  var3 = var21;
                  var15 = var23;
                  var12 = var25;
                  var14 = var26;
                  if (var25 > 0) {
                     break;
                  }

                  var34 = var26;
                  var33 = var25;
                  var32 = var24;
                  var31 = var23;
                  var30 = var13;
                  var29 = var22;
                  var28 = var21;
                  var27 = var20;
               }
            } catch (Exception var44) {
               var44.printStackTrace();
               StringBuilder var79 = new StringBuilder();
               var79.append("Src: ");
               var79.append(var1.toString());
               var79.append("Pos: ");
               var79.append(var107);
               var79.append("Opens/Closes: ");
               var79.append(var108);
               var79.append("/");
               var79.append(var106);
               String var62 = var79.toString();
               Log.e("Description", var62);
               Log.e("Pos", "" + var107);
               Log.e("Opens/Closes", var108 + "/" + var106);
               throw new RuntimeException(var62);
            }
         }

         try {
            var15 = var31 + 1;
            var4 = var27;
            var3 = var28;
            var2 = (byte)var29;
            var13 = var30;
            var11 = var32;
            var12 = var33;
            var14 = var34;
         } catch (Exception var37) {
            var37.printStackTrace();
            StringBuilder var80 = new StringBuilder();
            var80.append("Src: ");
            var80.append(var1.toString());
            var80.append("Pos: ");
            var80.append(var107);
            var80.append("Opens/Closes: ");
            var80.append(var108);
            var80.append("/");
            var80.append(var106);
            String var63 = var80.toString();
            Log.e("Description", var63);
            Log.e("Pos", "" + var107);
            Log.e("Opens/Closes", var108 + "/" + var106);
            throw new RuntimeException(var63);
         }
      }

      if (var14) {
         var106 = var13;
         var107 = var15;
         var108 = var12;

         try {
            var1.delete(0, var15 + 1);
         } catch (Exception var36) {
            var36.printStackTrace();
            StringBuilder var81 = new StringBuilder();
            var81.append("Src: ");
            var81.append(var1.toString());
            var81.append("Pos: ");
            var81.append(var107);
            var81.append("Opens/Closes: ");
            var81.append(var108);
            var81.append("/");
            var81.append(var106);
            String var64 = var81.toString();
            Log.e("Description", var64);
            Log.e("Pos", "" + var107);
            Log.e("Opens/Closes", var108 + "/" + var106);
            throw new RuntimeException(var64);
         }
      } else {
         label157: {
            try {
               if (var13 == var12 && var12 > 0) {
                  var106 = var13;
                  var107 = var15;
                  var108 = var12;
                  var1.delete(0, var15 + 1);
                  break label157;
               }
            } catch (Exception var43) {
               var43.printStackTrace();
               StringBuilder var82 = new StringBuilder();
               var82.append("Src: ");
               var82.append(var1.toString());
               var82.append("Pos: ");
               var82.append(var107);
               var82.append("Opens/Closes: ");
               var82.append(var108);
               var82.append("/");
               var82.append(var106);
               String var65 = var82.toString();
               Log.e("Description", var65);
               Log.e("Pos", "" + var107);
               Log.e("Opens/Closes", var108 + "/" + var106);
               throw new RuntimeException(var65);
            }

            var3 = null;
         }
      }

      try {
         return var3;
      } catch (Exception var35) {
         var35.printStackTrace();
         StringBuilder var83 = new StringBuilder();
         var83.append("Src: ");
         var83.append(var1.toString());
         var83.append("Pos: ");
         var83.append(var107);
         var83.append("Opens/Closes: ");
         var83.append(var108);
         var83.append("/");
         var83.append(var106);
         String var66 = var83.toString();
         Log.e("Description", var66);
         Log.e("Pos", "" + var107);
         Log.e("Opens/Closes", var108 + "/" + var106);
         throw new RuntimeException(var66);
      }
   }
}
