import ru.ivansuper.jasmin.compat.ScrollIndicatorGeometry;

/** Behavioral checks for the API-4 scrollbar, independent of Android stubs. */
public final class ScrollIndicatorTest {
    private static int checks;
    private static void equal(int expected, int actual) {
        checks++;
        if (expected != actual) throw new AssertionError("Expected " + expected + ", got " + actual);
    }

    public static void main(String[] args) {
        equal(0, ScrollIndicatorGeometry.thumbHeight(300, 0, 1000, 24));
        equal(0, ScrollIndicatorGeometry.thumbHeight(300, 1000, 1000, 24));
        equal(0, ScrollIndicatorGeometry.thumbHeight(0, 100, 1000, 24));
        equal(75, ScrollIndicatorGeometry.thumbHeight(300, 250, 1000, 24));
        equal(24, ScrollIndicatorGeometry.thumbHeight(300, 1, 1000, 24));
        equal(10, ScrollIndicatorGeometry.thumbHeight(10, 1, 1000, 24));
        equal(1000000000, ScrollIndicatorGeometry.thumbHeight(2000000000, 1000000000, 2000000000, 24));
        equal(0, ScrollIndicatorGeometry.thumbTop(300, 75, -50, 250, 1000));
        equal(0, ScrollIndicatorGeometry.thumbTop(300, 75, 0, 250, 1000));
        equal(112, ScrollIndicatorGeometry.thumbTop(300, 75, 375, 250, 1000));
        equal(225, ScrollIndicatorGeometry.thumbTop(300, 75, 750, 250, 1000));
        equal(225, ScrollIndicatorGeometry.thumbTop(300, 75, 999, 250, 1000));
        equal(0, ScrollIndicatorGeometry.thumbTop(300, 300, 100, 1, 1000));
        equal(1000000000, ScrollIndicatorGeometry.thumbTop(2000000000, 1000000000, 1000000000, 1000000000, 2000000000));
        int previous = -1;
        for (int offset = 0; offset <= 750; offset++) {
            int top = ScrollIndicatorGeometry.thumbTop(300, 75, offset, 250, 1000);
            checks++;
            if (top < previous || top < 0 || top + 75 > 300) throw new AssertionError("Thumb outside track or moves backward");
            previous = top;
        }
        equal(255, ScrollIndicatorGeometry.alpha(999, 1000, 300));
        equal(255, ScrollIndicatorGeometry.alpha(1000, 1000, 300));
        equal(128, ScrollIndicatorGeometry.alpha(1150, 1000, 300));
        equal(0, ScrollIndicatorGeometry.alpha(1300, 1000, 300));
        equal(0, ScrollIndicatorGeometry.alpha(100000, 1000, 300));
        equal(0, ScrollIndicatorGeometry.alpha(1001, 1000, 0));
        previous = 255;
        for (int time = 1000; time <= 1400; time++) {
            int alpha = ScrollIndicatorGeometry.alpha(time, 1000, 300);
            checks++;
            if (alpha > previous || alpha < 0 || alpha > 255) throw new AssertionError("Fade reverses or leaves opacity range");
            previous = alpha;
        }
        System.out.println("PASS: " + checks + " scrollbar geometry/fade checks");
    }
}
