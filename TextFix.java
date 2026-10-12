package com.example.visuals;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Small text helpers. */
public final class TextFix {
    private static final Pattern ESCAPE = Pattern.compile("\\\\+u([0-9a-fA-F]{4})");

    private TextFix() {}

    /** Turns literal "\\u0430"-style sequences (left as plain text by some resource packs) into real characters. */
    public static String decodeUnicodeEscapes(String s) {
        if (s == null || s.indexOf('u') < 0 || s.indexOf('\\') < 0) return s;
        Matcher m = ESCAPE.matcher(s);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(out, Matcher.quoteReplacement(String.valueOf((char) Integer.parseInt(m.group(1), 16))));
        }
        m.appendTail(out);
        return out.toString();
    }
}
