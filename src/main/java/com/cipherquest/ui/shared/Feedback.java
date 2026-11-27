package com.cipherquest.ui.shared;

import javafx.css.PseudoClass;
import javafx.scene.control.Label;

public final class Feedback {
    private static final PseudoClass SUCCESS = PseudoClass.getPseudoClass("success");
    private static final PseudoClass ERROR = PseudoClass.getPseudoClass("error");

    private Feedback() {}

    public static void show(Label label, String message, boolean success, boolean error) {
        label.setText(message);
        label.pseudoClassStateChanged(SUCCESS, success);
        label.pseudoClassStateChanged(ERROR, error);
    }
}
