package ru.ivansuper.jasmin.base;

import android.app.Application;
import android.util.Log;

import java.io.File;

import ru.ivansuper.jasmin.Clients.IcqCapsBase;
import ru.ivansuper.jasmin.Clients.IcqClientDetector;
import ru.ivansuper.jasmin.MD5;
import ru.ivansuper.jasmin.MediaTable;
import ru.ivansuper.jasmin.Preferences.Manager;
import ru.ivansuper.jasmin.SmileysManager;
import ru.ivansuper.jasmin.base.ach.ADB;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.debug;
import ru.ivansuper.jasmin.jabber.Clients;
import ru.ivansuper.jasmin.resources;

/**
 * The main application class for Jasmin.
 * This class extends {@link Application} and is responsible for initializing
 * various components and resources when the application starts.
 *
 * <p>Key responsibilities include:
 * <ul>
 *     <li>Setting up a global application instance ({@link #INSTANCE}).</li>
 *     <li>Initializing resources such as the application context and data path.</li>
 *     <li>Checking for the first application start and performing necessary setup.</li>
 *     <li>Initializing core components like ADB, MD5, debugging utilities, ICQ capabilities,
 *     Jabber clients, media table, smiley manager, and color schemes.</li>
 *     <li>Logging the maximum available heap size for the application.</li>
 * </ul>
 * </p>
 *
 * <p>The initialization logic in {@link #onCreate()} is designed to run only once,
 * even if the method is called multiple times (e.g., due to process recreation),
 * by checking if {@link resources#service} and {@link resources#ctx} are null.
 * </p>
 */
public class APP extends Application {

    /**
     * A static reference to the application instance. This allows for global access
     * to the application context and its resources.
     */
    public static Application INSTANCE;

    /**
     * Called when the application is starting, before any other application objects have been created.
     * This method initializes the application-wide resources and components.
     *
     * <p>The initialization process includes:
     * <ul>
     *     <li>Setting the global {@link #INSTANCE} to this application object.</li>
     *     <li>Putting the application context into {@link resources} if not already set.</li>
     *     <li>Checking if it's the first start of the application by looking for "profiles.cfg".
     *         If it's the first start, {@link Manager#checkFirstStartAndReset()} is called.</li>
     *     <li>Initializing {@link ADB}.</li>
     *     <li>Initializing {@link MD5}.</li>
     *     <li>Initializing {@link debug} if it hasn't been initialized yet.</li>
     *     <li>Initializing {@link IcqCapsBase}.</li>
     *     <li>Loading Jabber {@link Clients}.</li>
     *     <li>Initializing {@link IcqClientDetector}.</li>
     *     <li>Initializing {@link MediaTable}.</li>
     *     <li>Initializing {@link SmileysManager} with the application context.</li>
     *     <li>Initializing {@link ColorScheme}.</li>
     *     <li>Logging the maximum available heap size for the application.</li>
     * </ul>
     * </p>
     * <p>
     * This initialization logic is guarded by a check ({@code resources.service == null && resources.ctx == null})
     * to ensure that these components are initialized only once, even if {@code onCreate} is called multiple times
     * (e.g., due to process recreation).
     * </p>
     */
    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        if (resources.service == null && resources.ctx == null) {
            resources.putContext(this);
            File profs = new File(resources.dataPath + "profiles.cfg");
            if (!profs.exists()) {
                Manager.checkFirstStartAndReset();
            }
            ADB.init();
            MD5.init();
            if (!debug.initialized) {
                debug.init();
                debug.initialized = true;
            }
            IcqCapsBase.init();
            Clients.load();
            IcqClientDetector.init();
            MediaTable.init();
            SmileysManager.init(getApplicationContext());
            ColorScheme.initialize();
            Log.e("Runtime:VMHeap", "Max size: " + ((Runtime.getRuntime().maxMemory() / 1024) / 1024) + " MB");
        }
    }
}
