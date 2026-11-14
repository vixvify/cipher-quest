package com.cipherquest.levels.linux.commands;

import com.cipherquest.levels.linux.TerminalContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CatCommand implements Command {
    @Override public String execute(List<String> args, TerminalContext ctx) throws IOException {
        if (args.isEmpty()) return "cat: missing file operand";
        List<String> results = new ArrayList<>();
        for (String target : args) {
            Path path = ctx.resolve(target);
            if (Files.isDirectory(path)) {
                results.add("cat: " + target + ": Is a directory");
            } else if (Files.exists(path)) {
                results.add(Files.readString(path));
            } else {
                results.add("cat: " + target + ": No such file or directory");
            }
        }
        return String.join("\n", results);
    }
}
