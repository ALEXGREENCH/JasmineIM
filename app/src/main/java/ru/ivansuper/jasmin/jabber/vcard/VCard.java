package ru.ivansuper.jasmin.jabber.vcard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import ru.ivansuper.jasmin.Base64Coder;
import ru.ivansuper.jasmin.R;
import ru.ivansuper.jasmin.color_editor.ColorScheme;
import ru.ivansuper.jasmin.jabber.XML_ENGINE.Node;
import ru.ivansuper.jasmin.locale.Locale;
import ru.ivansuper.jasmin.resources;

/**
 * Represents a vCard, which is a file format standard for electronic business cards.
 * This class provides methods to create, manage, and compile vCard data,
 * including personal information, contact details, and an avatar image.
 * It also includes functionality to interact with an Android UI for editing vCard fields.
 *
 * <p>The class uses an inner {@link Entry.Type} enum to define the different types of
 * information that can be stored in a vCard, such as name, address, phone numbers, email, etc.
 *
 * <p>It maintains two sets of fields: {@code fields} for the current vCard data and
 * {@code temp_fields} for temporary storage during editing operations.
 *
 * <p>Key functionalities include:
 * <ul>
 *     <li>Getting and setting vCard entries.
 *     <li>Compiling the vCard data into an XML {@link Node} structure.
 *     <li>Preparing an Android {@link LinearLayout} editor for vCard fields.
 *     <li>Populating the editor with existing vCard data.
 *     <li>Reading data from the editor into the temporary fields.
 *     <li>Applying temporary changes to the main vCard data.
 * </ul>
 */
public class VCard {
    public Bitmap avatar;
    private final HashMap<Entry.Type, String> fields = new HashMap<>();
    private final HashMap<Entry.Type, String> temp_fields = new HashMap<>();

    public static class Entry {

        public enum Type {
            FN,
            N_FAMILY,
            N_GIVEN,
            N_MIDDLE,
            NICKNAME,
            URL,
            BDAY,
            ORGNAME,
            ORGUNIT,
            TITLE,
            ROLE,
            TEL_W_PHONE,
            TEL_W_FAX,
            TEL_W_MSG,
            TEL_H_PHONE,
            TEL_H_FAX,
            TEL_H_MSG,
            W_ADR_EXTADD,
            W_ADR_STREET,
            W_ADR_LOCALITY,
            W_ADR_REGION,
            W_ADR_PCODE,
            W_ADR_CTRY,
            H_ADR_EXTADD,
            H_ADR_STREET,
            H_ADR_LOCALITY,
            H_ADR_REGION,
            H_ADR_PCODE,
            H_ADR_CTRY,
            EMAIL,
            JABBERID,
            DESC,
            TZ;

            /** @noinspection unused*/ /* renamed from: values, reason: to resolve conflict with enum method */
            public static Type[] valuesCustom() {
                Type[] valuesCustom = values();
                int length = valuesCustom.length;
                Type[] typeArr = new Type[length];
                System.arraycopy(valuesCustom, 0, typeArr, 0, length);
                return typeArr;
            }
        }
    }

    public static VCard getInstance() {
        return new VCard();
    }

