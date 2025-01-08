package dk.spilstuff.engine;

import java.util.ArrayList;

public class Logger {
    private static ArrayList<String> log = new ArrayList<String>();
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    public static void addLog(String string) {
        log.add(string);
    }

    public static void addError(String string) {
        log.add(ANSI_RED + "ERROR: " + ANSI_RESET + string);
    }

    public static void addSuccess(String string) {
        log.add(ANSI_GREEN + "SUCCESS: " + ANSI_RESET + string);
    }

    public static void addWarning(String string) {
        log.add(ANSI_YELLOW + "WARNING: " + ANSI_RESET + string);
    }

    public static void write(int index) {
        System.out.println(log.get(index));
    }

    public static void writeAll() {
        for(String message : log) {
            System.out.println(message);
        }
    }

    public static void clear() {
        log.clear();
    }
}
