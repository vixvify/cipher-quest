package com.cipherquest.levels.linux.commands;

import com.cipherquest.levels.linux.TerminalContext;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CdCommand implements Command {
    @Override public String execute(List<String> args, TerminalContext ctx) {
        String target = args.isEmpty() ? "~" : args.get(0);
        if (target.equals("-")) return ctx.switchToPreviousDirectory().toString();
        Path path = ctx.resolve(target);
        if (Files.isDirectory(path)) {
            ctx.changeDirectory(path);
            return "";
        }
        if (Files.exists(path)) return "bash: cd: " + target + ": Not a directory";
        return "bash: cd: " + target + ": No such file or directory";
    }
}