    private VCard() {
        this.fields.put(Entry.Type.FN, "");
        this.fields.put(Entry.Type.N_FAMILY, "");
        this.fields.put(Entry.Type.N_GIVEN, "");
        this.fields.put(Entry.Type.N_MIDDLE, "");
        this.fields.put(Entry.Type.NICKNAME, "");
        this.fields.put(Entry.Type.URL, "");
        this.fields.put(Entry.Type.BDAY, "");
        this.fields.put(Entry.Type.ORGNAME, "");
        this.fields.put(Entry.Type.ORGUNIT, "");
        this.fields.put(Entry.Type.TITLE, "");
        this.fields.put(Entry.Type.ROLE, "");
        this.fields.put(Entry.Type.TEL_W_PHONE, "");
        this.fields.put(Entry.Type.TEL_W_FAX, "");
        this.fields.put(Entry.Type.TEL_W_MSG, "");
        this.fields.put(Entry.Type.TEL_H_PHONE, "");
        this.fields.put(Entry.Type.TEL_H_FAX, "");
        this.fields.put(Entry.Type.TEL_H_MSG, "");
        this.fields.put(Entry.Type.W_ADR_EXTADD, "");
        this.fields.put(Entry.Type.W_ADR_STREET, "");
        this.fields.put(Entry.Type.W_ADR_LOCALITY, "");
        this.fields.put(Entry.Type.W_ADR_REGION, "");
        this.fields.put(Entry.Type.W_ADR_PCODE, "");
        this.fields.put(Entry.Type.W_ADR_CTRY, "");
        this.fields.put(Entry.Type.H_ADR_EXTADD, "");
        this.fields.put(Entry.Type.H_ADR_STREET, "");
        this.fields.put(Entry.Type.H_ADR_LOCALITY, "");
        this.fields.put(Entry.Type.H_ADR_REGION, "");
        this.fields.put(Entry.Type.H_ADR_PCODE, "");
        this.fields.put(Entry.Type.H_ADR_CTRY, "");
        this.fields.put(Entry.Type.EMAIL, "");
        this.fields.put(Entry.Type.JABBERID, "");
        this.fields.put(Entry.Type.DESC, "");
        this.fields.put(Entry.Type.TZ, "");
        this.temp_fields.putAll(this.fields);
    }

    public final String getEntry(Entry.Type entry) {
        return this.fields.get(entry);
    }

    /** @noinspection unused*/
    public final void putEntry(Entry.Type entry, String value) {
        if (value == null) {
            value = "";
        }
        this.fields.put(entry, value.trim());
    }

    public final void putEntryTemp(Entry.Type entry, String value) {
        if (value == null) {
            value = "";
        }
        this.temp_fields.put(entry, value.trim());
    }

    public final void fillFromTemp() {
        this.fields.clear();
        this.fields.putAll(this.temp_fields);
        this.temp_fields.clear();
    }

    public final Node compile(Bitmap avatar) {
        return compile(avatar, this.fields);
    }

    public final Node compileTemp(Bitmap avatar) {
        return compile(avatar, this.temp_fields);
    }

