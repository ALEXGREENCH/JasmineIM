package ru.ivansuper.jasmin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Vector;

import ru.ivansuper.jasmin.Service.jasminSvc;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.dialogs.DialogBuilder;
import ru.ivansuper.jasmin.icq.ICQContact;
import ru.ivansuper.jasmin.icq.ICQGroup;
import ru.ivansuper.jasmin.icq.ICQProfile;
import ru.ivansuper.jasmin.icq.InfoContainer;
import ru.ivansuper.jasmin.icq.SearchCriteries;
import ru.ivansuper.jasmin.icq.SearchResultItem;
import ru.ivansuper.jasmin.icq.SearchResultsAdapter;

public class SearchActivity extends Activity implements Handler.Callback {

    @SuppressWarnings("unused")
    public static final int HANDLE_SEARCH_RESULT = 0;
    @SuppressWarnings("unused")
    public static final int SHOW_CONTACT_INFO = 1;
    public static boolean VISIBLE;
    private SearchResultItem context_item;
    private Button do_search;
    private ServiceConnection jasminSvcCnt;
    private ICQProfile profile;
    @SuppressWarnings("FieldCanBeLocal")
    private ListView results;
    private jasminSvc service;
    private Button set_criteries;
    private TextView status;
    private InfoContainer tempContactForDisplayInfo;
    private int viewing_page;
    @SuppressWarnings("FieldMayBeFinal")
    private SearchResultsAdapter adapter = new SearchResultsAdapter();
    @SuppressWarnings("FieldMayBeFinal")
    private SearchCriteries criteries = new SearchCriteries();
    @SuppressWarnings("FieldCanBeLocal")
    private int found_in_database = 0;
    private int page_to_request = 0;
    private int item_per_request = 0;
    @SuppressWarnings("FieldMayBeFinal")
    private Handler searchHdl = new Handler(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getBoolean("ms_sys_wallpaper", false)) {
            setTheme(R.style.WallpaperNoTitleTheme);
        } else {
            setTheme(R.style.BlackNoTitleTheme);
            if (!sp.getBoolean("ms_use_solid_wallpaper", false)) {
                getWindow().setBackgroundDrawable(ColorScheme.getSolid(ColorScheme.getColor(13)));
            } else {
                getWindow().setBackgroundDrawable(resources.custom_wallpaper);
            }
        }
        resources.attachContactlistBack(getWindow());
        setVolumeControlStream(3);
        setContentView(R.layout.search_activity);
        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (service == null) {
            bindToService();
        }
        VISIBLE = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        VISIBLE = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (jasminSvcCnt != null) {
            unbindService(jasminSvcCnt);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog ad = null;
        switch (id) {
            case 0:
                LinearLayout lay = (LinearLayout) View.inflate(this, R.layout.search_criteries_dialog, null);
                ((TextView) lay.findViewById(R.id.l1)).setText(resources.getString("s_search_param_gender"));
                final EditText nick = (EditText) lay.findViewById(R.id.criteries_nick);
                nick.setHint(resources.getString("s_search_param_nick"));
                final EditText name = (EditText) lay.findViewById(R.id.criteries_name);
                name.setHint(resources.getString("s_search_param_name"));
                final EditText lastname = (EditText) lay.findViewById(R.id.criteries_lastname);
                lastname.setHint(resources.getString("s_search_param_surname"));
                final Spinner gender = (Spinner) lay.findViewById(R.id.criteries_gender);
                final EditText city = (EditText) lay.findViewById(R.id.criteries_city);
                city.setHint(resources.getString("s_search_param_city"));
                UAdapter genders = new UAdapter();
                genders.setMode(2);
                genders.setPadding(10);
                genders.setTextColor(-7829368);
                genders.put(resources.getString("s_gender_param_none"), 0);
                genders.put(resources.getString("s_gender_param_woman"), 1);
                genders.put(resources.getString("s_gender_param_man"), 2);
                gender.setAdapter(genders);
                gender.setSelection(criteries.gender);
                nick.setText(criteries.nick);
                name.setText(criteries.name);
                lastname.setText(criteries.lastname);
                city.setText(criteries.city);
                ad = DialogBuilder.createYesNo(this, lay, 48, resources.getString("s_search_params"), resources.getString("s_ok"), resources.getString("s_cancel"), v -> {
                    removeDialog(0);
                    String Nick = nick.getText().toString().trim();
                    String Name = name.getText().toString().trim();
                    String LastName = lastname.getText().toString().trim();
                    String City = city.getText().toString().trim();
                    int gnd = gender.getSelectedItemPosition();
                    SearchCriteries searchCriteries = criteries;
                    if (gnd == -1) {
                        gnd = 0;
                    }
                    searchCriteries.gender = gnd;
                    criteries.nick = Nick;
                    criteries.name = Name;
                    criteries.lastname = LastName;
                    criteries.city = City;
                    page_to_request = 0;
                    do_search.setText(resources.getString("s_search"));
                    if (isSearchAvailable()) {
                        do_search.setEnabled(true);
                        status.setText(resources.getString("s_press_search_button"));
                        return;
                    }
                    do_search.setEnabled(false);
                    status.setText(resources.getString("s_search_without_params_error"));
                }, v -> removeDialog(0));
                break;
            case 1:
                final UAdapter adp = new UAdapter();
                adp.setMode(2);
                adp.setTextSize(18);
                adp.setPadding(15);
                adp.put(resources.getString("s_copy_uin"), 0);
                adp.put(resources.getString("s_contact_info"), 1);
                adp.put(resources.getString("s_add_contact"), 2);
                ad = DialogBuilder.createWithNoHeader(this, adp, 48, (arg0, arg1, arg2, arg3) -> {
                    if (context_item != null) {
                        removeDialog(1);
                        switch ((int) adp.getItemId(arg2)) {
                            case 0:
                                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                cm.setText(context_item.uin);
                                Toast.makeText(SearchActivity.this, resources.getString("s_copied"), Toast.LENGTH_SHORT).show();
                                return;
                            case 1:
                                profile.doRequestContactInfoForDisplayInSearch(context_item.uin);
                                return;
                            case 2:
                                removeDialog(3);
                                showDialog(3);
                                return;
                            default:
                        }
                    }
                });
                break;
            case 2:
                if (tempContactForDisplayInfo != null) {
                    LinearLayout info_lay = (LinearLayout) View.inflate(this, R.layout.vcard, null);
                    final ImageView vcard_avatar = (ImageView) info_lay.findViewById(R.id.vcard_avatar);
                    EditText vcard_desc = (EditText) info_lay.findViewById(R.id.vcard_desc);
                    final String data = resources.getString("s_icq_info_nick") + ": " + tempContactForDisplayInfo.nickname + "\n" + resources.getString("s_icq_info_name") + ": " + tempContactForDisplayInfo.name + "\n" + resources.getString("s_icq_info_surname") + ": " + tempContactForDisplayInfo.surname + "\n" + resources.getString("s_icq_info_city") + ": " + tempContactForDisplayInfo.city + "\n\n" + resources.getString("s_icq_info_birthdate") + ": " + tempContactForDisplayInfo.birthday + "/" + tempContactForDisplayInfo.birthmonth + "/" + tempContactForDisplayInfo.birthyear + "\n" + resources.getString("s_icq_info_age") + ": " + tempContactForDisplayInfo.age + "\n" + resources.getString("s_icq_info_gender") + ": " + tempContactForDisplayInfo.sex + "\n\n" + resources.getString("s_icq_info_homepage") + "\n" + tempContactForDisplayInfo.homepage + "\n\nE-Mail:\n" + tempContactForDisplayInfo.email + "\n\n" + resources.getString("s_icq_info_about") + "\n" + tempContactForDisplayInfo.about;
                    vcard_avatar.setImageDrawable(tempContactForDisplayInfo.avatar);
                    tempContactForDisplayInfo.setRedirect((object, args) -> {
                        //noinspection UnnecessaryLocalVariable
                        ImageView imageView = vcard_avatar;
                        //noinspection UnnecessaryLocalVariable
                        final ImageView imageView2 = vcard_avatar;
                        imageView.post(() -> imageView2.setImageDrawable((Drawable) object));
                    });
                    vcard_desc.setText(data);
                    vcard_desc.setFilters(new InputFilter[]{(source, arg1, arg2, dest, dstart, dend) -> source.length() < 1 ? dest.subSequence(dstart, dend) : ""});
                    //noinspection deprecation
                    ad = DialogBuilder.createYesNo(this, info_lay, 48, resources.getString("s_icq_info") + "[" + tempContactForDisplayInfo.uin + "]", resources.getString("s_copy"), resources.getString("s_close"), v -> {
                        ClipboardManager cb = (ClipboardManager) service.getSystemService(Context.CLIPBOARD_SERVICE);
                        cb.setText(data);
                        Toast.makeText(service, resources.getString("s_copied"), Toast.LENGTH_SHORT).show();
                        //noinspection deprecation
                        removeDialog(2);
                    }, v -> removeDialog(2));
                    break;
                } else {
                    return null;
                }
            case 3:
                UAdapter adp1 = new UAdapter();
                adp1.setMode(2);
                adp1.setTextSize(18);
                adp1.setTextColor(-7829368);
                adp1.setPadding(15);
                Vector<ICQGroup> groups = profile.contactlist.getGroups(false);
                for (ICQGroup group : groups) {
                    adp1.put(group.name, group.id);
                }
                final LinearLayout lay1 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.add_contact_dialog, (ViewGroup) null);
                if (context_item != null) {
                    @SuppressLint("CutPasteId")
                    EditText uin = (EditText) lay1.findViewById(R.id.add_contact_uin);
                    uin.setText(context_item.uin);
                    @SuppressLint("CutPasteId")
                    EditText name1 = (EditText) lay1.findViewById(R.id.add_contact_name);
                    name1.setText(context_item.nick);
                    name1.requestFocus();
                }
                @SuppressLint("CutPasteId")
                Spinner spn = (Spinner) lay1.findViewById(R.id.add_contact_groups);
                spn.setAdapter((SpinnerAdapter) adp1);
                //noinspection deprecation
                ad = DialogBuilder.createYesNo(this, lay1, 48, resources.getString("s_add_contact"), resources.getString("s_ok"), resources.getString("s_cancel"), v -> {
                    @SuppressLint("CutPasteId")
                    EditText uin2 = (EditText) lay1.findViewById(R.id.add_contact_uin);
                    resources.attachEditText(uin2);
                    String sUIN = uin2.getText().toString();
                    if (sUIN.length() < 4) {
                        Toast.makeText(SearchActivity.this, resources.getString("s_incorrect_uin"), Toast.LENGTH_SHORT).show();
                    } else if (!utilities.isUIN(sUIN)) {
                        Toast.makeText(SearchActivity.this, resources.getString("s_incorrect_uin"), Toast.LENGTH_SHORT).show();
                    } else {
                        @SuppressLint("CutPasteId")
                        EditText name2 = (EditText) lay1.findViewById(R.id.add_contact_name);
                        resources.attachEditText(name2);
                        String sNAME = name2.getText().toString();
                        if (sNAME.length() == 0) {
                            sNAME = sUIN;
                        }
                        @SuppressLint("CutPasteId")
                        Spinner spn2 = (Spinner) lay1.findViewById(R.id.add_contact_groups);
                        UAdapter adp2 = (UAdapter) spn2.getAdapter();
                        int groupId = (int) adp2.getItemId(spn2.getSelectedItemPosition());
                        ICQContact contact = new ICQContact();
                        contact.ID = sUIN;
                        contact.name = sNAME;
                        contact.group = groupId;
                        contact.id = utilities.getRandomSSIId();
                        contact.profile = profile;
                        contact.init();
                        try {
                            profile.doAddContact(contact, 0);
                        } catch (Exception e) {
                            Toast.makeText(SearchActivity.this, resources.getString("s_icq_contact_add_error"), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        //noinspection deprecation
                        removeDialog(3);
                    }
                }, v -> removeDialog(3));
                break;
        }
        return ad;
    }

    private void bindToService() {
        jasminSvcCnt = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName arg0, IBinder arg1) {
                service = ((jasminSvc.itf) arg1).getService();
                handleServiceConnected();
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                finish();
            }
        };
        Intent svc = new Intent();
        svc.setClass(this, jasminSvc.class);
        bindService(svc, jasminSvcCnt, 0);
    }

    public void handleServiceConnected() {
        service.searchHdl = searchHdl;
        Intent i = getIntent();
        String action = i.getAction();
        if (action != null) {
            profile = service.profiles.getProfileByUIN(action.trim());
        }
    }

    private void initViews() {
        set_criteries = (Button) findViewById(R.id.set_criteries_btn);
        set_criteries.setText(resources.getString("s_search_set_params_btn"));
        resources.attachButtonStyle(set_criteries);
        set_criteries.setOnClickListener(v -> {
            //noinspection deprecation
            removeDialog(0);
            //noinspection deprecation
            showDialog(0);
        });
        do_search = (Button) findViewById(R.id.do_search_btn);
        do_search.setText(resources.getString("s_search"));
        resources.attachButtonStyle(do_search);
        do_search.setOnClickListener(v -> {
            adapter.clear();
            item_per_request = 0;
            status.setText(resources.getString("s_search_in_progress"));
            sendRequest();
        });
        do_search.setEnabled(false);
        results = (ListView) findViewById(R.id.search_results_list);
        results.setSelector(resources.getListSelector());
        results.setAdapter((ListAdapter) adapter);
        results.setOnItemClickListener((arg0, arg1, pos, arg3) -> {
            context_item = adapter.getItem(pos);
            //noinspection deprecation
            removeDialog(1);
            //noinspection deprecation
            showDialog(1);
        });
        status = (TextView) findViewById(R.id.search_status);
        status.setText(resources.getString("s_search_instruction"));
    }

    public void sendRequest() {
        criteries.page = page_to_request;
        viewing_page = page_to_request + 1;
        profile.sendSearchRequest(criteries);
        set_criteries.setEnabled(false);
        do_search.setEnabled(false);
    }

    public boolean isSearchAvailable() {
        return criteries.nick.length() > 0 || criteries.name.length() > 0 || criteries.lastname.length() > 0 || criteries.gender > 0 || criteries.city.length() > 0;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 0:
                SearchResultItem item = (SearchResultItem) msg.obj;
                found_in_database = item.found_in_database;
                if (found_in_database == 0) {
                    status.setText(resources.getString("s_search_no_results"));
                    set_criteries.setEnabled(true);
                    do_search.setEnabled(true);
                    break;
                } else {
                    adapter.put(item);
                    item_per_request++;
                    if (item.isLast) {
                        status.setText(utilities.match(resources.getString("s_search_status_bar"), new String[]{String.valueOf(viewing_page), String.valueOf(item.pages_available), String.valueOf(item_per_request)}));
                        if (viewing_page < item.pages_available) {
                            do_search.setText(resources.getString("s_search_view_more"));
                            page_to_request++;
                        } else {
                            do_search.setText(resources.getString("s_search_restart"));
                            page_to_request = 0;
                        }
                        set_criteries.setEnabled(true);
                        do_search.setEnabled(true);
                        break;
                    }
                }
                break;
            case 1:
                tempContactForDisplayInfo = (InfoContainer) msg.obj;
                if (tempContactForDisplayInfo != null) {
                    //noinspection deprecation
                    removeDialog(2);
                    //noinspection deprecation
                    showDialog(2);
                    break;
                }
                break;
        }
        return false;
    }
}
