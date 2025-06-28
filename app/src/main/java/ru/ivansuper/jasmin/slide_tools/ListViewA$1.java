package ru.ivansuper.jasmin.slide_tools;

class ListViewA$1 implements Runnable {
    final ListViewA this$0;

    ListViewA$1(ListViewA var1) {
        this.this$0 = var1;
    }

    public void run() {
        this.this$0.setTranscriptMode(1);
        this.this$0.requestLayout();
    }
}