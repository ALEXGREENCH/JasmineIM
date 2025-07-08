package ru.ivansuper.jasmin.icq.FileTransfer;

import ru.ivansuper.jasmin.icq.ByteBuffer;

public class OFTParser {
    public int command;
    public String file_name;
    public int file_size;
    public int files_left;
    public int flags;
    public int total_files;
    public int total_size;

    public OFTParser(ByteBuffer data) {
        data.readPos = 0;
        data.skip(6);
        this.command = data.readWord();
        data.skip(12);
        this.total_files = data.readWord();
        this.files_left = data.readWord();
        data.skip(4);
        this.total_size = data.readDWord();
        this.file_size = data.readDWord();
        data.skip(64);
        this.flags = data.readByte();
        data.skip(87);
        int encoding = data.readWord();
        data.readWord();
        int name_length = data.getDoubleZeroTerminatedStringLength();
        if (name_length == -1) {
            this.file_name = "no_name_" + System.currentTimeMillis() + ".jif";
            data.readPos = 0;
            return;
        }
        switch (encoding) {
            case 1:
            case 0:
            default:
                this.file_name = data.readStringAscii(name_length);
                break;
            case 2:
                this.file_name = data.readStringUnicode(name_length);
                break;
            case 3:
                this.file_name = data.readString1251(name_length);
                break;
        }
        data.readPos = 0;
    }
}
