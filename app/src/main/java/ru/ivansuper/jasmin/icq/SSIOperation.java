package ru.ivansuper.jasmin.icq;

public class SSIOperation {
    public Object object;
    public Object objectA;
    /** @noinspection unused*/
    public Object objectB;
    public int operationType;

    public SSIOperation(int type, Object obj) {
        this.operationType = type;
        this.object = obj;
    }
}