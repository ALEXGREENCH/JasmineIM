package ru.ivansuper.jasmin.plugins._interface;

/**
 * A Runnable task that is associated with an integer identifier.
 * This class allows for tracking or categorizing runnable tasks by an ID.
 */
public class IdentificatedRunnable {
    public int id;
    public Runnable task;

    public IdentificatedRunnable(Runnable task, int id) {
        this.task = task;
        this.id = id;
    }
}