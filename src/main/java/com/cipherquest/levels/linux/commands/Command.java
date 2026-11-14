package com.cipherquest.levels.linux.commands;

import com.cipherquest.levels.linux.TerminalContext;
import java.io.IOException;
import java.util.List;

public interface Command {
    String execute(
        List<String> args,
        TerminalContext context
    ) throws IOException;
}