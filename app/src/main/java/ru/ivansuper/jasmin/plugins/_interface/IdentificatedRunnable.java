package ru.ivansuper.jasmin.plugins._interface;

/** @noinspection unused*/
public class IdentificatedRunnable {
    public int id;
    public Runnable task;

    public IdentificatedRunnable(Runnable task, int id) {
        this.task = task;
        this.id = id;
    }
}