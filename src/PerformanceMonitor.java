/**
 * PerformanceMonitor.java
 * ======================
 * Helper class for timing execution, memory footprint profiling, and formatting metrics.
 */
public class PerformanceMonitor {

    public static long getUsedMemoryBytes() {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); // Suggest GC for cleaner memory baseline
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.2f %cB", bytes / Math.pow(1024, exp), pre);
    }

    public static String formatDurationMs(double ms) {
        if (ms < 0.001) {
            return String.format("%.3f µs", ms * 1000.0);
        } else if (ms < 1000.0) {
            return String.format("%.3f ms", ms);
        } else {
            return String.format("%.4f s", ms / 1000.0);
        }
    }
}
