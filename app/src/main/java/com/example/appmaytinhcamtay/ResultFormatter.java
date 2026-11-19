package com.example.appmaytinhcamtay;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class ResultFormatter {

    private static final DecimalFormat FORMAT =
            new DecimalFormat("#,##0.########", DecimalFormatSymbols.getInstance(Locale.US));

    public static String format(double value) {
        return FORMAT.format(value);
    }
}
