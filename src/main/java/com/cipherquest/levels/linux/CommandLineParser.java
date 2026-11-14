package com.cipherquest.levels.linux;

import java.util.ArrayList;
import java.util.List;

/** Minimal shell-like tokenizer. Supports whitespace, quotes, and backslash escaping.
 * Does not expand shell variables or execute pipes/redirections. */
public final class CommandLineParser {
    private CommandLineParser() {}

    public static List<String> parse(String input) {
        List<String> result = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        char quote = 0;
        boolean escaped = false;
        boolean started = false;
        for (char ch : input.toCharArray()) {
            if (escaped) {
                token.append(ch);
                escaped = false;
                started = true;
            } else if (ch == '\\' && quote != '\'') {
                escaped = true;
                started = true;
            } else if (quote != 0) {
                if (ch == quote) quote = 0;
                else token.append(ch);
            } else if (ch == '\'' || ch == '"') {
                quote = ch;
                started = true;
            } else if (Character.isWhitespace(ch)) {
                if (started) {
                    result.add(token.toString());
                    token.setLength(0);
                    started = false;
                }
            } else {
                token.append(ch);
                started = true;
            }
        }
        if (escaped) throw new IllegalArgumentException("unfinished escape sequence");
        if (quote != 0) throw new IllegalArgumentException("unclosed quote");
        if (started) result.add(token.toString());
        return result;
    }
}
