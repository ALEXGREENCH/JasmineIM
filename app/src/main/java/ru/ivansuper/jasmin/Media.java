package ru.ivansuper.jasmin;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import java.io.File;

public class Media {
    /** @noinspection unused*/
    public static final int AUTH_ACCEPTED = 1;
    /** @noinspection unused*/
    public static final int AUTH_DENIED = 2;
    /** @noinspection unused*/
    public static final int AUTH_REQUEST = 3;
    /** @noinspection unused*/
    public static final int CONTACT_IN = 4;
    /** @noinspection unused*/
    public static final int CONTACT_OUT = 5;
    /** @noinspection unused*/
    public static final int INC_FILE = 6;
    /** @noinspection unused*/
    public static final int INC_MSG = 0;
    /** @noinspection unused*/
    public static final int OUT_MSG = 7;
    /** @noinspection unused*/
    public static final int TRANSFER_REJECTED = 8;
    /** @noinspection FieldCanBeLocal, unused */
    private final Context ctx;
    private final MediaPlayer mp = new MediaPlayer();
    public static int ring_mode = 0;
    public static int phone_mode = 0;

    public Media(Context ctxParam) {
        this.ctx = ctxParam;
    }

    public void playEvent(int event) {
        if (ring_mode == 0 && phone_mode == 0) {
            try {
                switch (event) {
                    case 0:
                        if (MediaTable.inc_msg_e) {
                            this.mp.reset();
                            if (MediaTable.inc_msg.equals("$*INTERNAL*$")) {
                                AssetFileDescriptor ad = resources.ctx.getResources().openRawResourceFd(R.raw.inc_msg);
                                this.mp.setDataSource(ad.getFileDescriptor(), ad.getStartOffset(), ad.getLength());
                                ad.close();
                            } else {
                                Uri link = Uri.fromFile(new File(MediaTable.inc_msg));
                                this.mp.setDataSource(resources.ctx, link);
                            }
                            this.mp.prepare();
                            this.mp.setLooping(false);
                            this.mp.start();
                            break;
                        }
                        break;
                    case 1:
                        if (MediaTable.auth_accepted_e) {
                            this.mp.reset();
                            if (MediaTable.auth_accepted.equals("$*INTERNAL*$")) {
                                AssetFileDescriptor ad2 = resources.ctx.getResources().openRawResourceFd(R.raw.auth_accepted);
                                this.mp.setDataSource(ad2.getFileDescriptor(), ad2.getStartOffset(), ad2.getLength());
                                ad2.close();
                            } else {
                                Uri link2 = Uri.fromFile(new File(MediaTable.auth_accepted));
                                this.mp.setDataSource(resources.ctx, link2);
                            }
                            this.mp.prepare();
                            this.mp.setLooping(false);
                            this.mp.start();
                            break;
                        }
                        break;
                    case 2:
                        if (MediaTable.auth_denied_e) {
                            this.mp.reset();
                            if (MediaTable.auth_denied.equals("$*INTERNAL*$")) {
                                AssetFileDescriptor ad3 = resources.ctx.getResources().openRawResourceFd(R.raw.auth_denied);
                                this.mp.setDataSource(ad3.getFileDescriptor(), ad3.getStartOffset(), ad3.getLength());
                                ad3.close();
                            } else {
                                Uri link3 = Uri.fromFile(new File(MediaTable.auth_denied));
                                this.mp.setDataSource(resources.ctx, link3);
                            }
                            this.mp.prepare();
                            this.mp.setLooping(false);
                            this.mp.start();
                            break;
                        }
                        break;
                    case 3:
                        if (MediaTable.auth_req_e) {
                            this.mp.reset();
                            if (MediaTable.auth_req.equals("$*INTERNAL*$")) {
                                AssetFileDescriptor ad4 = resources.ctx.getResources().openRawResourceFd(R.raw.auth_req);
                                this.mp.setDataSource(ad4.getFileDescriptor(), ad4.getStartOffset(), ad4.getLength());
                                ad4.close();
                            } else {
                                Uri link4 = Uri.fromFile(new File(MediaTable.auth_req));
                                this.mp.setDataSource(resources.ctx, link4);
                            }
                            this.mp.prepare();
                            this.mp.setLooping(false);
                            this.mp.start();
                            break;
                        }
                        break;
                    case 4:
                        if (MediaTable.contact_in_e) {
                            this.mp.reset();
                            if (MediaTable.contact_in.equals("$*INTERNAL*$")) {
                                AssetFileDescriptor ad5 = resources.ctx.getResources().openRawResourceFd(R.raw.contact_in);
                                this.mp.setDataSource(ad5.getFileDescriptor(), ad5.getStartOffset(), ad5.getLength());
                                ad5.close();
                            } else {
                                Uri link5 = Uri.fromFile(new File(MediaTable.contact_in));
                                this.mp.setDataSource(resources.ctx, link5);
                            }
                            this.mp.prepare();
                            this.mp.setLooping(false);
                            this.mp.start();
                            break;
                        }
                        break;
                    case 5:
                        if (MediaTable.contact_out_e) {
                            this.mp.reset();
                            if (MediaTable.contact_out.equals("$*INTERNAL*$")) {
                                AssetFileDescriptor ad6 = resources.ctx.getResources().openRawResourceFd(R.raw.contact_out);
                                this.mp.setDataSource(ad6.getFileDescriptor(), ad6.getStartOffset(), ad6.getLength());
                                ad6.close();
                            } else {
                                Uri link6 = Uri.fromFile(new File(MediaTable.contact_out));
                                this.mp.setDataSource(resources.ctx, link6);
                            }
                            this.mp.prepare();
                            this.mp.setLooping(false);
                            this.mp.start();
                            break;
                        }
                        break;
                    case 6:
                        if (MediaTable.inc_file_e) {
                            this.mp.reset();
                            if (MediaTable.inc_file.equals("$*INTERNAL*$")) {
                                AssetFileDescriptor ad7 = resources.ctx.getResources().openRawResourceFd(R.raw.inc_file);
                                this.mp.setDataSource(ad7.getFileDescriptor(), ad7.getStartOffset(), ad7.getLength());
                                ad7.close();
                            } else {
                                Uri link7 = Uri.fromFile(new File(MediaTable.inc_file));
                                this.mp.setDataSource(resources.ctx, link7);
                            }
                            this.mp.prepare();
                            this.mp.setLooping(false);
                            this.mp.start();
                            break;
                        }
                        break;
                    case 7:
                        if (MediaTable.out_msg_e) {
                            this.mp.reset();
                            if (MediaTable.out_msg.equals("$*INTERNAL*$")) {
                                AssetFileDescriptor ad8 = resources.ctx.getResources().openRawResourceFd(R.raw.out_msg);
                                this.mp.setDataSource(ad8.getFileDescriptor(), ad8.getStartOffset(), ad8.getLength());
                                ad8.close();
                            } else {
                                Uri link8 = Uri.fromFile(new File(MediaTable.out_msg));
                                this.mp.setDataSource(resources.ctx, link8);
                            }
                            this.mp.prepare();
                            this.mp.setLooping(false);
                            this.mp.start();
                            break;
                        }
                        break;
                    case 8:
                        if (MediaTable.transfer_rejected_e) {
                            if (MediaTable.transfer_rejected.equals("$*INTERNAL*$")) {
                                this.mp.reset();
                                AssetFileDescriptor ad9 = resources.ctx.getResources().openRawResourceFd(R.raw.transfer_rejected);
                                this.mp.setDataSource(ad9.getFileDescriptor(), ad9.getStartOffset(), ad9.getLength());
                                ad9.close();
                                this.mp.prepare();
                                this.mp.setLooping(false);
                                this.mp.start();
                                break;
                            } else {
                                this.mp.reset();
                                Uri link9 = Uri.fromFile(new File(MediaTable.transfer_rejected));
                                this.mp.setDataSource(resources.ctx, link9);
                                this.mp.prepare();
                                this.mp.setLooping(false);
                                this.mp.start();
                                break;
                            }
                        }
                        break;
                }
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }
    }
}