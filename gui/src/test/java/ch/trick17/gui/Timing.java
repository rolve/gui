package ch.trick17.gui;

import java.util.List;

import static java.util.Arrays.stream;

public class Timing {

    public static void main(String[] args) {
        for (int sleepTime : List.of(5, 10, 20, 50)) {
            Gui gui = Gui.create("Timing", 400, 400);
            gui.open();

            long[] diffs = new long[10_000 / sleepTime];
            long before = System.nanoTime();
            for (int i = 0; i < diffs.length; i++) {
                gui.refreshAndClear(sleepTime);
                var now = System.nanoTime();
                diffs[i] = now - before;
                before = now;
            }
            gui.close();

            var stats = stream(diffs)
                    .mapToDouble(nanos -> nanos / 1_000_000.0)
                    .summaryStatistics();
            System.out.printf("Target:  %d ms\n", sleepTime);
            System.out.printf("Average: %.1f ms\n", stats.getAverage());
            System.out.printf("Min:     %.1f ms\n", stats.getMin());
            System.out.printf("Max:     %.1f ms\n\n", stats.getMax());
        }
    }
}
