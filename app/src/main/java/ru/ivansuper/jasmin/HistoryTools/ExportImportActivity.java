package ru.ivansuper.jasmin.HistoryTools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.Vector;

import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.UAdapter;
import ru.ivansuper.jasmin.dialogs.DialogBuilder;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.protocols.IMProfile;
import ru.ivansuper.jasmin.resources;
import ru.ivansuper.jasmin.utilities;
import ru.ivansuper.jasmin.utils.SystemBarUtils;

/**
 * Activity for exporting and importing chat history.
 *
 * <p>This activity allows users to:
 * <ul>
 *     <li>Select a profile for which to manage history.</li>
 *     <li>Export the chat history of a selected profile to a file.</li>
 *     <li>Import chat history from a file into a selected profile.</li>
 * </ul>
 * It handles interactions with the file system for selecting directories and files,
 * and displays appropriate dialogs for user confirmation and progress indication.
 * The activity also warns the user if any profiles are connected or chats are open,
 * as these operations might require disconnecting profiles and closing chats.
 */
public class ExportImportActivity extends Activity {
    
    public static Dialog CONVERTING_DIALOG;
    @SuppressWarnings("unused")
    public static boolean CONVERTING_STARTED;
    @SuppressWarnings("FieldCanBeLocal")
    private ListView mList;
    private Dialog warning;

