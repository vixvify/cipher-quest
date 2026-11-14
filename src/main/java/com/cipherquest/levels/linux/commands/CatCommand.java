package com.cipherquest.levels.linux.commands;

import com.cipherquest.levels.linux.TerminalContext;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class CatCommand implements Command {

    @Override
    public String execute(
        List<String> args,
        TerminalContext context
    ) throws IOException {

        if (args.isEmpty()) {
            return "cat: missing file operand";
        }

        Path path = context.resolve(args.get(0));

        if (!Files.exists(path)) {
            return "cat: No such file or directory";
        }

        if (Files.isDirectory(path)) {
            return "cat: Is a directory";
        }

        return Files.readString(path);
    }
}