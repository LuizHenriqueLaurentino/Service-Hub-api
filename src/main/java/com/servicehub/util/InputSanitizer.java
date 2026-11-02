package com.servicehub.util;

import java.util.regex.Pattern;

/**
 * Sanitização de entradas de texto (proteção contra XSS armazenado).
 * Remove blocos script/style, qualquer tag HTML, caracteres "<" e ">" soltos e
 * caracteres de controle, e por fim aplica trim().
 */
public final class InputSanitizer {

    private static final Pattern SCRIPT_OR_STYLE = Pattern.compile("(?is)<(script|style)[^>]*>.*?</\\1\\s*>");
    private static final Pattern HTML_TAG = Pattern.compile("(?s)<[^>]*>");
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}&&[^\\r\\n\\t]]");

    private InputSanitizer() {
    }

    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        String result = SCRIPT_OR_STYLE.matcher(input).replaceAll("");
        result = HTML_TAG.matcher(result).replaceAll("");
        result = result.replace("<", "").replace(">", "");
        result = CONTROL_CHARS.matcher(result).replaceAll("");
        return result.trim();
    }
}