    @SuppressLint("ResourceType")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(16973833);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_history);
        SystemBarUtils.setupTransparentBars(this);
        initViews();
        boolean any_profiles_connected = resources.service.profiles.isAnyProfileConnected();
        boolean any_chat_opened = resources.service.isAnyChatOpened;
        if (any_profiles_connected || any_chat_opened) {
            warning = DialogBuilder.createYesNo(
                    this,
                    0,
                    Locale.getString("s_information"),
                    Locale.getString("s_history_tools_warning"),
                    Locale.getString("s_yes"),
                    Locale.getString("s_no"),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            resources.service.profiles.disconnectAll();
                            try {
                                Thread.sleep(1000L);
                            } catch (Exception ignored) {
                            }
                            resources.service.profiles.closeAllChats();
                            warning.dismiss();
                        }
                    },
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            warning.dismiss();
                            finish();
                        }
                    }
            );
            warning.show();
        }
    }

    public final void initViews() {
        ((TextView) findViewById(R.id.l1)).setText(Locale.getString("s_history_tools_header"));
        mList = (ListView) findViewById(R.id.history_export_profiles);
        mList.setDividerHeight(0);
        mList.setSelector(resources.getListSelector());
        UAdapter adp = new UAdapter();
        adp.setTextSize(16);
        adp.setPadding(16);
        Vector<IMProfile> profiles = resources.service.profiles.getProfiles();
        int i = 0;
        for (IMProfile profile : profiles) {
            adp.put(IMProfile.getProfileIcon(profile), IMProfile.getProfileID(profile), i);
            i++;
        }
        mList.setAdapter((ListAdapter) adp);
        mList.setOnItemClickListener(new AnonymousClass3(profiles));
    }


    public class AnonymousClass3 implements AdapterView.OnItemClickListener {
        Dialog d = null;
        @SuppressWarnings("rawtypes")
        private final /* synthetic */ Vector val$profiles;

        AnonymousClass3(@SuppressWarnings("rawtypes") Vector vector) {
            val$profiles = vector;
        }

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            IMProfile profile = (IMProfile) val$profiles.get(arg2);
            UAdapter menu = new UAdapter();
            menu.setMode(2);
            menu.setTextSize(16);
            menu.setPadding(16);
            menu.put(Locale.getString("s_save_history"), 0);
            menu.put(Locale.getString("s_restore_history"), 1);
            d = DialogBuilder.createWithNoHeader(ExportImportActivity.this, menu, 0, new AnonymousClass1(profile));
            d.show();
        }
        
        class AnonymousClass1 implements AdapterView.OnItemClickListener {
            private final /* synthetic */ IMProfile val$profile;
            Dialog selector = null;
            Dialog confirm = null;

            AnonymousClass1(IMProfile iMProfile) {
                val$profile = iMProfile;
            }

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                d.dismiss();
                ListView list = new ListView(ExportImportActivity.this);
                list.setCacheColorHint(0);
                list.setDividerHeight(0);
                list.setSelector(resources.getListSelector());
                switch (arg2) {
                    case 0:
                        FileSelector fs = new FileSelector(list, FileSelector.Mode.SELECT_DIRECTORY, new FileSelector.OnChosedListener() {
                            @Override
                            public void OnChosed(File file) {

                            }
                        });
                        selector = DialogBuilder.createYesNo(
                                ExportImportActivity.this,
                                list,
                                0,
                                Locale.getString("s_save_history"),
                                Locale.getString("s_save_history_here"),
                                Locale.getString("s_cancel"),
                                new AnonymousClass2(fs, val$profile),
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        selector.dismiss();
                                    }
                                },
                                false
                        );
                        selector.show();
                        return;
                    case 1:
                        FileSelector.Mode mode = FileSelector.Mode.SELECT_FILE;
                        final IMProfile iMProfile = val$profile;
                        new FileSelector(list, mode, new FileSelector.OnChosedListener() {
                            @Override
                            public void OnChosed(final File file) {
                                AnonymousClass1 anonymousClass1 = AnonymousClass1.this;
                                ExportImportActivity exportImportActivity = ExportImportActivity.this;
                                String string = Locale.getString("s_restore_history");
                                String string2 = Locale.getString("s_history_restore_warning");
                                String string3 = Locale.getString("s_yes");
                                String string4 = Locale.getString("s_cancel");
                                //noinspection UnnecessaryLocalVariable
                                final IMProfile iMProfile2 = iMProfile;
                                anonymousClass1.confirm = DialogBuilder.createYesNo(exportImportActivity, 0, string, string2, string3, string4, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        final Dialog progress = DialogBuilder.createProgress(ExportImportActivity.this, Locale.getString("s_please_wait"), false);
                                        progress.show();
                                        //noinspection UnnecessaryLocalVariable
                                        final IMProfile iMProfile3 = iMProfile2;
                                        Thread t = new Thread() {
                                            @Override
                                            public void run() {
                                                Import.Result result = Import.getInstance().performImport(file, iMProfile3);
                                                if (result == Import.Result.SUCCESS) {
                                                    resources.service.showToast(Locale.getString("s_history_restored"), 0);
                                                } else if (result == Import.Result.INCORRECT_PROFILE) {
                                                    resources.service.showToast(Locale.getString("s_history_incorrect_profile"), 0);
                                                } else if (result == Import.Result.UNKNOWN_ERROR) {
                                                    resources.service.showToast(Locale.getString("s_history_restore_error"), 0);
                                                }
                                                progress.dismiss();
                                            }
                                        };
                                        t.start();
                                        confirm.dismiss();
                                        selector.dismiss();
                                    }
                                }, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        confirm.dismiss();
                                    }
                                });
                                confirm.show();
                            }
                        });
                        selector = DialogBuilder.createOk(
                                ExportImportActivity.this,
                                list,
                                Locale.getString("s_restore_history"),
                                Locale.getString("s_cancel"),
                                0,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                },
                                true
                        );
                        selector.show();
                        return;
                    default:
                }
            }
            
            class AnonymousClass2 implements View.OnClickListener {
                Dialog name_input = null;
                private final /* synthetic */ FileSelector val$fs;
                private final /* synthetic */ IMProfile val$profile;

                AnonymousClass2(FileSelector fileSelector, IMProfile iMProfile) {
                    val$fs = fileSelector;
                    val$profile = iMProfile;
                }

                @Override
                public void onClick(View arg0) {
                    final String directory = utilities.normalizePath(val$fs.getCurrentDirPath());
                    String archive_name = IMProfile.getProfileID(val$profile) + "_" + utilities.getCurrentDateTimeString();
                    final EditText input = new EditText(ExportImportActivity.this);
                    input.setTextSize(16.0f);
                    input.setText(archive_name);
                    resources.attachEditText(input);
                    ExportImportActivity exportImportActivity = ExportImportActivity.this;
                    String string = Locale.getString("s_history_archive_name");
                    String string2 = Locale.getString("s_ok");
                    String string3 = Locale.getString("s_cancel");
                    final IMProfile iMProfile = val$profile;
                    name_input = DialogBuilder.createYesNo(exportImportActivity, input, 0, string, string2, string3, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String name = input.getText().toString().trim();
                            if (utilities.verifyFileName(name)) {
                                final Dialog progress = DialogBuilder.createProgress(ExportImportActivity.this, Locale.getString("s_please_wait"), false);
                                final File archive = new File(directory + name + ".jha2");
                                if (archive.exists()) {
                                    resources.service.showToast(Locale.getString("s_history_archive_already_exist"), 0);
                                } else {
                                    progress.show();
                                    //noinspection UnnecessaryLocalVariable
                                    final IMProfile iMProfile2 = iMProfile;
                                    Thread t = new Thread() {
                                        @Override
                                        public void run() {
                                            boolean result = Export.getInstance().performExport(archive, iMProfile2);
                                            progress.dismiss();
                                            if (result) {
                                                resources.service.showToast(Locale.getString("s_history_saved"), 0);
                                            } else {
                                                resources.service.showToast(Locale.getString("s_history_export_error"), 0);
                                            }
                                        }
                                    };
                                    t.start();
                                }
                                name_input.dismiss();
                                selector.dismiss();
                                return;
                            }
                            resources.service.showToast(Locale.getString("s_history_invalid_archive_name"), 0);
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            name_input.dismiss();
                        }
                    }, false);
                    name_input.show();
                }
            }
        }
    }
}
