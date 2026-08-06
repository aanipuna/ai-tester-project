package com.dialog.dtg.cli;

public final class CliErrorFormatter {

    private CliErrorFormatter() {
    }

    public static String format(String errorCode, String message) {
        return "[" + errorCode + "] " + message;
    }
}
