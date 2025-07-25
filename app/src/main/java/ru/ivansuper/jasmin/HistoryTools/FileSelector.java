package ru.ivansuper.jasmin.HistoryTools;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Vector;
import ru.ivansuper.jasmin.UAdapter;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;

/**
 * A class for selecting files or directories.
 * <p>
 * This class provides a user interface for browsing the file system and selecting a file or directory.
 * It uses a ListView to display the contents of the current directory and allows the user to navigate
 * through the file system by clicking on directories.
 * <p>
 * The class supports two modes: SELECT_FILE and SELECT_DIRECTORY. In SELECT_FILE mode, the user can
 * select a file. In SELECT_DIRECTORY mode, the user can select a directory.
 * <p>
 * When a file or directory is selected, the OnChosedListener is called with the selected file or
 * directory.
 */
public class FileSelector {
    private UAdapter adp;
    private File current_dir;
    private final OnChosedListener listener;
    private final ListView mList;
    private final Mode mode;

    public enum Mode {
        SELECT_FILE,
        SELECT_DIRECTORY;

        /** @noinspection unused*/
        public static Mode[] valuesCustom() {
            Mode[] valuesCustom = values();
            int length = valuesCustom.length;
            Mode[] modeArr = new Mode[length];
            System.arraycopy(valuesCustom, 0, modeArr, 0, length);
            return modeArr;
        }
    }

    public interface OnChosedListener {
        void OnChosed(File file);
    }

    public FileSelector(ListView container, Mode mode, OnChosedListener listener) {
        this.mList = container;
        this.mode = mode;
        this.listener = listener;
        init();
    }

    private void init() {
        this.adp = new UAdapter();
        this.adp.setTextSize(16);
        this.adp.setPadding(16);
        this.mList.setAdapter(this.adp);
        this.mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemView, int position, long itemId) {
                int id = (int) FileSelector.this.adp.getItemId(position);
                String item = FileSelector.this.adp.getItem(position);
                if (id == -1) {
                    FileSelector.this.current_dir = FileSelector.this.current_dir.getParentFile();
                    FileSelector.this.fill();
                } else {
                    if (id == 0) {
                        FileSelector.this.current_dir = new File(utilities.normalizePath(FileSelector.this.current_dir.getAbsolutePath()) + item);
                        FileSelector.this.fill();
                        FileSelector.this.mList.setSelection(0);
                        return;
                    }
                    if (id == 1 && FileSelector.this.mode == Mode.SELECT_FILE) {
                        FileSelector.this.listener.OnChosed(new File(utilities.normalizePath(FileSelector.this.current_dir.getAbsolutePath()) + item));
                    }
                }
            }
        });
        this.current_dir = new File(utilities.normalizePath(resources.SD_PATH));
        fill();
    }

    public final String getCurrentDirPath() {
        return this.current_dir == null ? utilities.normalizePath(resources.SD_PATH) : utilities.normalizePath(this.current_dir.getAbsolutePath());
    }

    private void fill() {
        this.adp.clear();
        if (!this.current_dir.getAbsolutePath().equals(resources.SD_PATH)) {
            this.adp.put(resources.directory_up, "..", -1);
        }
        if (this.current_dir != null) {
            Vector<File> dirs = new Vector<>();
            Vector<File> files = new Vector<>();
            File[] all = this.current_dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    if (file.isDirectory()) {
                        return true;
                    }
                    if (FileSelector.this.mode == Mode.SELECT_DIRECTORY) {
                        return false;
                    }
                    if (file.getName().toLowerCase().endsWith(".jha2")) {
                        try {
                            //noinspection IOStreamConstructor
                            InputStream is = new FileInputStream(file);
                            byte sig1 = (byte) is.read();
                            byte sig2 = (byte) is.read();
                            byte sig3 = (byte) is.read();
                            byte sig4 = (byte) is.read();
                            is.close();
                            if (sig1 == 74 && sig2 == 72 && sig3 == 65 && sig4 == 50) {
                                if (file.length() > 4) {
                                    return true;
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    return false;
                }
            });
            //noinspection DataFlowIssue
            for (File file : all) {
                if (file.isDirectory()) {
                    dirs.add(file);
                } else {
                    files.add(file);
                }
            }
            Collections.sort(dirs);
            Collections.sort(files);
            for (File dir : dirs) {
                this.adp.put(resources.directory, dir.getName(), 0);
            }
            for (File file : files) {
                this.adp.put(resources.file_brw, file.getName(), 1);
            }
        }
    }
}