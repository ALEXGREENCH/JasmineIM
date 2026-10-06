package ru.ivansuper.jasmin.jabber.FileTransfer;

import android.view.ViewGroup;
import java.util.Vector;

public class TransferController {
    private static final Vector<FileTransfer> mTransfers = new Vector<>();

    public static final FileTransfer clearDisplay(ViewGroup viewGroup) {
        int iHashCode = viewGroup.hashCode();
        for (int i = 0; i < mTransfers.size(); i++) {
            FileTransfer fileTransfer = mTransfers.get(i);
            if (fileTransfer.getDisplayHash() == iHashCode) {
                fileTransfer.clearDisplay();
                viewGroup.setVisibility(8);
                return fileTransfer;
            }
        }
        return null;
    }

    public static final FileTransfer getTransfer(String str) {
        FileTransfer fileTransfer;
        synchronized (TransferController.class) {
            int i = 0;
            while (true) {
                try {
                    if (i < mTransfers.size()) {
                        FileTransfer fileTransfer2 = mTransfers.get(i);
                        if (fileTransfer2.getID().equals(str)) {
                            fileTransfer = fileTransfer2;
                            break;
                        }
                        i++;
                    } else {
                        fileTransfer = null;
                        break;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
        return fileTransfer;
    }

    public static final void putTransfer(FileTransfer fileTransfer) {
        synchronized (TransferController.class) {
            try {
                mTransfers.add(fileTransfer);
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public static final FileTransfer updateDisplay(ViewGroup viewGroup) {
        int iHashCode = viewGroup.hashCode();
        for (int i = 0; i < mTransfers.size(); i++) {
            FileTransfer fileTransfer = mTransfers.get(i);
            if (fileTransfer.getDisplayHash() == iHashCode) {
                fileTransfer.updateDisplay();
                return fileTransfer;
            }
        }
        return null;
    }
}
