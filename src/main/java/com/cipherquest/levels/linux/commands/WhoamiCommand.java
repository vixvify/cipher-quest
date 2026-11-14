package com.cipherquest.levels.linux.commands;

import com.cipherquest.levels.linux.TerminalContext;
import java.util.List;

public final class WhoamiCommand implements Command {
    @Override public String execute(List<String> args, TerminalContext ctx) {
        return "agent";
    }
}
