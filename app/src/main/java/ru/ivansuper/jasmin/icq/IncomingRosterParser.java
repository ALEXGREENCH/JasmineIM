package ru.ivansuper.jasmin.icq;

import java.io.IOException;

/**
 * Parses incoming roster (contact list) data from the ICQ server.
 * This class is responsible for interpreting the binary data stream
 * representing the user's contact list and updating the local
 * {@link ICQProfile} with the parsed information.
 *
 * <p>The roster data includes information about contacts (UINs, nicknames, groups),
 * groups themselves, and visibility settings (visible, invisible, ignore lists).
 *
 * <p>The parsing logic iterates through SSI (Server-Side Information) items,
 * each representing a contact, group, or visibility setting. It handles
 * different item types (contacts, groups, visibility lists, etc.) and
 * extracts relevant information using TLV (Type-Length-Value) structures.
 */
public class IncomingRosterParser {
    public void parse(ByteBuffer buffer, ICQProfile profile) {
        String nickname;
        buffer.skip(1);
        int SSICount = buffer.readWord();
        for (int i = 0; i < SSICount; i++) {
            String itemName = "null";
            try {
                itemName = buffer.readStringUTF8(buffer.readWord());
            } catch (IOException e1) {
                //noinspection CallToPrintStackTrace
                e1.printStackTrace();
            }
            int group = buffer.readWord();
            int id = buffer.readWord();
            int type = buffer.readWord();
            int additionalLength = buffer.readWord();
            if (type == 0) {
                if (group == 0) {
                    buffer.skip(additionalLength);
                } else {
                    ICQContact contact = profile.contactlist.getContactByUIN(itemName);
                    if (contact != null) {
                        contact.ID = itemName;
                        contact.name = itemName;
                        contact.profile = profile;
                        contact.group = group;
                        contact.id = id;
                        if (profile.contactlist.getGroupById(group).isNotIntList) {
                            contact.added = false;
                            contact.as_accepted = false;
                        }
                    } else {
                        contact = new ICQContact();
                        contact.ID = itemName;
                        contact.name = itemName;
                        contact.profile = profile;
                        contact.group = group;
                        contact.id = id;
                        contact.init();
                        if (profile.contactlist.getGroupById(group).isNotIntList) {
                            contact.added = false;
                            contact.as_accepted = false;
                        }
                        profile.contactlist.put(contact);
                    }
                    int stamp = buffer.readPos + additionalLength;
                    while (buffer.readPos < stamp) {
                        int tlvType = buffer.readWord();
                        int tlvLength = buffer.readWord();
                        if (tlvType == 305) {
                            byte[] datachunk = ByteCache.getByteArray(tlvLength);
                            buffer.readBytes(tlvLength, datachunk);
                            TLV tlv = new TLV(datachunk, tlvType, tlvLength);
                            ByteBuffer tlvData = tlv.getData();
                            int len = tlv.length;
                            int backup = tlvData.readPos;
                            try {
                                nickname = tlvData.readStringUTF8(len);
                            } catch (IOException e) {
                                tlvData.readPos = backup;
                                nickname = tlvData.readString1251(len);
                                //noinspection CallToPrintStackTrace
                                e.printStackTrace();
                            }
                            if (!nickname.isEmpty()) {
                                contact.name = nickname;
                            } else {
                                contact.name = itemName + " [cant read nick]";
                            }
                            tlv.recycle();
                        } else if (tlvType == 102) {
                            contact.authorized = false;
                        } else {
                            buffer.skip(tlvLength);
                        }
                    }
                }
            } else if (type == 1) {
                if (group != 0) {
                    ICQGroup grp = new ICQGroup();
                    grp.id = group;
                    grp.name = itemName;
                    grp.profile = profile;
                    grp.opened = profile.sp.getBoolean("g" + group, true);
                    profile.contactlist.put(grp);
                    TLVList list = new TLVList(buffer, additionalLength, true);
                    if (list.getTLV(106) != null) {
                        grp.isNotIntList = true;
                        grp.opened = true;
                    }
                    list.recycle();
                } else {
                    buffer.skip(additionalLength);
                }
            } else if (type == 4) {
                int backup2 = buffer.readPos;
                TLVList list2 = new TLVList(buffer, additionalLength, true);
                TLV tlv202 = list2.getTLV(202);
                if (tlv202 != null) {
                    //noinspection WriteOnlyObject
                    ByteBuffer data202 = tlv202.getData();
                    data202.readByte();
                    profile.visibilityId = id;
                }
                buffer.readPos = backup2;
                buffer.skip(additionalLength);
                list2.recycle();
            } else if (type == 2) {
                if (profile.isInVisible(itemName) == null) {
                    ssi_item vi = new ssi_item();
                    vi.uin = itemName;
                    vi.id = id;
                    vi.listType = type;
                    synchronized (profile.visible_list) {
                        profile.visible_list.add(vi);
                    }
                }
                buffer.skip(additionalLength);
            } else if (type == 3) {
                if (profile.isInInvisible(itemName) == null) {
                    ssi_item vi2 = new ssi_item();
                    vi2.uin = itemName;
                    vi2.id = id;
                    vi2.listType = type;
                    synchronized (profile.invisible_list) {
                        profile.invisible_list.add(vi2);
                    }
                }
                buffer.skip(additionalLength);
            } else if (type == 14) {
                if (profile.isInIgnore(itemName) == null) {
                    ssi_item vi3 = new ssi_item();
                    vi3.uin = itemName;
                    vi3.id = id;
                    vi3.listType = type;
                    synchronized (profile.ignore_list) {
                        profile.ignore_list.add(vi3);
                    }
                }
                buffer.skip(additionalLength);
            } else if (type == 25) {
                profile.addPhantom(itemName, id, type);
                buffer.skip(additionalLength);
            } else if (type == 20) {
                profile.buddy_name = itemName;
                profile.buddy_group = group;
                profile.buddy_id = id;
                buffer.skip(additionalLength);
            } else {
                buffer.skip(additionalLength);
            }
        }
    }

    /** @noinspection unused*/
    public int getTlvCountByBlockSize(ByteBuffer buffer, int length) {
        int res = 0;
        int marker = 0;
        while (marker < length) {
            int marker2 = marker + 2;
            int size = buffer.previewWord(marker2);
            marker = marker2 + size + 2;
            res++;
        }
        return res;
    }
}