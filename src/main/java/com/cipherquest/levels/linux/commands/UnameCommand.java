package com.cipherquest.levels.linux.commands;

import com.cipherquest.levels.linux.TerminalContext;
import java.util.List;

public final class UnameCommand implements Command {
    @Override public String execute(List<String> args, TerminalContext ctx) {
        return args.contains("-a")
                ? "Linux cipherquest-terminal 5.15.0-x86_64 GNU/Linux"
                : "Linux";
    }
}
