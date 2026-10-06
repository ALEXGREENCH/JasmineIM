package ru.ivansuper.jasmin.jabber.vcard;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import ru.ivansuper.jasmin.Base64Coder;
import ru.ivansuper.jasmin.jabber.XML_ENGINE.Node;
import ru.ivansuper.jasmin.resources;

public class Avatar {
    public static Bitmap getAvatar(Node node) {
        Node nodeFindFirstNodeByName;
        Bitmap bitmapCopy;
        synchronized (Avatar.class) {
            if (node == null) {
                bitmapCopy = null;
            } else {
                try {
                    Node nodeFindFirstNodeByName2 = node.findFirstNodeByName("PHOTO");
                    if (nodeFindFirstNodeByName2 == null || (nodeFindFirstNodeByName = nodeFindFirstNodeByName2.findFirstNodeByName("BINVAL")) == null) {
                        bitmapCopy = null;
                    } else {
                        try {
                            try {
                                byte[] bArrDecode = Base64Coder.decode(nodeFindFirstNodeByName.getValue().replaceAll("\n", ""));
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inJustDecodeBounds = true;
                                BitmapFactory.decodeByteArray(bArrDecode, 0, bArrDecode.length, options);
                                if (options.outHeight <= 0) {
                                    throw new Exception("Can't decode bitmap");
                                }
                                int i = options.outWidth;
                                if (i < options.outHeight) {
                                    i = options.outHeight;
                                }
                                int i2 = (int) (resources.dm.density * 128.0f);
                                int i3 = i > i2 ? i / i2 : 1;
                                options.inJustDecodeBounds = false;
                                options.inScaled = false;
                                options.inSampleSize = i3;
                                bitmapCopy = BitmapFactory.decodeByteArray(bArrDecode, 0, bArrDecode.length, options).copy(Bitmap.Config.ARGB_4444, false);
                            } catch (OutOfMemoryError e) {
                                e.printStackTrace();
                                bitmapCopy = null;
                            }
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            bitmapCopy = null;
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
        return bitmapCopy;
    }

    public static Bitmap getAvatarHQ(Node node) {
        Node nodeFindFirstNodeByName;
        Bitmap bitmapDecodeByteArray;
        synchronized (Avatar.class) {
            if (node == null) {
                bitmapDecodeByteArray = null;
            } else {
                try {
                    Node nodeFindFirstNodeByName2 = node.findFirstNodeByName("PHOTO");
                    if (nodeFindFirstNodeByName2 == null || (nodeFindFirstNodeByName = nodeFindFirstNodeByName2.findFirstNodeByName("BINVAL")) == null) {
                        bitmapDecodeByteArray = null;
                    } else {
                        try {
                            byte[] bArrDecode = Base64Coder.decode(nodeFindFirstNodeByName.getValue().replaceAll("\n", ""));
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeByteArray(bArrDecode, 0, bArrDecode.length, options);
                            if (options.outHeight <= 0) {
                                throw new Exception("Can't decode bitmap");
                            }
                            int i = options.outWidth;
                            if (i < options.outHeight) {
                                i = options.outHeight;
                            }
                            int i2 = (int) (resources.dm.density * 256.0f);
                            int i3 = i > i2 ? i / i2 : 1;
                            options.inJustDecodeBounds = false;
                            options.inScaled = false;
                            options.inSampleSize = i3;
                            bitmapDecodeByteArray = BitmapFactory.decodeByteArray(bArrDecode, 0, bArrDecode.length, options);
                        } catch (Exception e) {
                            e.printStackTrace();
                            bitmapDecodeByteArray = null;
                        } catch (OutOfMemoryError e2) {
                            e2.printStackTrace();
                            bitmapDecodeByteArray = null;
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
        return bitmapDecodeByteArray;
    }

    public static Bitmap normalizeAvatar(File file) {
        Bitmap bitmap;
        synchronized (Avatar.class) {
            bitmap = null;
            if (file != null) {
                try {
                    if (file.exists()) {
                        try {
                            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                            bufferedInputStream.mark(0);
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeStream(bufferedInputStream, null, options);
                            if (options.outHeight <= 0) {
                                throw new Exception("Can't decode bitmap");
                            }
                            int i = options.outWidth;
                            float f = i / options.outHeight;
                            if (i > 256) {
                                i = 256;
                            }
                            options.inJustDecodeBounds = false;
                            bufferedInputStream.reset();
                            Bitmap bitmapCreateScaledBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(bufferedInputStream, null, options), i, (int) (i / f), true);
                            bufferedInputStream.close();
                            bitmapCreateScaledBitmap.setDensity(0);
                            bitmap = bitmapCreateScaledBitmap;
                        } catch (Exception e) {
                        } catch (OutOfMemoryError e2) {
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
        return bitmap;
    }
}
