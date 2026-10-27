package db;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/** java.io usage: appends session events to hacker_log.txt */
public final class LogWriter {
    private static final String FILE = "hacker_log.txt";

    private LogWriter() { }

    public static synchronized void log(String message) {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE, true))) {
            out.println(LocalDateTime.now() + "  " + message);
        } catch (IOException e) {
            System.err.println("Could not write log: " + e.getMessage());
        }
    }
}
