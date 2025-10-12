package com.jtprince.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Utility to print condensed stacktraces for when the lower frames of the stack are irrelevant. Also contains utilities
 * for rate limiting repeated exceptions.
 */
public class PartialStacktraceLogger {
    private final Logger logger;
    public PartialStacktraceLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Get the stacktrace of an exception as a string, only including frames between the exception itself and the caller
     * of this function.
     */
    private String getStackTraceString(String message, Exception e) {
        List<StackTraceElement> stack = new ArrayList<>(Arrays.asList(e.getStackTrace()));
        List<StackTraceElement> current = new ArrayList<>(Arrays.asList(Thread.currentThread().getStackTrace()));

        // Pop off common elements
        final int popBuffer = 2;
        int popped = 0;
        while (stack.size() > popBuffer+1 && current.size() > popBuffer+1 &&
            stack.get(stack.size()-popBuffer).equals(current.get(current.size()-popBuffer))) {
            stack.remove(stack.size()-1);
            current.remove(current.size()-1);
            popped++;
        }

        return message +
            "\nCaused by " + e.getClass().getName() + ": " + e.getMessage() + "\n  " +
            stack.stream().map(StackTraceElement::toString).collect(Collectors.joining("\n  ")) +
            "\n (+" + popped + " hidden frames)";
    }

    /**
     * Log a stacktrace based on an exception, including frames between the exception itself and the caller of this
     * log function.
     * @param message Message to include above the stacktrace
     * @param e Exception containing the stacktrace to log
     */
    public void logStacktrace(String message, Exception e) {
        logger.severe(getStackTraceString(message, e));
    }

    private static class RateLimitEntry {
        long lastLoggedTime;
        int numUnloggedOccurrences = 0;
        String lastUnloggedStacktrace;
        Exception lastUnloggedException;
    }
    private record RateLimitEntryKey(Class<? extends Exception> clazz, String aggregateKey) {}
    private final HashMap<RateLimitEntryKey, RateLimitEntry> activeRateLimits = new HashMap<>();

    /**
     * Log a stacktrace based on an exception, including frames between the exception itself and the caller of this
     * log function, with built-in rate limiting for repeated exceptions.
     *
     * @param logger Logger object to log to.
     * @param message Message to include above the stacktrace
     * @param e Exception containing the stacktrace to log
     * @param rateLimitIntervalMs Minimum time interval between logging the same exception class with the same
     *                            aggregateKey, in milliseconds.
     * @param aggregateKey Key that identifies this exception for rate limiting purposes. Exceptions with the same
     *                     aggregateKey and class will be rate limited together. This will be printed in the log message
     *                     if preceding exceptions were rate limited.
     *
     * @return True if the stacktrace was logged, false if it was rate limited.
     */
    public boolean logStacktraceRateLimited(Logger logger, String message, Exception e,
                                            long rateLimitIntervalMs, String aggregateKey) {
        long currentTime = System.currentTimeMillis();
        RateLimitEntryKey key = new RateLimitEntryKey(e.getClass(), aggregateKey);
        RateLimitEntry entry = activeRateLimits.get(key);

        boolean print = true;
        if (entry == null) {
            entry = new RateLimitEntry();
            entry.lastLoggedTime = currentTime;
            activeRateLimits.put(key, entry);
        } else {
            if (currentTime - entry.lastLoggedTime < rateLimitIntervalMs) {
                print = false;
            }
        }

        if (print) {
            if (entry.numUnloggedOccurrences > 0) {
                logger.severe(entry.numUnloggedOccurrences +
                    " occurrence" + (entry.numUnloggedOccurrences == 1 ? "" : "s") +
                    " of the following exception " + (entry.numUnloggedOccurrences == 1 ? "was" : "were") +
                    " rate limited for " + aggregateKey +
                    " in the last " + (currentTime - entry.lastLoggedTime) + "ms.");
                entry.lastLoggedTime = currentTime;
                entry.numUnloggedOccurrences = 0;
                entry.lastUnloggedStacktrace = null;
                entry.lastUnloggedException = null;
            }
            logStacktrace(message, e);
        } else {
            entry.numUnloggedOccurrences++;
            entry.lastUnloggedStacktrace = getStackTraceString(message, e);
            entry.lastUnloggedException = e;
        }

        return print;
    }

    /**
     * Clear stale rate limit entries and print rate limited exceptions.
     * <p>
     * Rate limiting means that the second and subsequent occurrences of the same exception are hidden until two
     * conditions are met:
     * <ul>
     * <li>A certain amount of time has passed since the last time the exception was printed, and</li>
     * <li>The exception occurs again.</li>
     * </ul>
     * <p>
     * If the two conditions never line up, some rate limited exceptions will never be printed. This function can be
     * called periodically to ensure that the full scope of rate limited exceptions is always printed.
     *
     * @param minAgeMs Minimum age of a rate limit entry to be cleared, in milliseconds. Entries older than this will be
     *                 cleared and the last rate limited exceptions will be printed if it was not yet printed.
     */
    public void flushRateLimits(long minAgeMs) {
        long currentTime = System.currentTimeMillis();
        activeRateLimits.keySet().removeIf(key -> {
            RateLimitEntry entry = activeRateLimits.get(key);

            boolean flush = (currentTime - entry.lastLoggedTime >= minAgeMs);

            if (flush && entry.numUnloggedOccurrences > 0) {
                logger.severe(entry.numUnloggedOccurrences +
                    " occurrence" + (entry.numUnloggedOccurrences == 1 ? "" : "s") +
                    " of " + entry.lastUnloggedException.getClass().getSimpleName() +
                    " " + (entry.numUnloggedOccurrences == 1 ? "was" : "were") +
                    " rate limited for " + key.aggregateKey +
                    " in the last " + (currentTime - entry.lastLoggedTime) + "ms. Last stacktrace was:");
                logger.severe(entry.lastUnloggedStacktrace);
            }
            return flush;
        });
    }

//    public static void recursiveException(int frameCount) {
//        if (frameCount == 0) {
//            throw new ArrayIndexOutOfBoundsException("Test exception only!");
//        } else {
//            recursiveException(frameCount-1);
//        }
//    }
}
