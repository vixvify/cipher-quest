package com.cipherquest.levels.linux.commands;

import com.cipherquest.levels.linux.TerminalContext;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class LsCommand implements Command {

    @Override
    public String execute(
        List<String> args,
        TerminalContext context
    ) throws IOException {

        boolean showAll = false;
        String target = ".";

        for (String arg : args) {
            if (arg.startsWith("-")) {
                showAll |= arg.contains("a");
            } else {
                target = arg;
            }
        }

        Path path = context.resolve(target);

        if (!Files.exists(path)) {
            return "ls: No such file or directory";
        }

        if (!Files.isDirectory(path)) {
            return path.getFileName().toString();
        }

        final boolean includeHidden = showAll;

        try (Stream<Path> stream = Files.list(path)) {
            return stream
                .filter(p ->
                    includeHidden ||
                    !p.getFileName().toString().startsWith(".")
                )
                .map(p ->
                    p.getFileName().toString()
                    + (Files.isDirectory(p) ? "/" : "")
                )
                .sorted()
                .collect(Collectors.joining("  "));
        }
    }
}