    private Node compile(Bitmap avatar, HashMap<Entry.Type, String> fields_) {
        Node vcard = new Node("vCard", "", "vcard-temp");
        vcard.putChild(Node.getInstance("FN", fields_.get(Entry.Type.FN)));
        Node N = new Node("N");
        N.putChild(Node.getInstance("FAMILY", fields_.get(Entry.Type.N_FAMILY)));
        N.putChild(Node.getInstance("GIVEN", fields_.get(Entry.Type.N_GIVEN)));
        N.putChild(Node.getInstance("MIDDLE", fields_.get(Entry.Type.N_MIDDLE)));
        vcard.putChild(N);
        vcard.putChild(Node.getInstance("NICKNAME", fields_.get(Entry.Type.NICKNAME)));
        vcard.putChild(Node.getInstance("URL", fields_.get(Entry.Type.URL)));
        vcard.putChild(Node.getInstance("BDAY", fields_.get(Entry.Type.BDAY)));
        Node ORG = new Node("ORG");
        ORG.putChild(Node.getInstance("ORGNAME", fields_.get(Entry.Type.ORGNAME)));
        ORG.putChild(Node.getInstance("ORGUNIT", fields_.get(Entry.Type.ORGUNIT)));
        vcard.putChild(ORG);
        vcard.putChild(Node.getInstance("TITLE", fields_.get(Entry.Type.TITLE)));
        vcard.putChild(Node.getInstance("ROLE", fields_.get(Entry.Type.ROLE)));
        Node TEL = new Node("TEL");
        TEL.putChild(Node.getInstance("WORK"), Node.getInstance("VOICE"), Node.getInstance("NUMBER", fields_.get(Entry.Type.TEL_W_PHONE)));
        vcard.putChild(TEL);
        Node TEL2 = new Node("TEL");
        TEL2.putChild(Node.getInstance("WORK"), Node.getInstance("FAX"), Node.getInstance("NUMBER", fields_.get(Entry.Type.TEL_W_FAX)));
        vcard.putChild(TEL2);
        Node TEL3 = new Node("TEL");
        TEL3.putChild(Node.getInstance("WORK"), Node.getInstance("MSG"), Node.getInstance("NUMBER", fields_.get(Entry.Type.TEL_W_MSG)));
        vcard.putChild(TEL3);
        Node TEL4 = new Node("TEL");
        TEL4.putChild(Node.getInstance("HOME"), Node.getInstance("VOICE"), Node.getInstance("NUMBER", fields_.get(Entry.Type.TEL_H_PHONE)));
        vcard.putChild(TEL4);
        Node TEL5 = new Node("TEL");
        TEL5.putChild(Node.getInstance("HOME"), Node.getInstance("FAX"), Node.getInstance("NUMBER", fields_.get(Entry.Type.TEL_H_FAX)));
        vcard.putChild(TEL5);
        Node TEL6 = new Node("TEL");
        TEL6.putChild(Node.getInstance("HOME"), Node.getInstance("MSG"), Node.getInstance("NUMBER", fields_.get(Entry.Type.TEL_H_MSG)));
        vcard.putChild(TEL6);
        Node ADR = new Node("ADR");
        ADR.putChild(Node.getInstance("WORK"), Node.getInstance("EXTADD", fields_.get(Entry.Type.W_ADR_EXTADD)), Node.getInstance("STREET", fields_.get(Entry.Type.W_ADR_STREET)), Node.getInstance("LOCALITY", fields_.get(Entry.Type.W_ADR_LOCALITY)), Node.getInstance("REGION", fields_.get(Entry.Type.W_ADR_REGION)), Node.getInstance("PCODE", fields_.get(Entry.Type.W_ADR_PCODE)), Node.getInstance("CTRY", fields_.get(Entry.Type.W_ADR_CTRY)));
        vcard.putChild(ADR);
        Node ADR2 = new Node("ADR");
        ADR2.putChild(Node.getInstance("HOME"), Node.getInstance("EXTADD", fields_.get(Entry.Type.H_ADR_EXTADD)), Node.getInstance("STREET", fields_.get(Entry.Type.H_ADR_STREET)), Node.getInstance("LOCALITY", fields_.get(Entry.Type.H_ADR_LOCALITY)), Node.getInstance("REGION", fields_.get(Entry.Type.H_ADR_REGION)), Node.getInstance("PCODE", fields_.get(Entry.Type.H_ADR_PCODE)), Node.getInstance("CTRY", fields_.get(Entry.Type.H_ADR_CTRY)));
        vcard.putChild(ADR2);
        Node EMAIL = new Node("EMAIL");
        EMAIL.putChild(Node.getInstance("USERID", fields_.get(Entry.Type.EMAIL)));
        vcard.putChild(EMAIL);
        vcard.putChild(Node.getInstance("JABBERID", fields_.get(Entry.Type.JABBERID)));
        vcard.putChild(Node.getInstance("TZ", fields_.get(Entry.Type.TZ)));
        vcard.putChild(Node.getInstance("DESC", fields_.get(Entry.Type.DESC)));
        if (avatar != null) {
            Node PHOTO = new Node("PHOTO");
            Node TYPE = new Node("TYPE", "image/png");
            Node BINVAL = new Node("BINVAL");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            avatar.compress(Bitmap.CompressFormat.PNG, 100, baos);
            BINVAL.setValue(Base64Coder.encodeLines(baos.toByteArray()));
            PHOTO.putChild(TYPE, BINVAL);
            vcard.putChild(PHOTO);
        }
        return vcard;
    }

