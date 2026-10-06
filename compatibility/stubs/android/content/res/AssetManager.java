package android.content.res;

import java.io.*;

/** Real asset bytes through a filesystem stream; no Android runtime emulation. */
public class AssetManager {
    public InputStream open(String name) throws IOException {
        return new FileInputStream(new File(System.getProperty("assets"), name));
    }
}
