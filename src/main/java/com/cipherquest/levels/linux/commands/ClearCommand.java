package com.cipherquest.levels.linux.commands;

import com.cipherquest.levels.linux.TerminalContext;
import java.util.List;

public final class ClearCommand implements Command {
    // Preserve the sentinel expected by the existing JavaFX terminal UI.
    @Override public String execute(List<String> args, TerminalContext ctx) {
        return "__CLEAR__";
    }
}
