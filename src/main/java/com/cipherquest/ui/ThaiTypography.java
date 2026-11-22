package com.cipherquest.ui;

import javafx.css.PseudoClass;
import javafx.scene.Node;

import java.text.BreakIterator;
import java.util.Locale;

final class ThaiTypography {
    private static final Locale THAI = Locale.forLanguageTag("th");
    private static final PseudoClass THAI_TEXT = PseudoClass.getPseudoClass("thai-text");

    private ThaiTypography() {}

    static void useThaiFontWhenNeeded(Node node, String text) {
        node.pseudoClassStateChanged(THAI_TEXT, text.codePoints().anyMatch(ThaiTypography::isThai));
    }

    static String withWordBreaks(String text) {
        if (!text.codePoints().anyMatch(ThaiTypography::isThai)) {
            return text;
        }
        BreakIterator words = BreakIterator.getWordInstance(THAI);
        words.setText(text);
        StringBuilder wrapped = new StringBuilder(text.length());
        int previous = words.first();
        for (int end = words.next(); end != BreakIterator.DONE; end = words.next()) {
            wrapped.append(text, previous, end);
            if (end < text.length() && isThai(text.codePointBefore(end)) && isThai(text.codePointAt(end))) {
                wrapped.append('\u200B');
            }
            previous = end;
        }
        return wrapped.toString();
    }

    private static boolean isThai(int character) {
        return Character.UnicodeScript.of(character) == Character.UnicodeScript.THAI;
    }
}