    @SuppressLint("CutPasteId")
    public static LinearLayout prepareEditor(Context context) {
        LinearLayout lay = (LinearLayout) View.inflate(context, R.layout.jabber_vcard_form, null);
        TextView l = lay.findViewById(R.id.l1);
        l.setText(Locale.getString("s_jabber_vcard_editor_fullname"));
        l.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(44), 2));
        TextView l2 = lay.findViewById(R.id.l2);
        l2.setText(Locale.getString("s_jabber_vcard_editor_family"));
        l2.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(44), 3));
        TextView l3 = lay.findViewById(R.id.l3);
        l3.setText(Locale.getString("s_jabber_vcard_editor_given"));
        l3.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(44), 3));
        TextView l4 = lay.findViewById(R.id.l4);
        l4.setText(Locale.getString("s_jabber_vcard_editor_middle"));
        l4.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(44), 3));
        TextView l5 = lay.findViewById(R.id.l5);
        l5.setText(Locale.getString("s_jabber_vcard_editor_nickname"));
        l5.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(44), 2));
        TextView l6 = lay.findViewById(R.id.l6);
        l6.setText(Locale.getString("s_jabber_vcard_editor_web"));
        l6.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(44), 2));
        TextView l7 = lay.findViewById(R.id.l65);
        l7.setText(Locale.getString("s_jabber_vcard_editor_birthday"));
        l7.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(44), 2));
        TextView l8 = lay.findViewById(R.id.l7);
        l8.setText(Locale.getString("s_jabber_vcard_editor_orgname"));
        l8.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(44), 2));
        TextView l9 = lay.findViewById(R.id.l8);
        l9.setText(Locale.getString("s_jabber_vcard_editor_orgunit"));
        l9.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(44), 2));
        TextView l10 = lay.findViewById(R.id.l9);
        l10.setText(Locale.getString("s_jabber_vcard_editor_title"));
        l10.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(44), 2));
        TextView l11 = lay.findViewById(R.id.l10);
        l11.setText(Locale.getString("s_jabber_vcard_editor_role"));
        l11.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(44), 2));
        TextView l12 = lay.findViewById(R.id.l11);
        l12.setText(Locale.getString("s_jabber_vcard_editor_work_phones"));
        l12.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(44), 2));
        TextView l13 = lay.findViewById(R.id.l12);
        l13.setText(Locale.getString("s_jabber_vcard_editor_voice"));
        l13.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(44), 3));
        TextView l14 = lay.findViewById(R.id.l13);
        l14.setText(Locale.getString("s_jabber_vcard_editor_fax"));
        l14.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(44), 3));
        TextView l15 = lay.findViewById(R.id.l14);
        l15.setText(Locale.getString("s_jabber_vcard_editor_msg"));
        l15.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(44), 3));
        TextView l16 = lay.findViewById(R.id.l15);
        l16.setText(Locale.getString("s_jabber_vcard_editor_home_phones"));
        l16.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(44), 2));
        TextView l17 = lay.findViewById(R.id.l16);
        l17.setText(Locale.getString("s_jabber_vcard_editor_voice"));
        l17.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(44), 3));
        TextView l18 = lay.findViewById(R.id.l17);
        l18.setText(Locale.getString("s_jabber_vcard_editor_fax"));
        l18.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(44), 3));
        TextView l19 = lay.findViewById(R.id.l18);
        l19.setText(Locale.getString("s_jabber_vcard_editor_msg"));
        l19.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(44), 3));
        TextView l20 = lay.findViewById(R.id.l19);
        l20.setText(Locale.getString("s_jabber_vcard_editor_work_address"));
        l20.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(44), 2));
        TextView l21 = lay.findViewById(R.id.l20);
        l21.setText(Locale.getString("s_jabber_vcard_editor_home_address"));
        l21.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(44), 2));
        TextView l22 = lay.findViewById(R.id.l21);
        l22.setText(Locale.getString("s_jabber_vcard_editor_email"));
        l22.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(44), 2));
        TextView l23 = lay.findViewById(R.id.l22);
        l23.setText(Locale.getString("s_jabber_vcard_editor_jabberid"));
        l23.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(44), 2));
        TextView l24 = lay.findViewById(R.id.l23);
        l24.setText(Locale.getString("s_jabber_vcard_editor_desc"));
        l24.setBackgroundColor(ColorScheme.divideAlpha(ColorScheme.getColor(44), 2));
        EditText e = lay.findViewById(R.id.vcard_w_addr_1);
        e.setHint(Locale.getString("s_jabber_vcard_editor_extadd"));
        EditText e2 = lay.findViewById(R.id.vcard_w_addr_2);
        e2.setHint(Locale.getString("s_jabber_vcard_editor_street"));
        EditText e3 = lay.findViewById(R.id.vcard_w_addr_3);
        e3.setHint(Locale.getString("s_jabber_vcard_editor_locality"));
        EditText e4 = lay.findViewById(R.id.vcard_w_addr_4);
        e4.setHint(Locale.getString("s_jabber_vcard_editor_region"));
        EditText e5 = lay.findViewById(R.id.vcard_w_addr_5);
        e5.setHint(Locale.getString("s_jabber_vcard_editor_pcode"));
        EditText e6 = lay.findViewById(R.id.vcard_w_addr_6);
        e6.setHint(Locale.getString("s_jabber_vcard_editor_country"));
        EditText e7 = lay.findViewById(R.id.vcard_h_addr_1);
        e7.setHint(Locale.getString("s_jabber_vcard_editor_extadd"));
        EditText e8 = lay.findViewById(R.id.vcard_h_addr_2);
        e8.setHint(Locale.getString("s_jabber_vcard_editor_street"));
        EditText e9 = lay.findViewById(R.id.vcard_h_addr_3);
        e9.setHint(Locale.getString("s_jabber_vcard_editor_locality"));
        EditText e10 = lay.findViewById(R.id.vcard_h_addr_4);
        e10.setHint(Locale.getString("s_jabber_vcard_editor_region"));
        EditText e11 = lay.findViewById(R.id.vcard_h_addr_5);
        e11.setHint(Locale.getString("s_jabber_vcard_editor_pcode"));
        EditText e12 = lay.findViewById(R.id.vcard_h_addr_6);
        e12.setHint(Locale.getString("s_jabber_vcard_editor_country"));
        EditText e13 = lay.findViewById(R.id.vcard_fn);
        resources.attachEditText(e13);
        EditText e14 = lay.findViewById(R.id.vcard_family);
        resources.attachEditText(e14);
        EditText e15 = lay.findViewById(R.id.vcard_given);
        resources.attachEditText(e15);
        EditText e16 = lay.findViewById(R.id.vcard_middle);
        resources.attachEditText(e16);
        EditText e17 = lay.findViewById(R.id.vcard_nick);
        resources.attachEditText(e17);
        EditText e18 = lay.findViewById(R.id.vcard_url);
        resources.attachEditText(e18);
        EditText e19 = lay.findViewById(R.id.vcard_bday);
        resources.attachEditText(e19);
        EditText e20 = lay.findViewById(R.id.vcard_orgname);
        resources.attachEditText(e20);
        EditText e21 = lay.findViewById(R.id.vcard_orgunit);
        resources.attachEditText(e21);
        EditText e22 = lay.findViewById(R.id.vcard_title);
        resources.attachEditText(e22);
        EditText e23 = lay.findViewById(R.id.vcard_role);
        resources.attachEditText(e23);
        EditText e24 = lay.findViewById(R.id.vcard_w_tel_1);
        resources.attachEditText(e24);
        EditText e25 = lay.findViewById(R.id.vcard_w_tel_2);
        resources.attachEditText(e25);
        EditText e26 = lay.findViewById(R.id.vcard_w_tel_3);
        resources.attachEditText(e26);
        EditText e27 = lay.findViewById(R.id.vcard_h_tel_1);
        resources.attachEditText(e27);
        EditText e28 = lay.findViewById(R.id.vcard_h_tel_2);
        resources.attachEditText(e28);
        EditText e29 = lay.findViewById(R.id.vcard_h_tel_3);
        resources.attachEditText(e29);
        EditText e30 = lay.findViewById(R.id.vcard_w_addr_1);
        resources.attachEditText(e30);
        EditText e31 = lay.findViewById(R.id.vcard_w_addr_2);
        resources.attachEditText(e31);
        EditText e32 = lay.findViewById(R.id.vcard_w_addr_3);
        resources.attachEditText(e32);
        EditText e33 = lay.findViewById(R.id.vcard_w_addr_4);
        resources.attachEditText(e33);
        EditText e34 = lay.findViewById(R.id.vcard_w_addr_5);
        resources.attachEditText(e34);
        EditText e35 = lay.findViewById(R.id.vcard_w_addr_6);
        resources.attachEditText(e35);
        EditText e36 = lay.findViewById(R.id.vcard_h_addr_1);
        resources.attachEditText(e36);
        EditText e37 = lay.findViewById(R.id.vcard_h_addr_2);
        resources.attachEditText(e37);
        EditText e38 = lay.findViewById(R.id.vcard_h_addr_3);
        resources.attachEditText(e38);
        EditText e39 = lay.findViewById(R.id.vcard_h_addr_4);
        resources.attachEditText(e39);
        EditText e40 = lay.findViewById(R.id.vcard_h_addr_5);
        resources.attachEditText(e40);
        EditText e41 = lay.findViewById(R.id.vcard_h_addr_6);
        resources.attachEditText(e41);
        EditText e42 = lay.findViewById(R.id.vcard_email);
        resources.attachEditText(e42);
        EditText e43 = lay.findViewById(R.id.vcard_jabberid);
        resources.attachEditText(e43);
        EditText e44 = lay.findViewById(R.id.vcard_desc);
        resources.attachEditText(e44);
        return lay;
    }

    public final void fillFields(LinearLayout form) {
        EditText e = form.findViewById(R.id.vcard_fn);
        e.setText(getEntry(Entry.Type.FN));
        EditText e2 = form.findViewById(R.id.vcard_family);
        e2.setText(getEntry(Entry.Type.N_FAMILY));
        EditText e3 = form.findViewById(R.id.vcard_given);
        e3.setText(getEntry(Entry.Type.N_GIVEN));
        EditText e4 = form.findViewById(R.id.vcard_middle);
        e4.setText(getEntry(Entry.Type.N_MIDDLE));
        EditText e5 = form.findViewById(R.id.vcard_nick);
        e5.setText(getEntry(Entry.Type.NICKNAME));
        EditText e6 = form.findViewById(R.id.vcard_url);
        e6.setText(getEntry(Entry.Type.URL));
        EditText e7 = form.findViewById(R.id.vcard_bday);
        e7.setText(getEntry(Entry.Type.BDAY));
        EditText e8 = form.findViewById(R.id.vcard_orgname);
        e8.setText(getEntry(Entry.Type.ORGNAME));
        EditText e9 = form.findViewById(R.id.vcard_orgunit);
        e9.setText(getEntry(Entry.Type.ORGUNIT));
        EditText e10 = form.findViewById(R.id.vcard_title);
        e10.setText(getEntry(Entry.Type.TITLE));
        EditText e11 = form.findViewById(R.id.vcard_role);
        e11.setText(getEntry(Entry.Type.ROLE));
        EditText e12 = form.findViewById(R.id.vcard_w_tel_1);
        e12.setText(getEntry(Entry.Type.TEL_W_PHONE));
        EditText e13 = form.findViewById(R.id.vcard_w_tel_2);
        e13.setText(getEntry(Entry.Type.TEL_W_FAX));
        EditText e14 = form.findViewById(R.id.vcard_w_tel_3);
        e14.setText(getEntry(Entry.Type.TEL_W_MSG));
        EditText e15 = form.findViewById(R.id.vcard_h_tel_1);
        e15.setText(getEntry(Entry.Type.TEL_H_PHONE));
        EditText e16 = form.findViewById(R.id.vcard_h_tel_2);
        e16.setText(getEntry(Entry.Type.TEL_H_FAX));
        EditText e17 = form.findViewById(R.id.vcard_h_tel_3);
        e17.setText(getEntry(Entry.Type.TEL_H_MSG));
        EditText e18 = form.findViewById(R.id.vcard_w_addr_1);
        e18.setText(getEntry(Entry.Type.W_ADR_EXTADD));
        EditText e19 = form.findViewById(R.id.vcard_w_addr_2);
        e19.setText(getEntry(Entry.Type.W_ADR_STREET));
        EditText e20 = form.findViewById(R.id.vcard_w_addr_3);
        e20.setText(getEntry(Entry.Type.W_ADR_LOCALITY));
        EditText e21 = form.findViewById(R.id.vcard_w_addr_4);
        e21.setText(getEntry(Entry.Type.W_ADR_REGION));
        EditText e22 = form.findViewById(R.id.vcard_w_addr_5);
        e22.setText(getEntry(Entry.Type.W_ADR_PCODE));
        EditText e23 = form.findViewById(R.id.vcard_w_addr_6);
        e23.setText(getEntry(Entry.Type.W_ADR_CTRY));
        EditText e24 = form.findViewById(R.id.vcard_h_addr_1);
        e24.setText(getEntry(Entry.Type.H_ADR_EXTADD));
        EditText e25 = form.findViewById(R.id.vcard_h_addr_2);
        e25.setText(getEntry(Entry.Type.H_ADR_STREET));
        EditText e26 = form.findViewById(R.id.vcard_h_addr_3);
        e26.setText(getEntry(Entry.Type.H_ADR_LOCALITY));
        EditText e27 = form.findViewById(R.id.vcard_h_addr_4);
        e27.setText(getEntry(Entry.Type.H_ADR_REGION));
        EditText e28 = form.findViewById(R.id.vcard_h_addr_5);
        e28.setText(getEntry(Entry.Type.H_ADR_PCODE));
        EditText e29 = form.findViewById(R.id.vcard_h_addr_6);
        e29.setText(getEntry(Entry.Type.H_ADR_CTRY));
        EditText e30 = form.findViewById(R.id.vcard_email);
        e30.setText(getEntry(Entry.Type.EMAIL));
        EditText e31 = form.findViewById(R.id.vcard_jabberid);
        e31.setText(getEntry(Entry.Type.JABBERID));
        EditText e32 = form.findViewById(R.id.vcard_desc);
        e32.setText(getEntry(Entry.Type.DESC));
    }

    public final void readFieldsTemporary(LinearLayout form) {
        EditText e = form.findViewById(R.id.vcard_fn);
        putEntryTemp(Entry.Type.FN, e.getText().toString());
        EditText e2 = form.findViewById(R.id.vcard_family);
        putEntryTemp(Entry.Type.N_FAMILY, e2.getText().toString());
        EditText e3 = form.findViewById(R.id.vcard_given);
        putEntryTemp(Entry.Type.N_GIVEN, e3.getText().toString());
        EditText e4 = form.findViewById(R.id.vcard_middle);
        putEntryTemp(Entry.Type.N_MIDDLE, e4.getText().toString());
        EditText e5 = form.findViewById(R.id.vcard_nick);
        putEntryTemp(Entry.Type.NICKNAME, e5.getText().toString());
        EditText e6 = form.findViewById(R.id.vcard_url);
        putEntryTemp(Entry.Type.URL, e6.getText().toString());
        EditText e7 = form.findViewById(R.id.vcard_bday);
        putEntryTemp(Entry.Type.BDAY, e7.getText().toString());
        EditText e8 = form.findViewById(R.id.vcard_orgname);
        putEntryTemp(Entry.Type.ORGNAME, e8.getText().toString());
        EditText e9 = form.findViewById(R.id.vcard_orgunit);
        putEntryTemp(Entry.Type.ORGUNIT, e9.getText().toString());
        EditText e10 = form.findViewById(R.id.vcard_title);
        putEntryTemp(Entry.Type.TITLE, e10.getText().toString());
        EditText e11 = form.findViewById(R.id.vcard_role);
        putEntryTemp(Entry.Type.ROLE, e11.getText().toString());
        EditText e12 = form.findViewById(R.id.vcard_w_tel_1);
        putEntryTemp(Entry.Type.TEL_W_PHONE, e12.getText().toString());
        EditText e13 = form.findViewById(R.id.vcard_w_tel_2);
        putEntryTemp(Entry.Type.TEL_W_FAX, e13.getText().toString());
        EditText e14 = form.findViewById(R.id.vcard_w_tel_3);
        putEntryTemp(Entry.Type.TEL_W_MSG, e14.getText().toString());
        EditText e15 = form.findViewById(R.id.vcard_h_tel_1);
        putEntryTemp(Entry.Type.TEL_H_PHONE, e15.getText().toString());
        EditText e16 = form.findViewById(R.id.vcard_h_tel_2);
        putEntryTemp(Entry.Type.TEL_H_FAX, e16.getText().toString());
        EditText e17 = form.findViewById(R.id.vcard_h_tel_3);
        putEntryTemp(Entry.Type.TEL_H_MSG, e17.getText().toString());
        EditText e18 = form.findViewById(R.id.vcard_w_addr_1);
        putEntryTemp(Entry.Type.W_ADR_EXTADD, e18.getText().toString());
        EditText e19 = form.findViewById(R.id.vcard_w_addr_2);
        putEntryTemp(Entry.Type.W_ADR_STREET, e19.getText().toString());
        EditText e20 = form.findViewById(R.id.vcard_w_addr_3);
        putEntryTemp(Entry.Type.W_ADR_LOCALITY, e20.getText().toString());
        EditText e21 = form.findViewById(R.id.vcard_w_addr_4);
        putEntryTemp(Entry.Type.W_ADR_REGION, e21.getText().toString());
        EditText e22 = form.findViewById(R.id.vcard_w_addr_5);
        putEntryTemp(Entry.Type.W_ADR_PCODE, e22.getText().toString());
        EditText e23 = form.findViewById(R.id.vcard_w_addr_6);
        putEntryTemp(Entry.Type.W_ADR_CTRY, e23.getText().toString());
        EditText e24 = form.findViewById(R.id.vcard_h_addr_1);
        putEntryTemp(Entry.Type.H_ADR_EXTADD, e24.getText().toString());
        EditText e25 = form.findViewById(R.id.vcard_h_addr_2);
        putEntryTemp(Entry.Type.H_ADR_STREET, e25.getText().toString());
        EditText e26 = form.findViewById(R.id.vcard_h_addr_3);
        putEntryTemp(Entry.Type.H_ADR_LOCALITY, e26.getText().toString());
        EditText e27 = form.findViewById(R.id.vcard_h_addr_4);
        putEntryTemp(Entry.Type.H_ADR_REGION, e27.getText().toString());
        EditText e28 = form.findViewById(R.id.vcard_h_addr_5);
        putEntryTemp(Entry.Type.H_ADR_PCODE, e28.getText().toString());
        EditText e29 = form.findViewById(R.id.vcard_h_addr_6);
        putEntryTemp(Entry.Type.H_ADR_CTRY, e29.getText().toString());
        EditText e30 = form.findViewById(R.id.vcard_email);
        putEntryTemp(Entry.Type.EMAIL, e30.getText().toString());
        EditText e31 = form.findViewById(R.id.vcard_jabberid);
        putEntryTemp(Entry.Type.JABBERID, e31.getText().toString());
        EditText e32 = form.findViewById(R.id.vcard_desc);
        putEntryTemp(Entry.Type.DESC, e32.getText().toString());
    }
}